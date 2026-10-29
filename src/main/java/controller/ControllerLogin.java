package controller;

import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.regex.Pattern;

@Controller
@RequiredArgsConstructor
public class ControllerLogin {

    // Minimo 8 caracteres, una mayuscula y un numero
    private static final Pattern PASSWORD_VALIDA = Pattern.compile("^(?=.*[A-Z])(?=.*\\d).{8,}$");

    private final UsuarioDao usuarioDao;
    private final PasswordEncoder codificador;

    // El POST a /login lo procesa Spring Security (SecurityConfig).
    @GetMapping("/")
    public String index() {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return "redirect:/home";
        }else{
            return "login/login";
        }
    }

    @GetMapping("/registro")
    public String registro() {
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
                              @RequestParam(required = false) String aceptaCondiciones) {
        String correo = email.trim().toLowerCase();
        boolean valido = aceptaCondiciones != null
                && !nombre.isBlank() && !apellidos.isBlank()
                && password.equals(repetirPassword)
                && PASSWORD_VALIDA.matcher(password).matches()
                && usuarioDao.findByEmail(correo).isEmpty();
        if (!valido) {
            return "redirect:/registro?error";
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(nombre.trim());
        cliente.setApellidos(apellidos.trim());
        cliente.setEmail(correo);
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

    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/login";
    }

}
