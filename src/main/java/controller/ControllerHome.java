package controller;


import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
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
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElse(null);
        model.addAttribute("nombre", usuario.getNombre());
        return "home";
    }

    @GetMapping("/tienda")
    public String tienda() {
        return "client/tienda";
    }
}
