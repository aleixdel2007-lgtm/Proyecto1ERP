package controller;

import exception.colaborador.ColaboradorConJuegosException;
import exception.email.EmailDuplicadoException;
import exception.imagen.ImagenInvalidaException;
import java.util.List;
import model.seguridad.Colaborador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.ColaboradorService;

@Controller
public class ControllerColaborador {

    @Autowired
    private ColaboradorService colaboradorService;

    @GetMapping("/admin/colaboradores")
    public String listarColaboradores(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        List<Colaborador> lista = colaboradorService.listarColaboradores(texto, activo);

        model.addAttribute("colaboradores", lista);
        model.addAttribute("fotos", colaboradorService.listarImagenesPerfil(lista));
        model.addAttribute("juegos", colaboradorService.contarJuegos());

        model.addAttribute("texto", texto);
        model.addAttribute("activo", activo);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "admin/colaboradores";
    }

    @GetMapping("/admin/colaboradores/nuevo")
    public String nuevoColaborador(Model model) {

        model.addAttribute("colaborador", new Colaborador());

        return "admin/formularioNuevoCol";
    }

    @GetMapping("/admin/colaboradores/editar/{id}")
    public String editarColaborador(@PathVariable Long id, Model model) {

        model.addAttribute("colaborador", colaboradorService.buscarColaboradorXId(id));
        model.addAttribute("juegosPublicados", colaboradorService.contarJuegosXColaborador(id));
        model.addAttribute("imagenColaborador", colaboradorService.buscarImagenPerfil(id));

        return "admin/formularioEditarCol";
    }

    @PostMapping("/admin/colaboradores/guardar")
    public String guardarColaborador(
            @ModelAttribute("colaborador") Colaborador colaborador,
            @RequestParam(required = false) String password,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Model model,
            RedirectAttributes flash) {

        Colaborador guardado;

        try {
            guardado = colaboradorService.guardarColaborador(colaborador, password);

        } catch (EmailDuplicadoException | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            if (colaborador.getId() == null) {
                return "admin/formularioNuevoCol";
            }

            model.addAttribute("juegosPublicados",
                    colaboradorService.contarJuegosXColaborador(colaborador.getId()));
            model.addAttribute("imagenColaborador",
                    colaboradorService.buscarImagenPerfil(colaborador.getId()));

            return "admin/formularioEditarCol";
        }

        if (foto != null && !foto.isEmpty()) {

            try {
                colaboradorService.guardarImagenPerfil(guardado.getId(), foto);

            } catch (ImagenInvalidaException e) {
                flash.addFlashAttribute("error",
                        "Colaborador guardado, pero la foto no: " + e.getMessage());
                return "redirect:/admin/colaboradores";
            }
        }

        flash.addFlashAttribute("mensaje", "Colaborador guardado correctamente");

        return "redirect:/admin/colaboradores";
    }

    @PostMapping("/admin/colaboradores/eliminar/{id}")
    public String eliminarColaborador(@PathVariable Long id, RedirectAttributes flash) {

        try {
            colaboradorService.eliminarColaborador(id);

        } catch (ColaboradorConJuegosException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/colaboradores";
        }

        flash.addFlashAttribute("mensaje", "Colaborador eliminado correctamente");

        return "redirect:/admin/colaboradores";
    }
}
