package service.implement;

import config.AlmacenArchivos;
import config.IndiceCiego;
import dao.DepartamentoDao;
import dao.EmpleadoDao;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import exception.email.EmailDuplicadoException;
import exception.empleado.EmpleadoConReferenciasException;
import exception.empleado.EmpleadoNoEncontradoException;
import exception.nif.NifDuplicadoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import model.seguridad.ImagenUsuario;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
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
    private ImagenUsuarioDao imagenUsuarioDao;

    @Autowired
    private AlmacenArchivos almacenArchivos;

    @Autowired
    private PasswordEncoder codificador;

    @Override
    public List<Empleado> listarEmpleados(String texto, Long departamentoId, Rol rol) {
        return empleadoDao.listarFiltrado(normalizarTexto(texto), departamentoId, rol);
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

        if (rol == null || rol == Rol.CLIENTE || rol == Rol.PROVEEDOR || rol == Rol.COLABORADOR) {
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

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
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

        eliminarImagenPerfil(id);

        try {
            empleadoDao.delete(empleado);

        } catch (DataIntegrityViolationException e) {
            throw new EmpleadoConReferenciasException();
        }
    }

    @Override
    public ImagenUsuario buscarImagenPerfil(Long empleadoId) {
        return imagenUsuarioDao.findById(empleadoId).orElse(null);
    }

    @Override
    public Map<Long, String> listarImagenesPerfil(List<Empleado> empleados) {

        Map<Long, String> fotos = new HashMap<>();

        if (empleados == null || empleados.isEmpty()) {
            return fotos;
        }

        List<Long> ids = new ArrayList<>();

        for (Empleado empleado : empleados) {
            ids.add(empleado.getId());
        }

        for (Object[] fila : imagenUsuarioDao.buscarPorUsuarios(ids)) {
            fotos.put((Long) fila[0], (String) fila[1]);
        }
        return fotos;
    }

    @Override
    public void guardarImagenPerfil(Long empleadoId, MultipartFile archivo) {

        Empleado empleado = buscarEmpleadoXId(empleadoId);

        String ruta = almacenArchivos.guardarImagenPerfil(empleadoId, archivo);

        ImagenUsuario imagen = imagenUsuarioDao.findById(empleadoId).orElseGet(() -> {
            ImagenUsuario nueva = new ImagenUsuario();
            nueva.setUsuario(empleado);
            return nueva;
        });

        String rutaAnterior = imagen.getRutaImagen();

        imagen.setRutaImagen(ruta);
        imagen.setTextoAlternativo("Foto de perfil de " + empleado.getNombre());

        try {
            imagenUsuarioDao.save(imagen);

        } catch (RuntimeException e) {
            almacenArchivos.borrar(ruta);
            throw e;
        }

        if (rutaAnterior != null) {
            almacenArchivos.borrar(rutaAnterior);
        }
    }

    private void eliminarImagenPerfil(Long empleadoId) {

        ImagenUsuario imagen = imagenUsuarioDao.findById(empleadoId).orElse(null);

        if (imagen == null) {
            return;
        }

        String ruta = imagen.getRutaImagen();

        imagenUsuarioDao.delete(imagen);
        imagenUsuarioDao.flush();

        almacenArchivos.borrar(ruta);
    }
}
