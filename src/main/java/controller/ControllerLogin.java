package controller;

import dao.UsuarioDao;
import exception.email.CorreoYaRegistradoException;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import model.seguridad.Paises;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.context.MessageSource;

import java.time.LocalDate;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.ui.Model;
import service.interfaces.ClienteService;

@Controller
@RequiredArgsConstructor
public class ControllerLogin {

    // Minimo 8 caracteres, una mayuscula y un numero
    private static final Pattern PASSWORD_VALIDA = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder codificador;
    private final ClienteService clienteService;
    private final MessageSource messageSource;

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

    @GetMapping("/login")
    public String login() {
        return "login/login";
    }

}
