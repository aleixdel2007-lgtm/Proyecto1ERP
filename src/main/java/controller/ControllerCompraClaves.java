package controller;

import exception.compra.EstadoCompraInvalidoException;
import exception.compra.LineaCompraInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Compra;
import model.aprovisionamiento.EstadoCompra;
import org.springframework.context.MessageSource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.CompraClavesService;

@Controller
@RequestMapping("/compras")
@RequiredArgsConstructor
public class ControllerCompraClaves {

    private final CompraClavesService compraClavesService;
    private final MessageSource messageSource;

    // Listado de ordenes de compra con cuatro filtros y un contador por estado
    @GetMapping("/ordenes")
    public String listarOrdenes(
            @RequestParam(required = false) Long proveedorId,
            @RequestParam(required = false) EstadoCompra estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        model.addAttribute("ordenes", compraClavesService.listarCompras(proveedorId, estado, desde, hasta));
        model.addAttribute("proveedores", compraClavesService.listarProveedoresActivos());
        model.addAttribute("estados", EstadoCompra.values());

        // Contadores de la cabecera
        model.addAttribute("borradores", compraClavesService.contarPorEstado(EstadoCompra.BORRADOR));
        model.addAttribute("confirmadas", compraClavesService.contarPorEstado(EstadoCompra.CONFIRMADA));
        model.addAttribute("recibidas", compraClavesService.contarPorEstado(EstadoCompra.RECIBIDA));
        model.addAttribute("canceladas", compraClavesService.contarPorEstado(EstadoCompra.CANCELADA));

        // Filtros y pagina de vuelta, para que sigan marcados
        model.addAttribute("proveedorId", proveedorId);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "compras/ordenes";
    }

    // Alta: la misma ficha con una orden vacia, que aun no puede tener lineas
    @GetMapping("/ordenes/nueva")
    public String formularioNuevaOrden(Model model) {

        Compra compra = new Compra();
        compra.setFecha(LocalDate.now());

        return prepararFicha(compra, model);
    }

    @GetMapping("/ordenes/{id}")
    public String verOrden(@PathVariable Long id, Model model) {
        return prepararFicha(compraClavesService.buscarCompraXId(id), model);
    }

    // Alta y edicion de la cabecera; el empleado que firma sale de la sesion
    @PostMapping("/ordenes/guardar")
    public String guardarOrden(@ModelAttribute("compra") Compra compra,
            @RequestParam(required = false) Long proveedorId,
            Authentication authentication,
            Model model,
            RedirectAttributes flash,
            Locale locale) {

        // Sin id es una orden nueva; cambia que pantalla se devuelve si hay error
        boolean esAlta = compra.getId() == null;

        try {
            // authentication.getName() es el email del empleado que firma la orden
            Compra guardada = compraClavesService.guardarCompra(
                    compra, proveedorId, authentication.getName());

            flash.addFlashAttribute("mensaje",
                    esAlta ? "Orden creada como borrador." : "Orden guardada.");

            return "redirect:/compras/ordenes/" + guardada.getId();

        } catch (EstadoCompraInvalidoException e) {
            // La orden ya no esta en un estado que permita editarla
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/compras/ordenes/" + compra.getId();

        } catch (IllegalArgumentException e) {

            // En edicion se recarga la ficha entera, porque el formulario solo trae la cabecera
            if (!esAlta) {
                flash.addFlashAttribute("error", traducir(e, locale));
                return "redirect:/compras/ordenes/" + compra.getId();
            }

            // En alta se devuelve la pantalla con lo ya escrito
            model.addAttribute("error", traducir(e, locale));
            return prepararFicha(compra, model);
        }
    }

