package dao;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import model.aprovisionamiento.Compra;
import model.aprovisionamiento.EstadoCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompraClavesDao extends JpaRepository<Compra, Long> {

    // El listado del panel: proveedor y estado se saltan solos cuando llegan a null, y las
    // fechas llegan siempre con valor, como en PedidoDao. Las dos con <=, que fecha es un
    // LocalDate. Las lineas van con fetch porque la tabla las cuenta y suma unidades; el
    // videojuego de cada una no, que ahi no se ensena. El distinct lo exige ese fetch.
    @Query("select distinct c from Compra c "
            + "join fetch c.proveedor p "
            + "left join fetch c.lineas "
            + "where (:proveedorId is null or p.id = :proveedorId) "
            + "and (:estado is null or c.estado = :estado) "
            + "and c.fecha >= :desde and c.fecha <= :hasta "
            + "order by c.fecha desc, c.numero desc")
    List<Compra> listarFiltrado(@Param("proveedorId") Long proveedorId,
            @Param("estado") EstadoCompra estado,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    // Una orden con todo lo que pintan la ficha y la recepcion ya cargado: el proveedor (nombre
    // y correo de contacto), el empleado que la firmo y las lineas con su videojuego. No vale un
    // findById: con open-in-view desactivado, los tres llegarian como proxys y la transaccion ya
    // estaria cerrada cuando la plantilla los lee.
    // El empleado va con join fetch y no left: la columna es NOT NULL, una orden sin firmar no existe.
    @Query("select distinct c from Compra c "
            + "join fetch c.proveedor "
            + "join fetch c.empleado "
            + "left join fetch c.lineas l "
            + "left join fetch l.videojuego "
            + "where c.id = :id")
    Optional<Compra> buscarConDetalle(@Param("id") Long id);

    // Los cuatro contadores de la cabecera, uno por estado
    long countByEstado(EstadoCompra estado);

    // El numero mas alto que se ha dado este ano, para que el siguiente sea ese mas uno. El
    // prefijo que llega es "OC-2026-", asi que el like no se come los de otros anos y el max
    // sobre el texto vale: los cuatro digitos van rellenos de ceros, asi que el orden
    // alfabetico y el numerico son el mismo. Se hace asi y NO con un count() de la tabla,
    // que es el fallo que arrastran PedidoServiceImpl y PagoServiceImpl: dos altas a la vez
    // sacan el mismo count y la segunda muere contra el UNIQUE del numero.
    @Query("select max(c.numero) from Compra c where c.numero like concat(:prefijo, '%')")
    Optional<String> buscarUltimoNumeroDelAno(@Param("prefijo") String prefijo);
}
