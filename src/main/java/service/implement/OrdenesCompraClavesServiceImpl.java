package service.implement;

import dao.CompraClavesDao;
import dao.EmpleadoDao;
import dao.ProveedorDao;
import dao.VideojuegoDao;
import exception.compra.CompraNoEncontradaException;
import exception.compra.EstadoCompraInvalidoException;
import exception.compra.LineaCompraInvalidaException;
import exception.usuario.UsuarioNoEncontradoException;
import exception.videojuego.VideojuegoNoEncontradoException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import model.aprovisionamiento.Compra;
import model.aprovisionamiento.EstadoCompra;
import model.aprovisionamiento.LineaCompra;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Videojuego;
import model.seguridad.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import service.interfaces.ClaveLicenciaService;
import service.interfaces.OrdenesCompraClavesService;
import service.interfaces.PdfService;

@Service
@Transactional(readOnly = true)
public class OrdenesCompraClavesServiceImpl implements OrdenesCompraClavesService {

    // Las fechas del filtro llegan vacias casi siempre y la consulta las quiere con valor:
    // estos dos son el limite abierto, igual que en PedidoServiceImpl
    private static final LocalDate SIEMPRE_DESDE = LocalDate.of(2000, 1, 1);
    private static final LocalDate SIEMPRE_HASTA = LocalDate.of(2999, 1, 1);

    @Autowired
    private CompraClavesDao compraClavesDao;

    @Autowired
    private ProveedorDao proveedorDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private EmpleadoDao empleadoDao;

    @Autowired
    private ClaveLicenciaService claveLicenciaService;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private PdfService pdfService;

    @Override
    public List<Compra> listarCompras(Long proveedorId, EstadoCompra estado,
                                      LocalDate desde, LocalDate hasta) {

        return compraClavesDao.listarFiltrado(proveedorId, estado,
                desde == null ? SIEMPRE_DESDE : desde,
                hasta == null ? SIEMPRE_HASTA : hasta);
    }

    @Override
    public long contarPorEstado(EstadoCompra estado) {
        return compraClavesDao.countByEstado(estado);
    }

    @Override
    public Compra buscarCompraXId(Long id) {

        Compra compra = compraClavesDao.buscarConDetalle(id).orElse(null);

        if (compra == null) {
            throw new CompraNoEncontradaException(id);
        }
        return compra;
    }

    @Override
    public List<Proveedor> listarProveedoresActivos() {
        return proveedorDao.listarFiltrado(null, null, true);
    }

    // Los juegos de un colaborador no se le compran a nadie, asi que el desplegable de las
    // lineas pide el listado del panel con soloColaborador en false
    @Override
    public List<Videojuego> listarVideojuegosPropios(Long proveedorId) {
        return videojuegoDao.listarFiltrado(null, null, null, null, null, null, proveedorId, false);
    }

    @Override
    @Transactional
    public Compra guardarCompra(Compra compra, Long proveedorId, String emailEmpleado) {

        if (compra.getFecha() == null) {
            throw new IllegalArgumentException("compras.error.fechaObligatoria");
        }

        if (compra.getId() == null) {
            return crearCompra(compra, proveedorId, emailEmpleado);
        }
        return actualizarCompra(compra, proveedorId);
    }

    private Compra crearCompra(Compra compra, Long proveedorId, String emailEmpleado) {

        Compra nueva = new Compra();
        nueva.setProveedor(buscarProveedor(proveedorId));
        nueva.setEmpleado(buscarEmpleado(emailEmpleado));
        nueva.setFecha(compra.getFecha());
        nueva.setObservaciones(normalizarTexto(compra.getObservaciones()));
        nueva.setEstado(EstadoCompra.BORRADOR);
        nueva.setNumero(siguienteNumero(compra.getFecha()));
        nueva.setTotalCoste(BigDecimal.ZERO);

        return compraClavesDao.save(nueva);
    }

    // El numero y el empleado que firma no se vuelven a tocar: el numero ya se le ha dado al
    // proveedor, y quien creo la orden es quien la creo
    private Compra actualizarCompra(Compra compra, Long proveedorId) {

        Compra existente = buscarCompraXId(compra.getId());

        if (existente.getEstado() != EstadoCompra.BORRADOR) {
            throw new EstadoCompraInvalidoException("compras.accion.editar", existente.getEstado());
        }

        existente.setProveedor(buscarProveedor(proveedorId));
        existente.setFecha(compra.getFecha());
        existente.setObservaciones(normalizarTexto(compra.getObservaciones()));

        return compraClavesDao.save(existente);
    }

