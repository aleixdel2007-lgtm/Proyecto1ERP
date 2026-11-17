package controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.support.RequestContextUtils;
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

    @Autowired
    private MessageSource messageSource;

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

    // Devuelve Object porque tiene dos salidas: el nombre de una vista (el envio normal del
    // formulario) o un ResponseEntity con JSON (el envio por XHR con barra de progreso)
    @PostMapping("/colaborador/mis-juegos/guardar")
    public Object guardarJuego(@RequestParam(required = false) String titulo,
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
                               RedirectAttributes flash,
                               Locale locale,
                               HttpServletRequest request,
                               HttpServletResponse response) {

        try {
            videojuegoService.publicarJuegoColaborador(authentication.getName(), titulo, sinopsis,
                    pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            // Por XHR el formulario sigue en pantalla con todo lo rellenado, archivos
            // incluidos: solo hace falta decirle que fallo y por que
            if (esXhr(request)) {
                return errorJson(traducir(e, locale));
            }

            // Se devuelve el formulario con lo que habia escrito y marcado: solo los
            // archivos hay que volver a elegirlos, el navegador no deja rellenarlos
            model.addAttribute("error", traducir(e, locale));
            model.addAttribute("titulo", titulo);
            model.addAttribute("sinopsis", sinopsis);
            model.addAttribute("pegi", pegi);
            model.addAttribute("version", version);
            cargarFormulario(model, conjuntoDe(categoriaIds), conjuntoDe(plataformaIds));

            return "colaboradores/nuevo-juego";
        }

        return exito(request, response, flash, messageSource.getMessage("colab.ok.subido", null, locale));
    }

    @GetMapping("/colaborador/mis-juegos/{id}/editar")
    public String editarJuego(@PathVariable Long id, Authentication authentication, Model model) {

        Videojuego juego = videojuegoService.buscarJuegoDeColaborador(authentication.getName(), id);

        cargarEdicionInicial(model, juego);

        return "colaboradores/editar-juego";
    }

    @PostMapping("/colaborador/mis-juegos/{id}/actualizar")
    public Object actualizarJuego(@PathVariable Long id,
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
                                  RedirectAttributes flash,
                                  Locale locale,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {

        try {
            videojuegoService.actualizarJuegoColaborador(authentication.getName(), id, titulo,
                    sinopsis, pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            if (esXhr(request)) {
                return errorJson(traducir(e, locale));
            }

            Videojuego juego = videojuegoService.buscarJuegoDeColaborador(authentication.getName(), id);

            model.addAttribute("error", traducir(e, locale));
            model.addAttribute("titulo", titulo);
            model.addAttribute("sinopsis", sinopsis);
            model.addAttribute("pegi", pegi);
            model.addAttribute("version", version);
            cargarEdicionConMarcadas(model, juego, conjuntoDe(categoriaIds), conjuntoDe(plataformaIds));

            return "colaboradores/editar-juego";
        }

        return exito(request, response, flash, messageSource.getMessage("colab.ok.actualizado", null, locale));
    }

    @PostMapping("/colaborador/mis-juegos/{id}/imagen/{imagenId}/quitar")
    public String quitarImagen(@PathVariable Long id,
                               @PathVariable Long imagenId,
                               Authentication authentication,
                               RedirectAttributes flash,
                               Locale locale) {

        try {
            videojuegoService.eliminarImagenDeColaborador(authentication.getName(), id, imagenId);
            flash.addFlashAttribute("mensaje", messageSource.getMessage("colab.ok.imagenQuitada", null, locale));

        } catch (ImagenInvalidaException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/colaborador/mis-juegos/" + id + "/editar";
    }

    // El JS de la subida manda esta cabecera. Sin ella (JS desactivado, otro cliente) todo
    // funciona como un formulario de toda la vida: redirect o vista con el error
    private boolean esXhr(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    // Fin feliz. En el envio normal el mensaje va como flash y se redirige. Por XHR no hay
    // redirect que guarde el flash (RedirectView es quien lo hace), asi que se guarda a
    // mano para que lo recoja la siguiente peticion, que es la que hace el JS al navegar
    // a la lista, y se le dice a donde ir
    private Object exito(HttpServletRequest request, HttpServletResponse response,
                         RedirectAttributes flash, String mensaje) {

        if (esXhr(request)) {
            FlashMap salida = RequestContextUtils.getOutputFlashMap(request);
            salida.put("mensaje", mensaje);
            RequestContextUtils.getFlashMapManager(request).saveOutputFlashMap(salida, request, response);

            return ResponseEntity.ok(Map.of("ok", true,
                    "redirect", request.getContextPath() + "/colaborador/mis-juegos"));
        }

        flash.addFlashAttribute("mensaje", mensaje);

        return "redirect:/colaborador/mis-juegos";
    }

    private ResponseEntity<Map<String, Object>> errorJson(String mensaje) {
        return ResponseEntity.badRequest().body(Map.of("ok", false, "error", mensaje));
    }

    // El mensaje de la excepcion es la clave de traduccion: aqui se pasa al idioma del
    // usuario. Si la clave no existiera se ensena la propia clave.
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
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
