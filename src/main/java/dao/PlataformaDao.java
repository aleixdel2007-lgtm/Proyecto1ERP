package dao;

import java.util.List;
import model.catalogo.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlataformaDao extends JpaRepository<Plataforma, Long> {

    @Query("select p from Plataforma p "
            + "where (:texto is null "
            + "       or lower(p.nombre) like lower(concat(:texto, '%')) "
            + "       or lower(p.codigo) like lower(concat(:texto, '%'))) "
            + "order by p.nombre")
    List<Plataforma> listarFiltrado(@Param("texto") String texto);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
