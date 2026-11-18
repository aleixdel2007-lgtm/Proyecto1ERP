package service.implement;

import config.AlmacenArchivos;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import exception.usuario.UsuarioNoEncontradoException;
import java.time.LocalDate;
import java.util.Locale;
import model.seguridad.Cliente;
import model.seguridad.ImagenUsuario;
import model.seguridad.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import service.interfaces.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final int MAX_INTENTOS = 5;
    private static final Locale IDIOMA_MOTIVO = Locale.forLanguageTag("es");

    @Autowired
    private UsuarioDao usuarioDao;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ImagenUsuarioDao imagenUsuarioDao;

    @Autowired
    private AlmacenArchivos almacenArchivos;

    @Override
    public Usuario buscarUsuarioXEmail(String email) {

        Usuario usuario = usuarioDao.findByEmail(email).orElse(null);

        if (usuario == null) {
            throw new UsuarioNoEncontradoException(email);
        }
        return usuario;
    }

    @Override
    public void guardarDatosPersonales(Long usuarioId, String nombre, String apellidos,
                                       String telefono, String direccion, String ciudad,
                                       String pais, LocalDate fechaNacimiento) {

        Usuario usuario = buscarUsuarioXId(usuarioId);

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("perfil.error.nombreObligatorio");
        }

        usuario.setNombre(nombre.trim());
        usuario.setApellidos(normalizarTexto(apellidos));

        if (usuario instanceof Cliente cliente) {
            cliente.setTelefono(normalizarTexto(telefono));
            cliente.setDireccion(normalizarTexto(direccion));
            cliente.setCiudad(normalizarTexto(ciudad));
            cliente.setPais(normalizarTexto(pais));
            cliente.setFechaNacimiento(fechaNacimiento);
        }
        usuarioDao.save(usuario);
    }

    @Override
    public ImagenUsuario buscarImagenPerfil(Long usuarioId) {
        return imagenUsuarioDao.findById(usuarioId).orElse(null);
    }

    @Override
    public void guardarImagenPerfil(Long usuarioId, MultipartFile archivo, String textoAlternativo) {

        Usuario usuario = buscarUsuarioXId(usuarioId);

        String ruta = almacenArchivos.guardarImagenPerfil(usuarioId, archivo);

        ImagenUsuario imagen = imagenUsuarioDao.findById(usuarioId).orElseGet(() -> {
            ImagenUsuario nueva = new ImagenUsuario();
            nueva.setUsuario(usuario);
            return nueva;
        });

        String rutaAnterior = imagen.getRutaImagen();

        imagen.setRutaImagen(ruta);
        imagen.setTextoAlternativo(textoAlternativo == null || textoAlternativo.isBlank()
                ? "Foto de perfil de " + usuario.getNombre() : textoAlternativo.trim());

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

    // Suma un fallo y bloquea la cuenta al llegar al limite; devuelve true solo si la ha bloqueado este
    @Override
    @Transactional
    public boolean registrarFalloLogin(String email) {

        String correo = normalizarCorreo(email);

        // Si el correo no existe no hay fila que actualizar, y no hace falta leer nada mas
        if (correo == null || usuarioDao.sumarIntentoFallido(correo) == 0) {
            return false;
        }

        Usuario usuario = usuarioDao.findByEmail(correo).orElse(null);

        // Solo se bloquea si sigue activa y el contador ha llegado al limite: un motivo a mano no se pisa
        if (usuario == null || !usuario.isActivo() || usuario.getIntentosFallidos() < MAX_INTENTOS) {
            return false;
        }

        usuario.setActivo(false);
        usuario.setMotivoInactivacion(messageSource.getMessage("clientes.motivo.intentosFallidos",
                new Object[]{MAX_INTENTOS}, IDIOMA_MOTIVO));
        usuarioDao.save(usuario);

        return true;
    }

    // Al entrar bien el contador vuelve a cero, asi que los fallos sueltos no se acumulan para siempre
    @Override
    @Transactional
    public void registrarAccesoCorrecto(String email) {

        String correo = normalizarCorreo(email);

        // Sin correo no hay nada que reiniciar
        if (correo != null) {
            usuarioDao.reiniciarIntentosFallidos(correo);
        }
    }

    // El correo solo se recorta: la colacion de MySQL ya ignora las mayusculas al buscar la fila
    private String normalizarCorreo(String email) {
        return (email == null || email.isBlank()) ? null : email.trim();
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }
        return texto.trim();
    }

    private Usuario buscarUsuarioXId(Long id) {

        Usuario usuario = usuarioDao.findById(id).orElse(null);

        if (usuario == null) {
            throw new UsuarioNoEncontradoException(id);
        }
        return usuario;
    }
}
