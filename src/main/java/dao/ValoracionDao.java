package dao;

import java.util.List;
import model.valoracion.EstadoValoracion;
import model.valoracion.Valoracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ValoracionDao extends JpaRepository<Valoracion, Long> {

    // Solo cuentan las valoraciones ACTIVAS cuyo cliente sigue activo
    // join fetch: la vista lee el nombre del cliente y open-in-view esta desactivado,
    // asi que el cliente tiene que venir ya cargado
    @Query("select v from Valoracion v join fetch v.cliente c "
            + "where v.videojuego.id = :videojuegoId "
            + "and v.estado = :estado and c.activo = true")
    List<Valoracion> listarVisiblesPorVideojuego(@Param("videojuegoId") Long videojuegoId,
            @Param("estado") EstadoValoracion estado);

    List<Valoracion> findByClienteId(Long clienteId);

    boolean existsByClienteEmailAndVideojuegoId(String email, Long videojuegoId);
}
