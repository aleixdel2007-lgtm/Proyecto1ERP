package dao;

import java.util.List;
import java.util.Optional;
import model.publicacion.VersionJuego;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VersionJuegoDao extends JpaRepository<VersionJuego, Long> {

    // La version activa de cada juego de la lista (solo puede haber una por juego). El
    // join fetch trae el juego para poder saber de cual es sin otra consulta
    @Query("select v from VersionJuego v join fetch v.videojuego "
            + "where v.videojuego.id in :ids and v.activa = true")
    List<VersionJuego> buscarActivas(@Param("ids") List<Long> ids);

    // La version activa de un solo juego, que es la que se sirve al descargar: de ella
    // salen la ruta del ZIP en disco y el nombre con el que se le entrega al cliente
    Optional<VersionJuego> findByVideojuegoIdAndActivaTrue(Long videojuegoId);

    boolean existsByVideojuegoIdAndNumero(Long videojuegoId, String numero);
}
