package service.implement;

import config.IndiceCiego;
import dao.DepartamentoDao;
import dao.EmpleadoDao;
import dao.UsuarioDao;
import exception.EmailDuplicadoException;
import exception.EmpleadoConReferenciasException;
import exception.EmpleadoNoEncontradoException;
import exception.NifDuplicadoException;
import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import service.interfaces.EmpleadoService;

@Service
public class EmpleadoServiceImpl implements EmpleadoService {

    @Autowired
    private EmpleadoDao empleadoDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private DepartamentoDao departamentoDao;

    @Autowired
    private PasswordEncoder codificador;

    @Override
    public List<Empleado> listarEmpleados() {
        return empleadoDao.listarConDepartamento();
    }

    @Override
    public List<Departamento> listarDepartamentos() {
        return departamentoDao.findAll();
    }

    @Override
    public Empleado buscarEmpleadoXId(Long id) {

        Empleado empleado = empleadoDao.findById(id).orElse(null);

        if (empleado == null) {
            throw new EmpleadoNoEncontradoException(id);
        }
        return empleado;
    }

    @Override
    public Empleado guardarEmpleado(Empleado empleado, Long departamentoId, String password) {

        validarRol(empleado);
        validarEmailRepetido(empleado);
        validarNifRepetido(empleado);

        if (empleado.getId() == null) {
            return crearEmpleado(empleado, departamentoId, password);
        }
        return actualizarEmpleado(empleado, departamentoId, password);
    }

    private Empleado crearEmpleado(Empleado empleado, Long departamentoId, String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena inicial es obligatoria");
        }

        empleado.setPasswordHash(codificador.encode(password));
        empleado.setDebeCambiarPassword(true);
        empleado.setDepartamento(buscarDepartamento(departamentoId));
        empleado.setNifHash(hashDelNif(empleado.getNif()));

        return empleadoDao.save(empleado);
    }

    private Empleado actualizarEmpleado(Empleado empleado, Long departamentoId, String password) {

        Empleado existente = buscarEmpleadoXId(empleado.getId());

        existente.setNombre(empleado.getNombre());
        existente.setApellidos(empleado.getApellidos());
        existente.setEmail(empleado.getEmail());
        existente.setRol(empleado.getRol());
        existente.setCargo(empleado.getCargo());
        existente.setNif(empleado.getNif());
        existente.setNifHash(hashDelNif(empleado.getNif()));
        existente.setIban(empleado.getIban());
        existente.setFechaContratacion(empleado.getFechaContratacion());
        existente.setActivo(empleado.isActivo());
        existente.setDepartamento(buscarDepartamento(departamentoId));

        if (password != null && !password.isBlank()) {
            existente.setPasswordHash(codificador.encode(password));
            existente.setDebeCambiarPassword(true);
        }

        return empleadoDao.save(existente);
    }

    private void validarRol(Empleado empleado) {

        Rol rol = empleado.getRol();

        if (rol == null || rol == Rol.CLIENTE || rol == Rol.PROVEEDOR) {
            throw new IllegalArgumentException("Ese rol no es de empleado");
        }
    }

    private void validarEmailRepetido(Empleado empleado) {

        boolean repetido;

        if (empleado.getId() == null) {
            repetido = usuarioDao.existsByEmail(empleado.getEmail());
        } else {
            repetido = usuarioDao.existsByEmailAndIdNot(empleado.getEmail(), empleado.getId());
        }

        if (repetido) {
            throw new EmailDuplicadoException();
        }
    }

    private void validarNifRepetido(Empleado empleado) {

        String hash = hashDelNif(empleado.getNif());

        if (hash == null) {
            return;
        }

        boolean repetido;

        if (empleado.getId() == null) {
            repetido = empleadoDao.existsByNifHash(hash);
        } else {
            repetido = empleadoDao.existsByNifHashAndIdNot(hash, empleado.getId());
        }

        if (repetido) {
            throw new NifDuplicadoException();
        }
    }

    private String hashDelNif(String nif) {

        if (nif == null || nif.isBlank()) {
            return null;
        }
        return IndiceCiego.hash(nif);
    }

    private Departamento buscarDepartamento(Long departamentoId) {

        if (departamentoId == null) {
            throw new IllegalArgumentException("El departamento es obligatorio");
        }

        Departamento departamento = departamentoDao.findById(departamentoId).orElse(null);

        if (departamento == null) {
            throw new IllegalArgumentException("El departamento no existe");
        }
        return departamento;
    }

    @Override
    public void eliminarEmpleado(Long id) {

        Empleado empleado = buscarEmpleadoXId(id);

        try {
            empleadoDao.delete(empleado);

        } catch (DataIntegrityViolationException e) {
            throw new EmpleadoConReferenciasException();
        }
    }
}
