package service.implement;

import config.AlmacenArchivos;
import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import exception.usuario.UsuarioNoEncontradoException;
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

    private Usuario buscarUsuarioXId(Long id) {

        Usuario usuario = usuarioDao.findById(id).orElse(null);

        if (usuario == null) {
            throw new UsuarioNoEncontradoException(id);
        }
        return usuario;
    }
}
