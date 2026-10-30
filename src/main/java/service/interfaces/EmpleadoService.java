package service.interfaces;

import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Empleado;

public interface EmpleadoService {
    List<Empleado> listarEmpleados();
    Empleado buscarEmpleadoXId(Long id);
    Empleado guardarEmpleado(Empleado empleado,Long departamentoId,String password);
    void cambiarActivo(Long id, boolean activo);
    List<Departamento> listarDepartamentos();
}