    @Override
    @Transactional
    public void anadirLinea(Long compraId, Long videojuegoId, Integer cantidad,
                            BigDecimal costeUnitario) {

        Compra compra = buscarCompraXId(compraId);
        exigirLineasEditables(compra);

        if (cantidad != null && cantidad < 1) {
            throw new LineaCompraInvalidaException(
                    "La cantidad tiene que ser 1 o mas, o quedarse vacia para ilimitada");
        }

        if (costeUnitario == null || costeUnitario.signum() < 0) {
            throw new LineaCompraInvalidaException("compras.error.costeNegativo");
        }

        Videojuego videojuego = videojuegoDao.findById(videojuegoId).orElse(null);

        if (videojuego == null) {
            throw new VideojuegoNoEncontradoException(videojuegoId);
        }

        if (videojuego.getColaborador() != null) {
            throw new LineaCompraInvalidaException(
                    "Los juegos de un colaborador no se le compran a nadie");
        }

        if (videojuego.getProveedor() == null
                || !videojuego.getProveedor().getId().equals(compra.getProveedor().getId())) {
            throw new LineaCompraInvalidaException("compras.error.juegoDeOtroProveedor");
        }

        if (buscarLineaXVideojuego(compra, videojuegoId) != null) {
            throw new LineaCompraInvalidaException("compras.error.lineaDuplicada");
        }

        LineaCompra linea = new LineaCompra();
        linea.setCompra(compra);
        linea.setVideojuego(videojuego);
        linea.setCantidad(cantidad);
        linea.setCosteUnitario(costeUnitario);
        compra.getLineas().add(linea);

        recalcularTotal(compra);
        compraClavesDao.save(compra);
    }

    @Override
    @Transactional
    public void quitarLinea(Long compraId, Long lineaId) {

        Compra compra = buscarCompraXId(compraId);
        exigirLineasEditables(compra);

        LineaCompra linea = buscarLineaXId(compra, lineaId);

        if (linea.getCantidadRecibida() != null) {
            throw new LineaCompraInvalidaException("compras.error.lineaRecibida");
        }

        compra.getLineas().remove(linea);

        recalcularTotal(compra);
        compraClavesDao.save(compra);
    }

    @Override
    @Transactional
    public void confirmarCompra(Long id) {

        Compra compra = buscarCompraXId(id);

        if (compra.getEstado() != EstadoCompra.BORRADOR) {
            throw new EstadoCompraInvalidoException("compras.accion.confirmar", compra.getEstado());
        }

        if (compra.getLineas().isEmpty()) {
            throw new LineaCompraInvalidaException(
                    "Una orden sin lineas no se puede confirmar: no habria nada que recibir");
        }

        compra.setEstado(EstadoCompra.CONFIRMADA);
        compraClavesDao.save(compra);
    }

    @Override
    @Transactional
    public void cancelarCompra(Long id) {

        Compra compra = buscarCompraXId(id);

        if (compra.getEstado() != EstadoCompra.BORRADOR
                && compra.getEstado() != EstadoCompra.CONFIRMADA) {
            throw new EstadoCompraInvalidoException("compras.accion.cancelar", compra.getEstado());
        }

        compra.setEstado(EstadoCompra.CANCELADA);
        compraClavesDao.save(compra);
    }

    // Las dos listas llegan en paralelo desde el formulario, una pareja por fila. Primero se
    // anota TODO lo recibido y se cierra la orden, y solo despues se acunan las claves: si algo
    // de lo que llega no cuadra, la transaccion se deshace y no queda ni media clave suelta
    @Override
    @Transactional
    public int recibirCompra(Long id, List<Long> lineaIds, List<Integer> recibidas,
                             String numeroAlbaran, String observaciones) {

        Compra compra = buscarCompraXId(id);

        if (compra.getEstado() != EstadoCompra.CONFIRMADA) {
            throw new EstadoCompraInvalidoException("compras.accion.recibir", compra.getEstado());
        }

        if (lineaIds == null || recibidas == null
                || lineaIds.size() != recibidas.size()
                || lineaIds.size() != compra.getLineas().size()) {
            throw new LineaCompraInvalidaException(
                    "Lo recibido no cuadra con las lineas de la orden");
        }

        int total = 0;

        for (int i = 0; i < lineaIds.size(); i++) {

            LineaCompra linea = buscarLineaXId(compra, lineaIds.get(i));
            Integer recibida = recibidas.get(i);

            if (recibida == null || recibida < 0) {
                throw new LineaCompraInvalidaException(
                        "Las unidades recibidas no pueden ser negativas");
            }

            // Dos parejas con el mismo id dejarian otra linea sin anotar, y esa llegaria
            // al acunado con las unidades a null
            if (linea.getCantidadRecibida() != null) {
                throw new LineaCompraInvalidaException(
                        "Esa linea viene dos veces en el formulario");
            }

            linea.setCantidadRecibida(recibida);
            total += recibida;
        }

        if (total == 0) {
            throw new LineaCompraInvalidaException(
                    "Si no ha llegado ninguna unidad, la orden se cancela en vez de recibirse");
        }

        compra.setFechaRecepcion(LocalDate.now());
        compra.setNumeroAlbaran(normalizarTexto(numeroAlbaran));
        compra.setObservaciones(normalizarTexto(observaciones));
        compra.setEstado(EstadoCompra.RECIBIDA);
        compraClavesDao.save(compra);

        int acunadas = 0;

        for (LineaCompra linea : compra.getLineas()) {
            acunadas += claveLicenciaService.acunarClaves(linea, linea.getCantidadRecibida());
        }
        return acunadas;
    }

