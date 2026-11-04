package controller;

import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import model.seguridad.ImagenUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

// La navbar sale en casi todas las paginas, asi que la foto del usuario se pone en el modelo aqui
@ControllerAdvice
@RequiredArgsConstructor
public class ImagenPerfilAdvice {

    private final UsuarioDao usuarioDao;
    private final ImagenUsuarioDao imagenUsuarioDao;


    @ModelAttribute("imagenPerfil")
    public ImagenUsuario imagenPerfil(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        return usuarioDao.findByEmail(authentication.getName())
                .flatMap(u -> imagenUsuarioDao.findById(u.getId()))
                .orElse(null);
    }
}
