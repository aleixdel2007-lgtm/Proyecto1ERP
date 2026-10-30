package controller;


import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class ControllerHome {

    private final UsuarioDao usuarioDao;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElseThrow();

        if (usuario.getRol() == Rol.CLIENTE) {
            return "redirect:/tienda";
        }
        model.addAttribute("usuario", usuario);
        return "home";
    }

    @GetMapping("/tienda")
    public String tienda() {
        return "client/tienda";
    }

    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", usuario instanceof Cliente c ? c : null);
        return "clients/configuracionUsuario";
    }
}
