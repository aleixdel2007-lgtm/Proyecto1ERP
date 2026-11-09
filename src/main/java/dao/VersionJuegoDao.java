package dao;

import java.util.List;
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
}
