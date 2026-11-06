package service.implement;

import config.AlmacenArchivos;
import dao.ColaboradorDao;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import dao.VideojuegoDao;
import exception.colaborador.ColaboradorConJuegosException;
import exception.colaborador.ColaboradorNoEncontradoException;
import exception.email.EmailDuplicadoException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.seguridad.Colaborador;
import model.seguridad.ImagenUsuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import service.interfaces.ColaboradorService;

@Service
public class ColaboradorServiceImpl implements ColaboradorService {

    @Autowired
    private ColaboradorDao colaboradorDao;

    @Autowired
    private VideojuegoDao videojuegoDao;

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private ImagenUsuarioDao imagenUsuarioDao;

    @Autowired
    private AlmacenArchivos almacenArchivos;

    @Autowired
    private PasswordEncoder codificador;

    @Override
    public List<Colaborador> listarColaboradores(String texto, Boolean activo) {
        return colaboradorDao.listarFiltrado(normalizarTexto(texto), activo);
    }

    @Override
    public Colaborador buscarColaboradorXId(Long id) {

        Colaborador colaborador = colaboradorDao.findById(id).orElse(null);

        if (colaborador == null) {
            throw new ColaboradorNoEncontradoException(id);
        }
        return colaborador;
    }

    @Override
    public Colaborador guardarColaborador(Colaborador colaborador, String password) {

        validarEmailRepetido(colaborador);

        if (colaborador.getId() == null) {
            return crearColaborador(colaborador, password);
        }
        return actualizarColaborador(colaborador, password);
    }

    private Colaborador crearColaborador(Colaborador colaborador, String password) {

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contrasena inicial es obligatoria");
        }

        colaborador.setPasswordHash(codificador.encode(password));
        colaborador.setDebeCambiarPassword(true);

        return colaboradorDao.save(colaborador);
    }

    private Colaborador actualizarColaborador(Colaborador colaborador, String password) {

        Colaborador existente = buscarColaboradorXId(colaborador.getId());

        existente.setNombre(colaborador.getNombre());
        existente.setApellidos(colaborador.getApellidos());
        existente.setEmail(colaborador.getEmail());
        existente.setPais(colaborador.getPais());
        existente.setActivo(colaborador.isActivo());

        if (password != null && !password.isBlank()) {
            existente.setPasswordHash(codificador.encode(password));
            existente.setDebeCambiarPassword(true);
        }

        return colaboradorDao.save(existente);
    }

    private void validarEmailRepetido(Colaborador colaborador) {

        boolean repetido;

        if (colaborador.getId() == null) {
            repetido = usuarioDao.existsByEmail(colaborador.getEmail());
        } else {
            repetido = usuarioDao.existsByEmailAndIdNot(colaborador.getEmail(), colaborador.getId());
        }

        if (repetido) {
            throw new EmailDuplicadoException();
        }
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    @Override
    public void eliminarColaborador(Long id) {

        Colaborador colaborador = buscarColaboradorXId(id);

        if (videojuegoDao.countByColaboradorId(id) > 0) {
            throw new ColaboradorConJuegosException();
        }

        eliminarImagenPerfil(id);

        colaboradorDao.delete(colaborador);
    }

    @Override
    public ImagenUsuario buscarImagenPerfil(Long colaboradorId) {
        return imagenUsuarioDao.findById(colaboradorId).orElse(null);
    }

    @Override
    public Map<Long, String> listarImagenesPerfil(List<Colaborador> colaboradores) {

        Map<Long, String> fotos = new HashMap<>();

        if (colaboradores == null || colaboradores.isEmpty()) {
            return fotos;
        }

        List<Long> ids = new ArrayList<>();

        for (Colaborador colaborador : colaboradores) {
            ids.add(colaborador.getId());
        }

        for (Object[] fila : imagenUsuarioDao.buscarPorUsuarios(ids)) {
            fotos.put((Long) fila[0], (String) fila[1]);
        }
        return fotos;
    }

    @Override
    public void guardarImagenPerfil(Long colaboradorId, MultipartFile archivo) {

        Colaborador colaborador = buscarColaboradorXId(colaboradorId);

        String ruta = almacenArchivos.guardarImagenPerfil(colaboradorId, archivo);

        ImagenUsuario imagen = imagenUsuarioDao.findById(colaboradorId).orElseGet(() -> {
            ImagenUsuario nueva = new ImagenUsuario();
            nueva.setUsuario(colaborador);
            return nueva;
        });

        String rutaAnterior = imagen.getRutaImagen();

        imagen.setRutaImagen(ruta);
        imagen.setTextoAlternativo("Foto de perfil de " + colaborador.getNombre());

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

    private void eliminarImagenPerfil(Long colaboradorId) {

        ImagenUsuario imagen = imagenUsuarioDao.findById(colaboradorId).orElse(null);

        if (imagen == null) {
            return;
        }

        String ruta = imagen.getRutaImagen();

        imagenUsuarioDao.delete(imagen);
        imagenUsuarioDao.flush();

        almacenArchivos.borrar(ruta);
    }

    @Override
    public Map<Long, Long> contarJuegos() {

        Map<Long, Long> cuenta = new HashMap<>();

        for (Object[] fila : videojuegoDao.contarPorColaborador()) {
            cuenta.put((Long) fila[0], (Long) fila[1]);
        }
        return cuenta;
    }

    @Override
    public long contarJuegosXColaborador(Long id) {
        return videojuegoDao.countByColaboradorId(id);
    }
}