    // Exige la orden RECIBIDA: antes de eso no hay ni albaran, ni fecha, ni cantidades recibidas
    @Override
    public byte[] generarAlbaranPdf(Long id, Locale locale) {

        Compra compra = buscarCompraXId(id);

        if (compra.getEstado() != EstadoCompra.RECIBIDA) {
            throw new EstadoCompraInvalidoException("compras.accion.albaran", compra.getEstado());
        }

        Context contexto = new Context(locale);
        contexto.setVariable("compra", compra);

        return pdfService.desdeHtml(templateEngine.process("compras/albaranPdf", contexto));
    }

    // Las lineas se tocan mientras el proveedor todavia puede cambiar el pedido. Una RECIBIDA
    // no, que de ella han salido claves, y una CANCELADA tampoco
    private void exigirLineasEditables(Compra compra) {

        if (compra.getEstado() != EstadoCompra.BORRADOR
                && compra.getEstado() != EstadoCompra.CONFIRMADA) {
            throw new EstadoCompraInvalidoException("compras.accion.tocarLineas", compra.getEstado());
        }
    }

    // Una linea ilimitada no tiene unidades que multiplicar, asi que no suma coste: lo que se
    // sabe que se va a pagar es lo de las lineas con cantidad
    private void recalcularTotal(Compra compra) {

        BigDecimal total = BigDecimal.ZERO;

        for (LineaCompra linea : compra.getLineas()) {
            if (linea.getCantidad() != null) {
                total = total.add(linea.getCosteUnitario()
                        .multiply(new BigDecimal(linea.getCantidad())));
            }
        }
        compra.setTotalCoste(total);
    }

    private String siguienteNumero(LocalDate fecha) {

        String prefijo = "OC-" + fecha.getYear() + "-";
        String ultimo = compraClavesDao.buscarUltimoNumeroDelAno(prefijo).orElse(null);

        int siguiente = 1;

        if (ultimo != null) {
            siguiente = Integer.parseInt(ultimo.substring(prefijo.length())) + 1;
        }
        return prefijo + String.format("%04d", siguiente);
    }

    private Proveedor buscarProveedor(Long proveedorId) {

        if (proveedorId == null) {
            throw new IllegalArgumentException("compras.error.proveedorObligatorio");
        }

        Proveedor proveedor = proveedorDao.findById(proveedorId).orElse(null);

        if (proveedor == null) {
            throw new IllegalArgumentException("compras.error.proveedorNoExiste");
        }
        return proveedor;
    }

    private Empleado buscarEmpleado(String email) {

        Empleado empleado = empleadoDao.findByEmail(email).orElse(null);

        if (empleado == null) {
            throw new UsuarioNoEncontradoException(email);
        }
        return empleado;
    }

    // Solo entre las lineas de ESTA orden: asi nadie toca la de otra escribiendo el id a mano
    private LineaCompra buscarLineaXId(Compra compra, Long lineaId) {

        for (LineaCompra linea : compra.getLineas()) {
            if (linea.getId().equals(lineaId)) {
                return linea;
            }
        }
        throw new LineaCompraInvalidaException("compras.error.lineaAjena");
    }

    private LineaCompra buscarLineaXVideojuego(Compra compra, Long videojuegoId) {

        for (LineaCompra linea : compra.getLineas()) {
            if (linea.getVideojuego().getId().equals(videojuegoId)) {
                return linea;
            }
        }
        return null;
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }
}
