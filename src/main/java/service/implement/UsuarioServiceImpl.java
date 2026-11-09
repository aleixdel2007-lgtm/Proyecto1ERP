package service.implement;

import config.AlmacenArchivos;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import exception.usuario.UsuarioNoEncontradoException;
import java.time.LocalDate;
import model.seguridad.Cliente;
import model.seguridad.ImagenUsuario;
import model.seguridad.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import service.interfaces.UsuarioService;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioDao usuarioDao;

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
            throw new IllegalArgumentException("El nombre es obligatorio");
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
