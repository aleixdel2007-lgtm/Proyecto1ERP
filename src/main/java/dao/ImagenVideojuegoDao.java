package dao;

import java.util.List;
import model.catalogo.ImagenVideojuego;
import model.catalogo.TipoImagen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImagenVideojuegoDao extends JpaRepository<ImagenVideojuego, Long> {

    List<ImagenVideojuego> findByVideojuegoIdOrderByOrdenAscIdAsc(Long videojuegoId);

    List<ImagenVideojuego> findByVideojuegoIdAndTipoOrderByOrdenAscIdAsc(Long videojuegoId, TipoImagen tipo);

    @Query("select coalesce(max(i.orden), 0) from ImagenVideojuego i "
            + "where i.videojuego.id = :videojuegoId and i.tipo = :tipo")
    int maximoOrden(@Param("videojuegoId") Long videojuegoId, @Param("tipo") TipoImagen tipo);

    @Query("select i.videojuego.id, i.ruta from ImagenVideojuego i "
            + "where i.tipo = :tipo and i.videojuego.id in :ids "
            + "order by i.orden, i.id")
    List<Object[]> buscarPorTipo(@Param("ids") List<Long> ids, @Param("tipo") TipoImagen tipo);
}
