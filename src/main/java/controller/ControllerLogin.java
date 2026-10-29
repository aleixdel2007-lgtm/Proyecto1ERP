package controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerLogin {

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
