package controller;

import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import model.seguridad.ImagenUsuario;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdviceImagenPerfil {

    private final UsuarioDao usuarioDao;
    private final ImagenUsuarioDao imagenUsuarioDao;


    // Pone la foto del usuario en el modelo de todas las vistas, para la navbar
    @ModelAttribute("imagenPerfil")
    public ImagenUsuario imagenPerfil(Authentication authentication) {

        // Sin sesion no hay foto
        // Paginas publicas (login, registro): no hay usuario, luego no hay foto
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        // Busca al usuario por email y luego su imagen por el mismo id
        return usuarioDao.findByEmail(authentication.getName())
                .flatMap(u -> imagenUsuarioDao.findById(u.getId()))
                .orElse(null);
    }
}
