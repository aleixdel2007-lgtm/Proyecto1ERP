package controller;

import exception.imagen.ImagenInvalidaException;
import exception.usuario.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.Locale;
import model.seguridad.Cliente;
import model.seguridad.Usuario;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
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
    private final MessageSource messageSource;

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

    @PostMapping("/user/profile")
    public String guardarPerfil(Authentication authentication,
            @RequestParam String nombre,
            @RequestParam(required = false) String apellidos,
            @RequestParam(required = false) String telefono,
            @RequestParam(required = false) String direccion,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaNacimiento,
            RedirectAttributes flash, Locale locale) {

        Usuario usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());

        try {
            usuarioService.guardarDatosPersonales(usuario.getId(), nombre, apellidos,
                    telefono, direccion, ciudad, pais, fechaNacimiento);

        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("errorDatos", traducir(e.getMessage(), locale));
            return "redirect:/user/profile";
        }

        flash.addFlashAttribute("okDatos", traducir("perfil.ok.datos", locale));

        return "redirect:/user/profile";
    }

    @PostMapping("/user/profile/imagen")
    public String cambiarImagen(Authentication authentication,
            @RequestParam("imagen") MultipartFile archivo,
            @RequestParam(value = "textoAlternativo", required = false) String textoAlternativo,
            RedirectAttributes flash, Locale locale) {

        Usuario usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());

        try {
            usuarioService.guardarImagenPerfil(usuario.getId(), archivo, textoAlternativo);
        } catch (ImagenInvalidaException e) {
            flash.addFlashAttribute("errorImagen", traducir(e.getMessage(), locale));
            return "redirect:/user/profile";
        }

        flash.addFlashAttribute("okImagen", traducir("perfil.ok.imagen", locale));

        return "redirect:/user/profile";
    }

    // Las validaciones y los avisos llevan la clave de traduccion, no el texto: aqui se
    // pasa al idioma del usuario. Si la clave no existiera se ensena la propia clave.
    private String traducir(String clave, Locale locale) {
        return messageSource.getMessage(clave, null, clave, locale);
    }
}
