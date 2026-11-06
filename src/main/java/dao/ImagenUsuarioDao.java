package dao;

import java.util.List;
import model.seguridad.ImagenUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ImagenUsuarioDao extends JpaRepository<ImagenUsuario, Long> {

    @Query("select i.idUsuario, i.rutaImagen from ImagenUsuario i "
            + "where i.idUsuario in :ids")
    List<Object[]> buscarPorUsuarios(@Param("ids") List<Long> ids);
}
