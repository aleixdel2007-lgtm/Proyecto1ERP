package service.implement;

import dao.TrabajadorDao;
import exception.DepartamentoConTrabajadoresException;
import exception.DepartamentoNoEncontradoException;
import exception.NombreDepartamentoDuplicadoException;
import exception.ResponsableOcupadoException;
import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Trabajador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import service.interfaces.DepartamentoService;
import dao.DepartamentoDao;

@Service
public class DepartamentoServiceImpl implements DepartamentoService {

    @Autowired
    private DepartamentoDao departamentoDao;

    @Autowired
    private TrabajadorDao trabajadorDao;

    @Override
    public List<Departamento> listarDepartamentos() {
        return departamentoDao.listarConDetalle();
    }

    @Override
    public Departamento buscarDepartamentoXId(Long id) {

        Departamento departamento = departamentoDao.findById(id).orElse(null);

        if (departamento == null) {
            throw new DepartamentoNoEncontradoException(id);
        }
        return departamento;
    }

    @Override
    public Departamento guardarDepartamento(Departamento departamento, Long responsableId) {

        validarNombreRepetido(departamento);
        validarResponsableLibre(departamento, responsableId);
        asignarResponsable(departamento, responsableId);

        return departamentoDao.save(departamento);
    }

    private void validarNombreRepetido(Departamento departamento) {

        boolean repetido;

        if (departamento.getId() == null) {
            repetido = departamentoDao.existsByNombreIgnoreCase(departamento.getNombre());
        } else {
            repetido = departamentoDao.existsByNombreIgnoreCaseAndIdNot(
                    departamento.getNombre(), departamento.getId());
        }

        if (repetido) {
            throw new NombreDepartamentoDuplicadoException();
        }
    }

    private void validarResponsableLibre(Departamento departamento, Long responsableId) {

        if (responsableId == null) {
            return;
        }

        boolean ocupado;

        if (departamento.getId() == null) {
            ocupado = departamentoDao.existsByResponsableId(responsableId);
        } else {
            ocupado = departamentoDao.existsByResponsableIdAndIdNot(responsableId, departamento.getId());
        }

        if (ocupado) {
            throw new ResponsableOcupadoException();
        }
    }

    private void asignarResponsable(Departamento departamento, Long responsableId) {

        if (responsableId != null) {

            Trabajador responsable = trabajadorDao.findById(responsableId).orElse(null);

            if (responsable == null) {
                throw new IllegalArgumentException("El responsable no existe");
            }

            departamento.setResponsable(responsable);

        } else {
            departamento.setResponsable(null);
        }
    }

    @Override
    @Transactional
    public void eliminarDepartamento(Long id) {

        Departamento departamento = buscarDepartamentoXId(id);

        validarTrabajadoresAsociados(departamento);

        departamentoDao.delete(departamento);
    }

    private void validarTrabajadoresAsociados(Departamento departamento) {

        if (!departamento.getTrabajadores().isEmpty()) {
            throw new DepartamentoConTrabajadoresException();
        }
    }

    @Override
    public List<Trabajador> listarTrabajadores() {
        return trabajadorDao.findAll();
    }
}
