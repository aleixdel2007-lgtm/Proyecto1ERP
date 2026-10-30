package service.interfaces;

import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Trabajador;

public interface DepartamentoService {

    List<Departamento> listarDepartamentos();

    Departamento buscarDepartamentoXId(Long id);

    Departamento guardarDepartamento(Departamento departamento, Long responsableId);

    void eliminarDepartamento(Long id);

    List<Trabajador> listarTrabajadores();
}
