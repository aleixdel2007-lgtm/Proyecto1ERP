package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import service.interfaces.PanelService;

@Controller
@RequiredArgsConstructor
public class ControllerPanelControl {

    private final PanelService panelService;

    // Las cifras entran en el modelo de golpe, con los nombres que les pone el servicio
    @GetMapping("/panelControl")
    public String panelControl(Model model) {

        model.addAllAttributes(panelService.resumen());

        return "panelControl";
    }
}
