package controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import exception.imagen.ImagenInvalidaException;
import model.catalogo.Videojuego;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.VideojuegoService;

@Controller
public class ControllerColaboradorJuegos {

    @Autowired
    private VideojuegoService videojuegoService;

    @GetMapping("/colaborador/mis-juegos")
    public String misJuegos(Authentication authentication, Model model) {

        List<Videojuego> juegos = videojuegoService.listarJuegosDeColaborador(authentication.getName());

        model.addAttribute("juegos", juegos);
        model.addAttribute("portadas", videojuegoService.listarPortadas(juegos));
        model.addAttribute("versiones", videojuegoService.listarVersionesActivas(juegos));

        return "colaboradores/mis-juegos";
    }

    @GetMapping("/colaborador/mis-juegos/nuevo")
    public String nuevoJuego(Model model) {

        // Formulario en blanco: no hay nada marcado todavia
        cargarFormulario(model, new HashSet<>(), new HashSet<>());

        return "colaboradores/nuevo-juego";
    }

    @PostMapping("/colaborador/mis-juegos/guardar")
    public String guardarJuego(@RequestParam(required = false) String titulo,
                               @RequestParam(required = false) String sinopsis,
                               @RequestParam(required = false) String pegi,
                               @RequestParam(required = false) String version,
                               @RequestParam(required = false) List<Long> categoriaIds,
                               @RequestParam(required = false) List<Long> plataformaIds,
                               @RequestParam(required = false) MultipartFile zip,
                               @RequestParam(required = false) MultipartFile portada,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes flash) {

        try {
            videojuegoService.publicarJuegoColaborador(authentication.getName(), titulo, sinopsis,
                    pegi, version, categoriaIds, plataformaIds, zip, portada);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            // Se devuelve el formulario con lo que habia escrito y marcado: solo los
            // archivos hay que volver a elegirlos, el navegador no deja rellenarlos
            model.addAttribute("error", e.getMessage());
            model.addAttribute("titulo", titulo);
            model.addAttribute("sinopsis", sinopsis);
            model.addAttribute("pegi", pegi);
            model.addAttribute("version", version);
            cargarFormulario(model, conjuntoDe(categoriaIds), conjuntoDe(plataformaIds));

            return "colaboradores/nuevo-juego";
        }

        flash.addFlashAttribute("mensaje",
                "Juego subido. Queda inactivo hasta que se revise y se publique en la tienda");

        return "redirect:/colaborador/mis-juegos";
    }

    // Lo que necesita el formulario tanto en blanco como al volver con un error. Los dos
    // conjuntos son los ids marcados, para que los chips sigan seleccionados
    private void cargarFormulario(Model model, Set<Long> categoriasMarcadas, Set<Long> plataformasMarcadas) {

        model.addAttribute("categorias", videojuegoService.listarCategorias());
        model.addAttribute("plataformas", videojuegoService.listarPlataformas());
        model.addAttribute("pegis", videojuegoService.listarPegis());
        model.addAttribute("categoriasMarcadas", categoriasMarcadas);
        model.addAttribute("plataformasMarcadas", plataformasMarcadas);
    }

    private Set<Long> conjuntoDe(List<Long> ids) {
        return ids == null ? new HashSet<>() : new HashSet<>(ids);
    }
}
