package controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import model.catalogo.ImagenVideojuego;
import model.catalogo.TipoImagen;
import model.catalogo.Videojuego;
import model.valoracion.Valoracion;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.interfaces.TiendaService;
import service.interfaces.ValoracionService;

@Controller
@RequestMapping("/tienda")
@RequiredArgsConstructor
public class ControllerTienda {

    private final TiendaService tiendaService;
    private final ValoracionService valoracionService;

    @GetMapping
    public String listarCatalogo(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long plataformaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        return cargarListado(model, "catalogo", "/tienda",
                tiendaService.listarCatalogo(texto, categoriaId, plataformaId, precioMin, precioMax, orden),
                texto, categoriaId, plataformaId, precioMin, precioMax, orden, pagina);
    }

    @GetMapping("/ofertas")
    public String listarOfertas(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long plataformaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        return cargarListado(model, "ofertas", "/tienda/ofertas",
                tiendaService.listarOfertas(texto, categoriaId, plataformaId, precioMin, precioMax, orden),
                texto, categoriaId, plataformaId, precioMin, precioMax, orden, pagina);
    }

    @GetMapping("/colaboraciones")
    public String listarColaboraciones(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) Long plataformaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax,
            @RequestParam(required = false) String orden,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        return cargarListado(model, "colaboraciones", "/tienda/colaboraciones",
                tiendaService.listarColaboraciones(texto, categoriaId, plataformaId, precioMin, precioMax, orden),
                texto, categoriaId, plataformaId, precioMin, precioMax, orden, pagina);
    }

    private String cargarListado(Model model, String vista, String ruta, List<Videojuego> videojuegos,
                                 String texto, Long categoriaId, Long plataformaId,
                                 BigDecimal precioMin, BigDecimal precioMax, String orden, int pagina) {

        model.addAttribute("videojuegos", videojuegos);
        model.addAttribute("portadas", tiendaService.listarPortadas(videojuegos));
        model.addAttribute("vista", vista);
        model.addAttribute("ruta", ruta);

        model.addAttribute("categorias", tiendaService.listarCategorias());
        model.addAttribute("plataformas", tiendaService.listarPlataformas());

        model.addAttribute("texto", texto);
        model.addAttribute("categoriaId", categoriaId);
        model.addAttribute("plataformaId", plataformaId);
        model.addAttribute("precioMin", precioMin);
        model.addAttribute("precioMax", precioMax);
        model.addAttribute("orden", orden);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "client/tienda";
    }

    @GetMapping("/juego/{id}")
    public String verFicha(@PathVariable Long id, Model model, Authentication authentication) {

        Videojuego videojuego = tiendaService.buscarVideojuegoActivoXId(id);

        model.addAttribute("videojuego", videojuego);
        model.addAttribute("vistaPrevia", false);
        model.addAttribute("vista", videojuego.getColaborador() != null ? "colaboraciones" : "catalogo");
        model.addAttribute("tieneDescarga", tiendaService.tieneDescarga(id));
        model.addAttribute("valoraciones", valoracionService.listarVisiblesPorVideojuego(id));
        model.addAttribute("yaValorado", valoracionService.haValorado(authentication.getName(), id));

        cargarImagenes(model, id);

        return "client/ficha";
    }

    private void cargarImagenes(Model model, Long id) {

        ImagenVideojuego portada = null;
        ImagenVideojuego banner = null;
        List<ImagenVideojuego> capturas = new ArrayList<>();

        for (ImagenVideojuego imagen : tiendaService.listarImagenes(id)) {

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
}
