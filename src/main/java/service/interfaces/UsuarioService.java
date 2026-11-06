package service.interfaces;

import model.seguridad.ImagenUsuario;
import model.seguridad.Usuario;
import org.springframework.web.multipart.MultipartFile;

public interface UsuarioService {

    Usuario buscarUsuarioXEmail(String email);

    ImagenUsuario buscarImagenPerfil(Long usuarioId);

    void guardarImagenPerfil(Long usuarioId, MultipartFile archivo, String textoAlternativo);
}
