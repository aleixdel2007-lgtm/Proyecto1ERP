package service.interfaces;

import java.time.LocalDate;
import model.seguridad.ImagenUsuario;
import model.seguridad.Usuario;
import org.springframework.web.multipart.MultipartFile;

public interface UsuarioService {

    Usuario buscarUsuarioXEmail(String email);

    void guardarDatosPersonales(Long usuarioId, String nombre, String apellidos,
                                String telefono, String direccion, String ciudad,
                                String pais, LocalDate fechaNacimiento);

    ImagenUsuario buscarImagenPerfil(Long usuarioId);

    void guardarImagenPerfil(Long usuarioId, MultipartFile archivo, String textoAlternativo);
}
