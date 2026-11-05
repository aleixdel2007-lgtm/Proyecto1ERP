package controller;

import exception.CodigoPlataformaDuplicadoException;
import exception.NombrePlataformaDuplicadoException;
import exception.PlataformaConJuegosException;
import model.catalogo.Plataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.PlataformaService;

@Controller
public class ControllerPlataforma {

    @Autowired
    private PlataformaService plataformaService;

    @GetMapping("/catalogo/plataformas")
    public String listarPlataformas(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        model.addAttribute("plataformas", plataformaService.listarPlataformas(texto));
        model.addAttribute("juegos", plataformaService.contarJuegos());

        model.addAttribute("texto", texto);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "catalogo/plataformas";
    }

    @GetMapping("/catalogo/plataformas/nuevo")
    public String nuevaPlataforma(Model model) {

        model.addAttribute("plataforma", new Plataforma());

        return "catalogo/formularioNuevoPlat";
    }

    @GetMapping("/catalogo/plataformas/editar/{id}")
    public String editarPlataforma(@PathVariable Long id, Model model) {

        model.addAttribute("plataforma", plataformaService.buscarPlataformaXId(id));
        model.addAttribute("juegosAsignados", plataformaService.contarJuegosXPlataforma(id));

        return "catalogo/formularioEditarPlat";
    }

    @PostMapping("/catalogo/plataformas/guardar")
    public String guardarPlataforma(
            @ModelAttribute("plataforma") Plataforma plataforma,
            Model model,
            RedirectAttributes flash) {

        try {
            plataformaService.guardarPlataforma(plataforma);

        } catch (CodigoPlataformaDuplicadoException | NombrePlataformaDuplicadoException
                | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            if (plataforma.getId() == null) {
                return "catalogo/formularioNuevoPlat";
            }

            model.addAttribute("juegosAsignados",
                    plataformaService.contarJuegosXPlataforma(plataforma.getId()));

            return "catalogo/formularioEditarPlat";
        }

        flash.addFlashAttribute("mensaje", "Plataforma guardada correctamente");

        return "redirect:/catalogo/plataformas";
    }

    @PostMapping("/catalogo/plataformas/eliminar/{id}")
    public String eliminarPlataforma(@PathVariable Long id, RedirectAttributes flash) {

        try {
            plataformaService.eliminarPlataforma(id);

        } catch (PlataformaConJuegosException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo/plataformas";
        }

        flash.addFlashAttribute("mensaje", "Plataforma eliminada correctamente");

        return "redirect:/catalogo/plataformas";
    }
}
