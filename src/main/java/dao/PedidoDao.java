package dao;

import model.ventas.EstadoPedido;
import model.ventas.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PedidoDao extends JpaRepository<Pedido, Long> {

    // Lista los pedidos saltandose cada filtro que llegue a null. Las fechas llegan siempre
    // con valor (el service pone un limite abierto) para no depender del tipo de un null.
    // Se traen cliente y lineas de golpe: la vista las lee y open-in-view esta desactivado
    @Query("select distinct p from Pedido p "
            + "join fetch p.cliente c "
            + "left join fetch p.lineas "
            + "where (:texto is null "
            + "       or lower(p.numero) like lower(concat(:texto, '%')) "
            + "       or lower(c.nombre) like lower(concat(:texto, '%')) "
            + "       or lower(c.apellidos) like lower(concat(:texto, '%')) "
            + "       or lower(c.email) like lower(concat(:texto, '%'))) "
            + "and (:estado is null or p.estado = :estado) "
            + "and p.fecha >= :desde and p.fecha < :hasta "
            + "order by p.fecha desc")
    List<Pedido> listarFiltrado(@Param("texto") String texto,
                                @Param("estado") EstadoPedido estado,
                                @Param("desde") LocalDateTime desde,
                                @Param("hasta") LocalDateTime hasta);

    // Un pedido con su cliente y sus lineas (con el videojuego de cada una) ya cargados.
    // El proveedor y el colaborador del juego tambien se traen aqui: la vista ensena quien
    // es el estudio, y con open-in-view desactivado un ManyToOne LAZY sin fetch revienta al
    // pintarlo
    @Query("select distinct p from Pedido p "
            + "join fetch p.cliente "
            + "left join fetch p.lineas l "
            + "left join fetch l.videojuego v "
            + "left join fetch v.proveedor "
            + "left join fetch v.colaborador "
            + "where p.id = :id")
    Optional<Pedido> buscarConDetalle(@Param("id") Long id);

    long countByEstado(EstadoPedido estado);
}
