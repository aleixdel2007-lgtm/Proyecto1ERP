package service.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import model.aprovisionamiento.Compra;
import model.aprovisionamiento.EstadoCompra;
import model.aprovisionamiento.Proveedor;
import model.catalogo.Videojuego;

public interface OrdenesCompraClavesService {

    // El listado del panel, saltandose cada filtro que llegue vacio
    List<Compra> listarCompras(Long proveedorId, EstadoCompra estado,
                               LocalDate desde, LocalDate hasta);

    // Cuantas ordenes hay en un estado, para los contadores de la cabecera
    long contarPorEstado(EstadoCompra estado);

    // Una orden con su proveedor, su empleado y sus lineas ya cargados
    Compra buscarCompraXId(Long id);

    // Los proveedores del desplegable de la cabecera
    List<Proveedor> listarProveedoresActivos();

    // Los juegos del desplegable de las lineas: los nuestros, que son los que se compran
    List<Videojuego> listarVideojuegosPropios(Long proveedorId);

    // Crea el borrador con su numero, o guarda la cabecera de uno que ya existe
    Compra guardarCompra(Compra compra, Long proveedorId, String emailEmpleado);

    // Una linea mas. Cantidad nula es ILIMITADA
    void anadirLinea(Long compraId, Long videojuegoId, Integer cantidad, BigDecimal costeUnitario);

    void quitarLinea(Long compraId, Long lineaId);

    void confirmarCompra(Long id);

    void cancelarCompra(Long id);

    // Cierra la orden con lo que ha llegado y acuna las claves. Devuelve cuantas ha creado
    int recibirCompra(Long id, List<Long> lineaIds, List<Integer> recibidas,
                      String numeroAlbaran, String observaciones);

    // El albaran de recepcion en PDF. Solo tiene sentido sobre una orden RECIBIDA
    byte[] generarAlbaranPdf(Long id, Locale locale);
}
