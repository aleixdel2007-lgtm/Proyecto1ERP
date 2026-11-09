package controller;

import exception.imagen.ImagenInvalidaException;
import exception.usuario.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import model.seguridad.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.UsuarioService;

@Controller
@RequiredArgsConstructor
public class ControllerHome {

    private final UsuarioService usuarioService;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {

        Usuario usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());

        model.addAttribute("usuario", usuario);

        return "home";
    }

    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {

        Usuario usuario;

        try {
            usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());

        } catch (UsuarioNoEncontradoException e) {
            // Sesion de un usuario que ya no existe en la base de datos: a iniciar sesion de nuevo
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", usuario instanceof Cliente c ? c : null);
        model.addAttribute("imagen", usuarioService.buscarImagenPerfil(usuario.getId()));

        return "clients/configuracionUsuario";
    }

    @PostMapping("/user/profile/imagen")
    public String cambiarImagen(Authentication authentication,
            @RequestParam("imagen") MultipartFile archivo,
            @RequestParam(value = "textoAlternativo", required = false) String textoAlternativo,
            RedirectAttributes flash) {

        Usuario usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());

        try {
            usuarioService.guardarImagenPerfil(usuario.getId(), archivo, textoAlternativo);

        } catch (ImagenInvalidaException e) {
            flash.addFlashAttribute("errorImagen", e.getMessage());
            return "redirect:/user/profile";
        }

        flash.addFlashAttribute("okImagen", "Foto actualizada.");

        return "redirect:/user/profile";
    }
}
