/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service.implement;

import com.stripe.StripeClient;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import dao.ClaveLicenciaDao;
import dao.FacturasDAO;
import dao.PedidoDao;
import exception.pago.PagoNoCompletadoException;
import exception.pago.SinClavesDisponiblesException;
import exception.pedido.PedidoNoEncontradoException;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import model.catalogo.Videojuego;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import model.ventas.EstadoPedido;
import model.ventas.Factura;
import model.ventas.LineaPedido;
import model.ventas.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import service.interfaces.CestaService;
import service.interfaces.ClaveLicenciaService;
import service.interfaces.PagoService;
import service.interfaces.PedidoService;

/**
 *
 * @author Sandy
 */
@Service
@Transactional(readOnly = true)
public class PagoServiceImpl implements PagoService {

    // Vienen de application.properties (que a su vez lee la variable de entorno)
    @Value("${stripe.secret-key}")
    private String claveSecreta;

    // Stripe caduca la pagina de pago a los 31 minutos (su minimo es 30). Nosotros cancelamos
    // un poco despues, para no cancelar un pedido que aun se puede pagar
    private static final long MINUTOS_CADUCIDAD_STRIPE = 31;
    private static final long MINUTOS_CANCELAR_PEDIDO = 35;

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private PedidoDao pedidoDao;

    @Autowired
    private ClaveLicenciaDao claveLicenciaDao;

    @Autowired
    private FacturasDAO facturasDAO;

    @Autowired
    private CestaService cestaService;

    // Para avisar a los gestores de inventario cuando, despues de vender, quedan pocas claves.
    // El pago solo vende; contar el stock y mandar el correo lo hace el service de claves
    @Autowired
    private ClaveLicenciaService claveLicenciaService; //para avisar a los gestores de inventario cuando, despues de vender, quedan pocas claves.

    @Override
    @Transactional
    public String iniciarPago(String email) {

        StripeClient stripe = clienteStripe();

        // Si algo falla despues de esta linea, la transaccion entera se deshace y el pedido
        // no se queda guardado a medias
        Pedido pedido = pedidoService.crearPedidoDesdeCesta(email);

        comprobarQueHayClaves(pedido);
        // La direccion desde la que ha entrado el usuario (localhost, la del servidor...), para
        // que Stripe le devuelva al mismo sitio. No hace falta configurarla en ningun archivo
        String urlBase = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();                    //EL RETORNOOOO

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(email)
                // Es lo que nos permite saber, al volver, de que pedido es esta sesion
                .setClientReferenceId(String.valueOf(pedido.getId()))
                // Stripe cambia {CHECKOUT_SESSION_ID} por el id real de la sesion al volver
                .setSuccessUrl(urlBase + "/tienda/pago/exito?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(urlBase + "/tienda/pago/cancelado")
                .setExpiresAt(Instant.now().plusSeconds(MINUTOS_CADUCIDAD_STRIPE * 60).getEpochSecond());

        // Una linea de Stripe por cada linea del pedido. Los precios salen de NUESTRA base de
        // datos (del pedido ya creado), nunca del formulario
        for (LineaPedido linea : pedido.getLineas()) {
            params.addLineItem(SessionCreateParams.LineItem.builder()
                    .setQuantity(1L)
                    .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("eur")
                            .setUnitAmount(enCentimos(linea))
                            .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(linea.getVideojuego().getTitulo())
                                    .build())
                            .build())
                    .build());
        }

