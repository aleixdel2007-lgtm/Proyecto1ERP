package dao;

import model.ventas.Factura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FacturasDAO extends JpaRepository<Factura, Long> {

    // Factura no tiene cliente, lineas, estado ni importes: viven en el pedido, asi que se
    // traen con join fetch (la vista los lee y open-in-view esta desactivado). El importe
    // de la factura es p.total, y las lineas no hacen falta para mostrarlo
    @Query("select f from Factura f "
            + "join fetch f.pedido p "
            + "join fetch p.cliente c "
            + "where (:texto is null "
            + "       or lower(f.numero) like lower(concat('%', :texto)) "
            + "       or lower(p.numero) like lower(concat('%', :texto))) "
            + "order by f.fechaEmision desc, f.numero desc")
    List<Factura> listarFiltrado(@Param("texto") String texto);

    // Una factura con su pedido, el cliente y las lineas (con su videojuego) para el PDF
    @Query("select f from Factura f "
            + "join fetch f.pedido p "
            + "join fetch p.cliente "
            + "left join fetch p.lineas l "
            + "left join fetch l.videojuego "
            + "where f.id = :id")
    Optional<Factura> buscarConDetalle(@Param("id") Long id);
}
