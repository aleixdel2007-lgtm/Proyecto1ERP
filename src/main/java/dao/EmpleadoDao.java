package dao;

import java.util.List;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmpleadoDao extends JpaRepository<Empleado, Long> {

    // Lista los empleados con su departamento, saltandose cada filtro que llegue a null
    @Query("select e from Empleado e join fetch e.departamento "
            + "where (:texto is null "
            + "       or lower(e.nombre) like lower(concat('%', :texto, '%')) "
            + "       or lower(e.apellidos) like lower(concat('%', :texto, '%')) "
            + "       or lower(e.email) like lower(concat('%', :texto, '%'))) "
            + "and (:departamentoId is null or e.departamento.id = :departamentoId) "
            + "and (:rol is null or e.rol = :rol) "
            + "order by e.nombre, e.apellidos")
    List<Empleado> listarFiltrado(@Param("texto") String texto,
                                  @Param("departamentoId") Long departamentoId,
                                  @Param("rol") Rol rol);

    boolean existsByNifHash(String nifHash);

    boolean existsByNifHashAndIdNot(String nifHash, Long id);
}
