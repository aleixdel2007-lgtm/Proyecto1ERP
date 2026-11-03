package service.interfaces;

import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import model.seguridad.Rol;

public interface EmpleadoService {
    List<Empleado> listarEmpleados(String texto, Long departamentoId, Rol rol);
    Empleado buscarEmpleadoXId(Long id);
    Empleado guardarEmpleado(Empleado empleado,Long departamentoId,String password);
    void eliminarEmpleado(Long id);
    List<Departamento> listarDepartamentos();
}
