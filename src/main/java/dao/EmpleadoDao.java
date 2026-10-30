package dao;

import java.util.List;
import model.seguridad.Empleado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmpleadoDao extends JpaRepository<Empleado, Long> {

    @Query("select t from Empleado t join fetch t.departamento order by t.nombre, t.apellidos")
    List<Empleado> listarConDepartamento();

    boolean existsByNifHash(String nifHash);

    boolean existsByNifHashAndIdNot(String nifHash, Long id);
}