        try {
            Session sesion = stripe.v1().checkout().sessions().create(params.build());
            return sesion.getUrl();
        } catch (StripeException e) {
            throw new PagoNoCompletadoException("pago.error.conectar");
        }
    }

    @Override
    @Transactional
    public Pedido confirmarPago(String email, String sessionId) {

        StripeClient stripe = clienteStripe();

        // No nos fiamos de que el cliente haya vuelto a esta direccion: se lo preguntamos a Stripe
        Session sesion;
        try {
            sesion = stripe.v1().checkout().sessions().retrieve(sessionId);
        } catch (StripeException e) {
            throw new PagoNoCompletadoException("pago.error.comprobar");
        }

        if (!"paid".equals(sesion.getPaymentStatus())) {
            throw new PagoNoCompletadoException("pago.error.noCompletado");
        }

        Long pedidoId = Long.valueOf(sesion.getClientReferenceId());
        Pedido pedido = pedidoDao.buscarConDetalle(pedidoId)
                .orElseThrow(() -> new PedidoNoEncontradoException(pedidoId));

        // Control de propiedad: el pedido tiene que ser del cliente que ha entrado
        if (!pedido.getCliente().getEmail().equals(email)) {
            throw new PedidoNoEncontradoException(pedidoId);
        }

        // Si recarga esta pagina, el pedido ya esta pagado: no se entregan las claves otra vez
        if (pedido.getEstado() == EstadoPedido.PAGADO) {
            return pedido;
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE_PAGO) {
            throw new PagoNoCompletadoException("pago.error.yaNoPagable");
        }

        asignarClaves(pedido);
        for (LineaPedido linea : pedido.getLineas()) {
            claveLicenciaService.avisarSiQuedaPocoStock(linea.getVideojuego()); // Ya vendidas las claves: si de algun juego quedan pocas, se avisa a inventario
        }
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setMetodoPago("STRIPE");
        crearFactura(pedido);
        cestaService.vaciarCesta(email);

        return pedidoDao.save(pedido);
    }

    private StripeClient clienteStripe() {
        if (claveSecreta == null || claveSecreta.isBlank()) {
            throw new PagoNoCompletadoException("pago.error.sinClaveStripe");
        }
        return new StripeClient(claveSecreta);
    }

    // Avisa antes de cobrar: pagar y descubrir despues que no hay clave seria peor
    private void comprobarQueHayClaves(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Videojuego videojuego = linea.getVideojuego();
            if (!claveLicenciaDao.existsByVideojuegoIdAndEstado(videojuego.getId(), EstadoClave.DISPONIBLE)) {
                throw new SinClavesDisponiblesException(videojuego.getTitulo());
            }
        }
    }

    // Cada linea del pedido se lleva UNA clave: pasa de DISPONIBLE a VENDIDA y queda ligada
    // al cliente y a esa linea
    private void asignarClaves(Pedido pedido) {
        for (LineaPedido linea : pedido.getLineas()) {
            Videojuego videojuego = linea.getVideojuego();
            ClaveLicencia clave = claveLicenciaDao
                    .findFirstByVideojuegoIdAndEstadoOrderByIdAsc(videojuego.getId(), EstadoClave.DISPONIBLE)
                    .orElseThrow(() -> new SinClavesDisponiblesException(videojuego.getTitulo()));
            clave.setEstado(EstadoClave.VENDIDA);
            clave.setCliente(pedido.getCliente());
            clave.setLineaPedido(linea);
            clave.setFechaVenta(LocalDateTime.now());
        }
    }

    private void crearFactura(Pedido pedido) {
        Factura factura = new Factura();
        factura.setPedido(pedido);
        factura.setNumero(String.format("FAC-%d-%05d", LocalDate.now().getYear(), facturasDAO.count() + 1));
        factura.setFechaEmision(LocalDate.now());
        facturasDAO.save(factura);
    }

    // Stripe quiere el importe en centimos y como entero: 24,99 EUR -> 2499
    private long enCentimos(LineaPedido linea) {
        return linea.getPrecioUnitario().subtract(linea.getDescuento())
                .movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    @Override
    @Transactional
    public int cancelarPedidosCaducados() {

        LocalDateTime limite = LocalDateTime.now().minusMinutes(MINUTOS_CANCELAR_PEDIDO);

        List<Pedido> caducados = pedidoDao.buscarPendientesAnterioresA(EstadoPedido.PENDIENTE_PAGO, limite);

        for (Pedido pedido : caducados) {
            pedido.setEstado(EstadoPedido.CANCELADO);
            pedidoDao.save(pedido);
        }
        return caducados.size();
    }

}
