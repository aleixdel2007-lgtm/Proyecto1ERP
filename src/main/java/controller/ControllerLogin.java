package controller;

import exception.email.CorreoYaRegistradoException;
import exception.usuario.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import model.seguridad.Paises;
import model.seguridad.Usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.context.MessageSource;

import java.beans.Encoder;
import java.net.Authenticator;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.ui.Model;
import service.interfaces.ClienteService;
import service.interfaces.CuentaService;

import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequiredArgsConstructor
public class ControllerLogin {

    private final ClienteService clienteService;
    private final MessageSource messageSource;
    private final CuentaService cuentaService;

    // Con sesion va al home; sin ella, al login
    @GetMapping("/")
    public String index() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return "redirect:/panelControl";
        } else {
            return "login/login";
        }
    }

    @GetMapping("/registro")
    public String registro(Model model) {
        Paises[] paises = Paises.values();
        model.addAttribute("paises", paises);
        return "login/crearCuenta";
    }

    @PostMapping("/registro")
    public String crearCuenta(@RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String repetirPassword,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) LocalDate fechaNacimiento,
            @RequestParam(required = false) String aceptaCondiciones,
            Model model, Locale locale) {
        try {
            cuentaService.registrarCliente(nombre, apellidos, email, password, repetirPassword,
                    pais, fechaNacimiento, aceptaCondiciones != null);
        } catch (CorreoYaRegistradoException e) {
            // Se devuelve el formulario sin redirect para conservar lo escrito (menos las contrasenas)
            model.addAttribute("errorEmail", messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale));
            model.addAttribute("nombre", nombre);
            model.addAttribute("apellidos", apellidos);
            model.addAttribute("email", email);
            model.addAttribute("pais", pais);
            model.addAttribute("paises", Paises.values());
            model.addAttribute("fechaNacimiento", fechaNacimiento);
            return "login/crearCuenta";
        } catch (IllegalArgumentException e) {
            return "redirect:/registro?error";
        }
        return "redirect:/login?registrado";
    }

    @PostMapping("/user/changePassword")
    public String cambiarPassword(@RequestParam String passwordActual,
                                @RequestParam String nuevaPassword,
                                @RequestParam String repetirPassword,
                                Authentication authentication,
                                RedirectAttributes flash, Locale locale) {

        try {
            cuentaService.cambiarPassword(authentication.getName(),
                    passwordActual, nuevaPassword, repetirPassword);
        } catch (UsuarioNoEncontradoException e) {
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", messageSource.getMessage(e.getMessage(), null, locale));
            return "redirect:/user/changePassword";
        }

        flash.addFlashAttribute("okPassword", messageSource.getMessage("password.ok", null, locale));
        return "redirect:/user/profile";
    }
    

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

}
