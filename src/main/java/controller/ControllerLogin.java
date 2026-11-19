package controller;

import dao.UsuarioDao;
import exception.email.CorreoYaRegistradoException;
import exception.usuario.UsuarioNoEncontradoException;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import model.seguridad.Paises;
import model.seguridad.Usuario;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
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
import java.util.regex.Pattern;
import org.springframework.ui.Model;
import service.interfaces.ClienteService;
import service.interfaces.UsuarioService;

import org.springframework.web.bind.annotation.RequestBody;


@Controller
@RequiredArgsConstructor
public class ControllerLogin {

    // Minimo 8 caracteres, una mayuscula y un numero
    private static final Pattern PASSWORD_VALIDA = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    // Reglas del cambio de contraseña: una mayúscula (también Ñ, À...) y un símbolo especial
    private static final Pattern TIENE_MAYUSCULA = Pattern.compile("\\p{Lu}");
    private static final Pattern TIENE_SIMBOLO = Pattern.compile("[^\\p{L}\\p{N}\\s]");

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder codificador;
    private final ClienteService clienteService;
    private final MessageSource messageSource;
    private final UsuarioService usuarioService;

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
        // El correo se guarda siempre en minusculas y sin espacios
        String correo = email.trim().toLowerCase();

        try {
            // Primera comprobacion: que el correo no este ya registrado
            clienteService.comprobarCorreoLibre(correo);
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
        }
        // Validacion del servidor: no se fia de lo que haya comprobado el navegador
        boolean valido = aceptaCondiciones != null
                && !nombre.isBlank() && !apellidos.isBlank()
                && password.equals(repetirPassword)
                && PASSWORD_VALIDA.matcher(password).matches()
                && usuarioDao.findByEmail(correo).isEmpty();
        if (!valido) {
            return "redirect:/registro?error";
        }

        // Todo correcto: se crea el cliente con los datos ya limpios
        Cliente cliente = new Cliente();
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(apellidos.trim());
        cliente.setEmail(correo);
        // La contrasena se guarda cifrada, nunca en claro
        cliente.setPasswordHash(codificador.encode(password));
        cliente.setPais(pais == null || pais.isBlank() ? null : pais);
        cliente.setFechaNacimiento(fechaNacimiento);
        usuarioDao.save(cliente);
        return "redirect:/login?registrado";
    }

    @PostMapping("/user/changePassword")
    public String cambiarPassword(@RequestParam String passwordActual,
                                @RequestParam String nuevaPassword,
                                @RequestParam String repetirPassword,
                                Authentication authentication,
                                RedirectAttributes flash, Locale locale) {

        Usuario usuario;
        try {
            usuario = usuarioService.buscarUsuarioXEmail(authentication.getName());
        } catch (UsuarioNoEncontradoException e) {
            return "redirect:/login";
        }

        // 1. La contraseña actual: matches(claro, hash), nunca encode + equals
        if (!codificador.matches(passwordActual, usuario.getPasswordHash())) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.actual", null, locale));
            return "redirect:/user/changePassword";
        }

        // 2. La nueva y su repetición deben coincidir
        if (!nuevaPassword.equals(repetirPassword)) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.noCoincide", null, locale));
            return "redirect:/user/changePassword";
        }

        // 3. Longitud mínima
        if (nuevaPassword.length() < 8) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.corta", null, locale));
            return "redirect:/user/changePassword";
        }

        // 4. Al menos una mayúscula
        if (!TIENE_MAYUSCULA.matcher(nuevaPassword).find()) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.mayuscula", null, locale));
            return "redirect:/user/changePassword";
        }

        // 5. Al menos un símbolo especial (cualquier cosa que no sea letra, número ni espacio)
        if (!TIENE_SIMBOLO.matcher(nuevaPassword).find()) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.simbolo", null, locale));
            return "redirect:/user/changePassword";
        }

        // 6. Distinta de la actual
        if (codificador.matches(nuevaPassword, usuario.getPasswordHash())) {
            flash.addFlashAttribute("error", messageSource.getMessage("password.error.igual", null, locale));
            return "redirect:/user/changePassword";
        }

        // 7. Todo bien: codificar, guardar y volver al perfil
        usuario.setPasswordHash(codificador.encode(nuevaPassword));
        usuarioDao.save(usuario);

        flash.addFlashAttribute("okPassword", messageSource.getMessage("password.ok", null, locale));
        return "redirect:/user/profile";
    }
    

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

}
