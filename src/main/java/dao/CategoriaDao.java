package dao;

import java.util.List;
import model.catalogo.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoriaDao extends JpaRepository<Categoria, Long> {

    @Query("select c from Categoria c "
            + "where (:texto is null "
            + "       or lower(c.nombre) like lower(concat(:texto, '%')) "
            + "       or lower(c.codigo) like lower(concat(:texto, '%'))) "
            + "order by c.nombre")
    List<Categoria> listarFiltrado(@Param("texto") String texto);

    boolean existsByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);
}
