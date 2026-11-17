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

    // Juegos del colaborador con su portada y su version activa
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

        // Formulario en blanco: nada marcado
        cargarFormulario(model, new HashSet<>(), new HashSet<>());

        return "colaboradores/nuevo-juego";
    }

    // Devuelve Object porque tiene dos salidas: una vista (envio normal) o un JSON (envio por XHR)
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
            // El colaborador sale de la sesion; el juego queda inactivo hasta que lo revise un gestor
            videojuegoService.publicarJuegoColaborador(authentication.getName(), titulo, sinopsis,
                    pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            // Por XHR el formulario sigue en pantalla con los archivos: solo se avisa del error
            if (esXhr(request)) {
                return errorJson(traducir(e, locale));
            }

            // Envio normal: se devuelve el formulario con lo escrito; los archivos hay que volver a elegirlos
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

        // Comprueba que el juego es de este colaborador
        Videojuego juego = videojuegoService.buscarJuegoDeColaborador(authentication.getName(), id);

        cargarEdicionInicial(model, juego);

        return "colaboradores/editar-juego";
    }

    // Igual que guardarJuego: devuelve vista o JSON segun sea envio normal o XHR
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
            // El servicio comprueba que el juego es de este colaborador; al guardar vuelve a inactivo
            videojuegoService.actualizarJuegoColaborador(authentication.getName(), id, titulo,
                    sinopsis, pegi, version, categoriaIds, plataformaIds, zip, portada, banner, capturas);

        } catch (IllegalArgumentException | ImagenInvalidaException e) {

            if (esXhr(request)) {
                return errorJson(traducir(e, locale));
            }

            // Se recarga el juego guardado y se devuelve el formulario con lo que escribio el usuario
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

    // El JS de la subida manda esta cabecera; sin ella todo funciona como un formulario normal
    private boolean esXhr(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }

    // Fin feliz: por XHR guarda el flash a mano y devuelve JSON con la URL destino; si no, redirect
    private Object exito(HttpServletRequest request, HttpServletResponse response,
                         RedirectAttributes flash, String mensaje) {

        if (esXhr(request)) {
            // Sin redirect nadie guarda el flash, asi que se guarda aqui para la siguiente peticion
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

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }

    // Listas a elegir del formulario y ids ya marcados
    private void cargarFormulario(Model model, Set<Long> categoriasMarcadas, Set<Long> plataformasMarcadas) {

        model.addAttribute("categorias", videojuegoService.listarCategorias());
        model.addAttribute("plataformas", videojuegoService.listarPlataformas());
        model.addAttribute("pegis", videojuegoService.listarPegis());
        model.addAttribute("categoriasMarcadas", categoriasMarcadas);
        model.addAttribute("plataformasMarcadas", plataformasMarcadas);
    }

    // Primera apertura de editar: lo marcado es lo que ya tiene guardado el juego
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

    // Editar tras un error: lo marcado es lo que acababa de elegir el usuario; tambien carga imagenes y version activa
    private void cargarEdicionConMarcadas(Model model, Videojuego juego,
                                          Set<Long> categoriasMarcadas, Set<Long> plataformasMarcadas) {

        cargarFormulario(model, categoriasMarcadas, plataformasMarcadas);

        ImagenVideojuego portada = null;
        ImagenVideojuego banner = null;
        List<ImagenVideojuego> capturas = new ArrayList<>();

        // Reparte las imagenes por tipo: una portada, un banner y varias capturas
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
