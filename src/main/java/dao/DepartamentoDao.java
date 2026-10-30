package dao;

import java.util.List;
import model.seguridad.Departamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartamentoDao extends JpaRepository<Departamento, Long> {

    @Query("select d from Departamento d left join fetch d.responsable left join fetch d.trabajadores order by d.nombre")
    List<Departamento> listarConDetalle();

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByResponsableId(Long responsableId);

    boolean existsByResponsableIdAndIdNot(Long responsableId, Long id);
}
