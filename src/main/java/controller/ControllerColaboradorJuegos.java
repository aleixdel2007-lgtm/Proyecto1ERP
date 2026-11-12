package controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import exception.imagen.ImagenInvalidaException;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
                               @RequestParam(required = false) MultipartFile banner,
                               @RequestParam(required = false) List<MultipartFile> capturas,
                               Authentication authentication,
                               Model model,
                               RedirectAttributes flash) {

        try {
            videojuegoService.publicarJuegoColaborador(authentication.getName(), titulo, sinopsis,
                    pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

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
                "Juego subido. Queda inactivo hasta que un gestor lo publique en la tienda");

        return "redirect:/colaborador/mis-juegos";
    }

    @GetMapping("/colaborador/mis-juegos/{id}/editar")
    public String editarJuego(@PathVariable Long id, Authentication authentication, Model model) {

        Videojuego juego = videojuegoService.buscarJuegoDeColaborador(authentication.getName(), id);

        cargarEdicionInicial(model, juego);

        return "colaboradores/editar-juego";
    }

    @PostMapping("/colaborador/mis-juegos/{id}/actualizar")
    public String actualizarJuego(@PathVariable Long id,
                                  @RequestParam(required = false) String titulo,
                                  @RequestParam(required = false) String sinopsis,
                                  @RequestParam(required = false) String pegi,
                                  @RequestParam(required = false) String version,
                                  @RequestParam(required = false) List<Long> categoriaIds,
                                  @RequestParam(required = false) List<Long> plataformaIds,
                                  @RequestParam(required = false) MultipartFile zip,
                                  @RequestParam(required = false) MultipartFile portada,
                                  @RequestParam(required = false) MultipartFile banner,
                                  @RequestParam(required = false) List<MultipartFile> capturas,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes flash) {

        try {
            videojuegoService.actualizarJuegoColaborador(authentication.getName(), id, titulo,
                    sinopsis, pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            Videojuego juego = videojuegoService.buscarJuegoDeColaborador(authentication.getName(), id);

            model.addAttribute("error", e.getMessage());
            model.addAttribute("titulo", titulo);
            model.addAttribute("sinopsis", sinopsis);
            model.addAttribute("pegi", pegi);
            model.addAttribute("version", version);
            cargarEdicionConMarcadas(model, juego, conjuntoDe(categoriaIds), conjuntoDe(plataformaIds));

            return "colaboradores/editar-juego";
        }

        flash.addFlashAttribute("mensaje",
                "Cambios guardados. El juego queda inactivo hasta que un gestor lo vuelva a publicar");

        return "redirect:/colaborador/mis-juegos";
    }

    @PostMapping("/colaborador/mis-juegos/{id}/imagen/{imagenId}/quitar")
    public String quitarImagen(@PathVariable Long id,
                               @PathVariable Long imagenId,
                               Authentication authentication,
                               RedirectAttributes flash) {

        try {
            videojuegoService.eliminarImagenDeColaborador(authentication.getName(), id, imagenId);
            flash.addFlashAttribute("mensaje", "Imagen quitada");

        } catch (ImagenInvalidaException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/colaborador/mis-juegos/" + id + "/editar";
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

    // Primera vez que se abre la pantalla de editar: los chips marcados son los que tiene
    // GUARDADOS el juego, asi que los dos conjuntos salen de la propia entidad
    private void cargarEdicionInicial(Model model, Videojuego juego) {

        Set<Long> categoriasMarcadas = new HashSet<>();
        Set<Long> plataformasMarcadas = new HashSet<>();

        for (Categoria categoria : juego.getCategorias()) {
            categoriasMarcadas.add(categoria.getId());
        }

        for (Plataforma plataforma : juego.getPlataformas()) {
            plataformasMarcadas.add(plataforma.getId());
        }

        cargarEdicionConMarcadas(model, juego, categoriasMarcadas, plataformasMarcadas);
    }

    // Vuelta a la pantalla de editar despues de un error: los chips marcados son los que
    // ACABABA DE ELEGIR el usuario, que no son los guardados, y llegan ya hechos conjunto.
    // Ademas pone en el modelo las imagenes repartidas por tipo y la version activa
    private void cargarEdicionConMarcadas(Model model, Videojuego juego,
                                          Set<Long> categoriasMarcadas, Set<Long> plataformasMarcadas) {

        cargarFormulario(model, categoriasMarcadas, plataformasMarcadas);

        ImagenVideojuego portada = null;
        ImagenVideojuego banner = null;
        List<ImagenVideojuego> capturas = new ArrayList<>();

        for (ImagenVideojuego imagen : videojuegoService.listarImagenes(juego.getId())) {

            if (imagen.getTipo() == TipoImagen.PORTADA && portada == null) {
                portada = imagen;

            } else if (imagen.getTipo() == TipoImagen.BANNER && banner == null) {
                banner = imagen;

            } else if (imagen.getTipo() == TipoImagen.CAPTURA) {
                capturas.add(imagen);
            }
        }

        model.addAttribute("juego", juego);
        model.addAttribute("portada", portada);
        model.addAttribute("banner", banner);
        model.addAttribute("capturas", capturas);
        model.addAttribute("versionActiva", videojuegoService.buscarVersionActiva(juego.getId()));
    }

    private Set<Long> conjuntoDe(List<Long> ids) {
        return ids == null ? new HashSet<>() : new HashSet<>(ids);
    }
}
