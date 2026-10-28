package controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ControllerLogin {

    // El POST a /login lo procesa Spring Security (SecurityConfig).
    @GetMapping("/login")
    public String login() {
        return "login/login";
    }
    @GetMapping("/logout")
    public String logout() {
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }
}
