package controller;

import exception.plataforma.CodigoPlataformaDuplicadoException;
import exception.plataforma.NombrePlataformaDuplicadoException;
import exception.plataforma.PlataformaConJuegosException;
import model.catalogo.Plataforma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.PlataformaService;

import java.util.Locale;

@Controller
public class ControllerPlataforma {

    @Autowired
    private PlataformaService plataformaService;

    @Autowired
    private MessageSource messageSource;

    // Listado con buscador y paginacion
    @GetMapping("/catalogo/plataformas")
    public String listarPlataformas(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        // El filtro por texto lo aplica el servicio; la paginacion la hace la plantilla
        model.addAttribute("plataformas", plataformaService.listarPlataformas(texto));
        // Total de juegos por plataforma, para la columna del listado
        model.addAttribute("juegos", plataformaService.contarJuegos());

        // Se devuelven para que el filtro y la pagina sigan marcados
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

    // Sirve para crear y para editar: sin id es una plataforma nueva
    @PostMapping("/catalogo/plataformas/guardar")
    public String guardarPlataforma(
            @ModelAttribute("plataforma") Plataforma plataforma,
            Model model,
            RedirectAttributes flash,
            Locale locale) {

        try {
            // El servicio valida que el codigo y el nombre no esten repetidos
            plataformaService.guardarPlataforma(plataforma);

        } catch (CodigoPlataformaDuplicadoException | NombrePlataformaDuplicadoException
                | IllegalArgumentException e) {

            // Se vuelve al formulario con el error, el que corresponda segun sea alta o edicion
            model.addAttribute("error", traducir(e, locale));

            if (plataforma.getId() == null) {
                return "catalogo/formularioNuevoPlat";
            }

            model.addAttribute("juegosAsignados",
                    plataformaService.contarJuegosXPlataforma(plataforma.getId()));

            return "catalogo/formularioEditarPlat";
        }

        // El flash sobrevive al redirect, el Model no
        flash.addFlashAttribute("mensaje", messageSource.getMessage("plataformas.ok.guardada", null, locale));

        return "redirect:/catalogo/plataformas";
    }

    @PostMapping("/catalogo/plataformas/eliminar/{id}")
    public String eliminarPlataforma(@PathVariable Long id, RedirectAttributes flash,
            Locale locale) {

        try {
            plataformaService.eliminarPlataforma(id);

        } catch (PlataformaConJuegosException e) {
            // No se borra una plataforma que aun tiene juegos
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/catalogo/plataformas";
        }

        flash.addFlashAttribute("mensaje", messageSource.getMessage("plataformas.ok.eliminada", null, locale));

        return "redirect:/catalogo/plataformas";
    }

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }
}
