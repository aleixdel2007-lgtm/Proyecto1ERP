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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
import service.interfaces.CestaService;
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

    @Value("${app.url}")
    private String urlBase;

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

    @Override
    @Transactional
    public String iniciarPago(String email) {

        StripeClient stripe = clienteStripe();

        // Si algo falla despues de esta linea, la transaccion entera se deshace y el pedido
        // no se queda guardado a medias
        Pedido pedido = pedidoService.crearPedidoDesdeCesta(email);

        comprobarQueHayClaves(pedido);

        SessionCreateParams.Builder params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCustomerEmail(email)
                // Es lo que nos permite saber, al volver, de que pedido es esta sesion
                .setClientReferenceId(String.valueOf(pedido.getId()))
                // Stripe cambia {CHECKOUT_SESSION_ID} por el id real de la sesion al volver
                .setSuccessUrl(urlBase + "/tienda/pago/exito?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(urlBase + "/tienda/pago/cancelado");

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
            throw new PagoNoCompletadoException("No se ha podido conectar con Stripe");
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
            throw new PagoNoCompletadoException("No se ha podido comprobar el pago con Stripe");
        }

        if (!"paid".equals(sesion.getPaymentStatus())) {
            throw new PagoNoCompletadoException("El pago no se ha completado");
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
            throw new PagoNoCompletadoException("Este pedido ya no se puede pagar");
        }

        asignarClaves(pedido);
        pedido.setEstado(EstadoPedido.PAGADO);
        pedido.setMetodoPago("STRIPE");
        crearFactura(pedido);
        cestaService.vaciarCesta(email);

        return pedidoDao.save(pedido);
    }

    private StripeClient clienteStripe() {
        if (claveSecreta == null || claveSecreta.isBlank()) {
            throw new PagoNoCompletadoException("Falta configurar la clave de Stripe (STRIPE_SECRET_KEY)");
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
}
