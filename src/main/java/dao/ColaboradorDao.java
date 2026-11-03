package dao;

import java.util.List;
import model.seguridad.Colaborador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ColaboradorDao extends JpaRepository<Colaborador, Long> {

    @Query("select c from Colaborador c "
            + "where (:texto is null "
            + "       or lower(c.nombre) like lower(concat(:texto, '%')) "
            + "       or lower(c.apellidos) like lower(concat(:texto, '%')) "
            + "       or lower(c.email) like lower(concat(:texto, '%'))) "
            + "and (:activo is null or c.activo = :activo) "
            + "order by c.nombre")
    List<Colaborador> listarFiltrado(@Param("texto") String texto,
                                     @Param("activo") Boolean activo);
}
