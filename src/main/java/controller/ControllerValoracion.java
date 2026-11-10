package controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.interfaces.ValoracionService;

@Controller
@RequestMapping("/valoraciones")
public class ControllerValoracion {

    @Autowired
    private ValoracionService valoracionService;

    @PostMapping("/guardar")
    public String guardar(@RequestParam Long videojuegoId,
                          @RequestParam String titulo,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam("valoracion") int puntuacion,
                          Authentication authentication) {

        valoracionService.crear(authentication.getName(), videojuegoId, titulo, descripcion, puntuacion);
        return "redirect:/tienda/juego/" + videojuegoId;
    }
}
