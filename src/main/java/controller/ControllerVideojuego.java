package controller;

import exception.imagen.ImagenInvalidaException;
import exception.precio.PrecioDescuentoInvalidoException;
import exception.precio.PrecioInvalidoException;
import exception.videojuego.ProveedorObligatorioException;
import exception.videojuego.VideojuegoConReferenciasException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import model.catalogo.Categoria;
import model.catalogo.ImagenVideojuego;
import model.catalogo.Plataforma;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
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
import service.interfaces.VideojuegoService;

@Controller
public class ControllerVideojuego {

    @Autowired
    private VideojuegoService videojuegoService;

    @GetMapping("/catalogo/videojuegos")
    public String listarVideojuegos(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long plataformaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        List<Videojuego> videojuegos = videojuegoService.listarVideojuegos(
                texto, categoriaId, plataformaId, precioMin, precioMax, activo);

        model.addAttribute("videojuegos", videojuegos);
        model.addAttribute("portadas", videojuegoService.listarPortadas(videojuegos));
        model.addAttribute("categorias", videojuegoService.listarCategorias());
        model.addAttribute("plataformas", videojuegoService.listarPlataformas());

        model.addAttribute("texto", texto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("plataformaId", plataformaId);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("activo", activo);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "catalogo/videojuegos";
    }

    @GetMapping("/catalogo/videojuegos/nuevo")
    public String nuevoVideojuego(Model model) {

        model.addAttribute("videojuego", new Videojuego());
        cargarCatalogos(model, null, new HashSet<>(), new HashSet<>());

        return "catalogo/videojuego";
    }

    @GetMapping("/catalogo/videojuegos/editar/{id}")
    public String editarVideojuego(@PathVariable Long id, Model model) {

        Videojuego videojuego = videojuegoService.buscarVideojuegoXId(id);
        model.addAttribute("videojuego", videojuego);

        cargarCatalogos(model, idDelProveedor(videojuego),
                idsDeCategorias(videojuego), idsDePlataformas(videojuego));
        cargarImagenes(model, id);

        return "catalogo/videojuego";
    }

    @PostMapping("/catalogo/videojuegos/guardar")
    public String guardarVideojuego(
            @ModelAttribute("videojuego") Videojuego videojuego,
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) List<Long> categoriaIds,
            @RequestParam(required = false) List<Long> plataformaIds,
            Model model,
            RedirectAttributes flash) {

        try {
            videojuegoService.guardarVideojuego(videojuego, proveedorId, categoriaIds, plataformaIds);

        } catch (ProveedorObligatorioException | PrecioInvalidoException
                | PrecioDescuentoInvalidoException | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            cargarCatalogos(model, proveedorId, conjuntoDe(categoriaIds), conjuntoDe(plataformaIds));

            return "catalogo/videojuego";
        }

        flash.addFlashAttribute("mensaje", "Videojuego guardado correctamente");

        return "redirect:/catalogo/videojuegos";
    }

    @PostMapping("/catalogo/videojuegos/eliminar/{id}")
    public String eliminarVideojuego(@PathVariable Long id, RedirectAttributes flash) {

        try {
            videojuegoService.eliminarVideojuego(id);

        } catch (VideojuegoConReferenciasException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo/videojuegos";
        }

        flash.addFlashAttribute("mensaje", "Videojuego eliminado correctamente");

        return "redirect:/catalogo/videojuegos";
    }

    @GetMapping("/catalogo/videojuegos/{id}/vista-previa")
    public String vistaPreviaVideojuego(@PathVariable Long id, Model model) {

        model.addAttribute("videojuego", videojuegoService.buscarVideojuegoXId(id));
        model.addAttribute("vistaPrevia", true);
        cargarImagenes(model, id);

        return "client/ficha";
    }

    @PostMapping("/catalogo/videojuegos/{id}/imagenes")
    public String guardarImagen(@PathVariable Long id,
                                @RequestParam("archivo") MultipartFile archivo,
                                @RequestParam("tipo") TipoImagen tipo,
                                @RequestParam(value = "textoAlternativo", required = false) String textoAlternativo,
                                RedirectAttributes flash) {

        try {
            videojuegoService.guardarImagen(id, archivo, tipo, textoAlternativo);
            flash.addFlashAttribute("mensaje", "Imagen anadida correctamente");

        } catch (ImagenInvalidaException | IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/catalogo/videojuegos/editar/" + id;
    }

    @PostMapping("/catalogo/videojuegos/{id}/imagenes/{imagenId}/eliminar")
    public String eliminarImagen(@PathVariable Long id, @PathVariable Long imagenId,
                                 RedirectAttributes flash) {

        videojuegoService.eliminarImagen(id, imagenId);
        flash.addFlashAttribute("mensaje", "Imagen eliminada correctamente");

        return "redirect:/catalogo/videojuegos/editar/" + id;
    }

    private void cargarImagenes(Model model, Long id) {

        ImagenVideojuego portada = null;
        ImagenVideojuego banner = null;
        List<ImagenVideojuego> capturas = new ArrayList<>();

        for (ImagenVideojuego imagen : videojuegoService.listarImagenes(id)) {

            if (imagen.getTipo() == TipoImagen.PORTADA && portada == null) {
                portada = imagen;

            } else if (imagen.getTipo() == TipoImagen.BANNER && banner == null) {
                banner = imagen;

            } else if (imagen.getTipo() == TipoImagen.CAPTURA) {
                capturas.add(imagen);
            }
        }

        model.addAttribute("portada", portada);
        model.addAttribute("banner", banner);
        model.addAttribute("capturas", capturas);
    }

    private void cargarCatalogos(Model model, Long proveedorId,
                                 Set<Long> categoriasMarcadas, Set<Long> plataformasMarcadas) {

        model.addAttribute("proveedores", videojuegoService.listarProveedoresActivos());
        model.addAttribute("categorias", videojuegoService.listarCategorias());
        model.addAttribute("plataformas", videojuegoService.listarPlataformas());

        model.addAttribute("proveedorId", proveedorId);
        model.addAttribute("categoriasMarcadas", categoriasMarcadas);
        model.addAttribute("plataformasMarcadas", plataformasMarcadas);
    }

    private Long idDelProveedor(Videojuego videojuego) {
        return videojuego.getProveedor() == null ? null : videojuego.getProveedor().getId();
    }

    private Set<Long> idsDeCategorias(Videojuego videojuego) {

        Set<Long> ids = new HashSet<>();

        for (Categoria categoria : videojuego.getCategorias()) {
            ids.add(categoria.getId());
        }
        return ids;
    }

    private Set<Long> idsDePlataformas(Videojuego videojuego) {

        Set<Long> ids = new HashSet<>();

        for (Plataforma plataforma : videojuego.getPlataformas()) {
            ids.add(plataforma.getId());
        }
        return ids;
    }

    private Set<Long> conjuntoDe(List<Long> ids) {
        return ids == null ? new HashSet<>() : new HashSet<>(ids);
    }
}