    // Cantidad vacia significa ilimitada, por eso es Integer y puede ser null
    @PostMapping("/ordenes/{id}/lineas")
    public String anadirLinea(@PathVariable Long id,
            @RequestParam Long videojuegoId,
            @RequestParam(required = false) Integer cantidad,
            @RequestParam(required = false) BigDecimal costeUnitario,
            RedirectAttributes flash,
            Locale locale) {

        try {
            compraClavesService.anadirLinea(id, videojuegoId, cantidad, costeUnitario);
            flash.addFlashAttribute("mensaje", "Linea anadida.");

        } catch (EstadoCompraInvalidoException | LineaCompraInvalidaException
                | IllegalArgumentException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/compras/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/lineas/{lineaId}/eliminar")
    public String quitarLinea(@PathVariable Long id,
            @PathVariable Long lineaId,
            RedirectAttributes flash,
            Locale locale) {

        try {
            compraClavesService.quitarLinea(id, lineaId);
            flash.addFlashAttribute("mensaje", "Linea quitada.");

        } catch (EstadoCompraInvalidoException | LineaCompraInvalidaException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/compras/ordenes/" + id;
    }

    // BORRADOR -> CONFIRMADA
    @PostMapping("/ordenes/{id}/confirmar")
    public String confirmarOrden(@PathVariable Long id, RedirectAttributes flash, Locale locale) {

        try {
            compraClavesService.confirmarCompra(id);
            flash.addFlashAttribute("mensaje", "Orden confirmada. Ya se puede recibir.");

        } catch (EstadoCompraInvalidoException | LineaCompraInvalidaException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/compras/ordenes/" + id;
    }

    @PostMapping("/ordenes/{id}/cancelar")
    public String cancelarOrden(@PathVariable Long id, RedirectAttributes flash, Locale locale) {

        try {
            compraClavesService.cancelarCompra(id);
            flash.addFlashAttribute("mensaje", "Orden cancelada.");

        } catch (EstadoCompraInvalidoException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/compras/ordenes/" + id;
    }

    // Pantalla de recepcion: solo con la orden CONFIRMADA
    @GetMapping("/ordenes/{id}/recepcion")
    public String formularioRecepcion(@PathVariable Long id, Model model,
            RedirectAttributes flash, Locale locale) {

        Compra compra = compraClavesService.buscarCompraXId(id);

        // El servicio lo vuelve a comprobar al recibir; aqui se evita ensenar un formulario inutil
        if (compra.getEstado() != EstadoCompra.CONFIRMADA) {
            flash.addFlashAttribute("error", messageSource.getMessage(
                    "compras.error.estadoInvalido",
                    new Object[]{"recibir", compra.getEstado()},
                    "compras.error.estadoInvalido", locale));

            return "redirect:/compras/ordenes/" + id;
        }

        model.addAttribute("compra", compra);

        return "compras/recepcion";
    }

    // Recibe la mercancia y crea las claves; lineaId y recibida son listas paralelas, una pareja por fila
    @PostMapping("/ordenes/{id}/recepcion")
    public String recibirOrden(@PathVariable Long id,
            @RequestParam(name = "lineaId", required = false) List<Long> lineaIds,
            @RequestParam(name = "recibida", required = false) List<Integer> recibidas,
            @RequestParam(required = false) String numeroAlbaran,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes flash,
            Locale locale) {

        try {
            // Devuelve cuantas claves se han creado a partir de lo recibido
            int claves = compraClavesService.recibirCompra(
                    id, lineaIds, recibidas, numeroAlbaran, observaciones);

            flash.addFlashAttribute("mensaje",
                    "Orden recibida. Se han creado " + claves + " claves DISPONIBLES.");

        } catch (EstadoCompraInvalidoException | LineaCompraInvalidaException e) {
            // Se vuelve a la recepcion para corregir las cantidades
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/compras/ordenes/" + id + "/recepcion";
        }

        return "redirect:/compras/ordenes/" + id;
    }

    // Prepara la ficha de la orden y decide que partes se pueden editar
    private String prepararFicha(Compra compra, Model model) {

        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", compraClavesService.listarProveedoresActivos());
        model.addAttribute("videojuegos", compraClavesService.listarVideojuegosPropios());

        // La cabecera solo se edita en BORRADOR; las lineas tambien en CONFIRMADA y si ya existe la orden
        model.addAttribute("editable", compra.getEstado() == EstadoCompra.BORRADOR);
        model.addAttribute("lineasEditables", compra.getId() != null
                && (compra.getEstado() == EstadoCompra.BORRADOR
                    || compra.getEstado() == EstadoCompra.CONFIRMADA));

        return "compras/orden";
    }

    // Traduce el mensaje de la excepcion; el estado invalido lleva dos argumentos (accion y estado)
    private String traducir(RuntimeException e, Locale locale) {

        Object[] argumentos = null;

        // La accion es una clave i18n y el estado un enum, y los dos se traducen
        if (e instanceof EstadoCompraInvalidoException estadoInvalido) {
            argumentos = new Object[]{
                messageSource.getMessage(estadoInvalido.getAccion(), null,
                        estadoInvalido.getAccion(), locale),
                messageSource.getMessage("estadoCompra." + estadoInvalido.getEstado(), null,
                        String.valueOf(estadoInvalido.getEstado()), locale)};
        }

        return messageSource.getMessage(e.getMessage(), argumentos, e.getMessage(), locale);
    }
}
