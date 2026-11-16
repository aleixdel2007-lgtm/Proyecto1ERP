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

    // El listado del panel, con los cuatro filtros y los cuatro contadores. La pagina se pasa
    // tal cual: el troceo lo hace la plantilla con subList, igual que en el panel de claves
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

        model.addAttribute("borradores", compraClavesService.contarPorEstado(EstadoCompra.BORRADOR));
        model.addAttribute("confirmadas", compraClavesService.contarPorEstado(EstadoCompra.CONFIRMADA));
        model.addAttribute("recibidas", compraClavesService.contarPorEstado(EstadoCompra.RECIBIDA));
        model.addAttribute("canceladas", compraClavesService.contarPorEstado(EstadoCompra.CANCELADA));

        model.addAttribute("proveedorId", proveedorId);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "compras/ordenes";
    }

    // Un alta es la misma ficha con una Compra vacia: sin id no puede tener lineas todavia
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

    // El alta y la edicion de la cabecera. El empleado que firma sale de quien esta dentro
    // de la aplicacion, no de un desplegable
    @PostMapping("/ordenes/guardar")
    public String guardarOrden(@ModelAttribute("compra") Compra compra,
            @RequestParam(required = false) Long proveedorId,
            Authentication authentication,
            Model model,
            RedirectAttributes flash,
            Locale locale) {

        boolean esAlta = compra.getId() == null;

        try {
            Compra guardada = compraClavesService.guardarCompra(
                    compra, proveedorId, authentication.getName());

            flash.addFlashAttribute("mensaje",
                    esAlta ? "Orden creada como borrador." : "Orden guardada.");

            return "redirect:/compras/ordenes/" + guardada.getId();

        } catch (EstadoCompraInvalidoException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/compras/ordenes/" + compra.getId();

        } catch (IllegalArgumentException e) {

            // En una edicion se recarga la ficha de verdad, que del formulario solo viene la
            // cabecera. En un alta no: se devuelve la pantalla con lo que ya habia escrito,
            // para no obligar a teclearlo otra vez
            if (!esAlta) {
                flash.addFlashAttribute("error", traducir(e, locale));
                return "redirect:/compras/ordenes/" + compra.getId();
            }

            model.addAttribute("error", traducir(e, locale));
            return prepararFicha(compra, model);
        }
    }

    // Cantidad vacia es ILIMITADA, asi que llega como Integer y puede ser null
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

    // La pantalla de recepcion solo tiene sentido con la orden CONFIRMADA. El servicio lo
    // vuelve a comprobar al recibir; esto es para no ensenar un formulario que no va a colar
    @GetMapping("/ordenes/{id}/recepcion")
    public String formularioRecepcion(@PathVariable Long id, Model model,
            RedirectAttributes flash, Locale locale) {

        Compra compra = compraClavesService.buscarCompraXId(id);

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

    // Las cantidades vienen en dos listas paralelas, una pareja por fila
    @PostMapping("/ordenes/{id}/recepcion")
    public String recibirOrden(@PathVariable Long id,
            @RequestParam(name = "lineaId", required = false) List<Long> lineaIds,
            @RequestParam(name = "recibida", required = false) List<Integer> recibidas,
            @RequestParam(required = false) String numeroAlbaran,
            @RequestParam(required = false) String observaciones,
            RedirectAttributes flash,
            Locale locale) {

        try {
            int claves = compraClavesService.recibirCompra(
                    id, lineaIds, recibidas, numeroAlbaran, observaciones);

            flash.addFlashAttribute("mensaje",
                    "Orden recibida. Se han creado " + claves + " claves DISPONIBLES.");

        } catch (EstadoCompraInvalidoException | LineaCompraInvalidaException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/compras/ordenes/" + id + "/recepcion";
        }

        return "redirect:/compras/ordenes/" + id;
    }

    // La cabecera se edita mientras es BORRADOR; las lineas, mientras el proveedor todavia
    // puede cambiar el pedido. Una orden sin id no tiene lineas que tocar
    private String prepararFicha(Compra compra, Model model) {

        model.addAttribute("compra", compra);
        model.addAttribute("proveedores", compraClavesService.listarProveedoresActivos());
        model.addAttribute("videojuegos", compraClavesService.listarVideojuegosPropios());

        model.addAttribute("editable", compra.getEstado() == EstadoCompra.BORRADOR);
        model.addAttribute("lineasEditables", compra.getId() != null
                && (compra.getEstado() == EstadoCompra.BORRADOR
                    || compra.getEstado() == EstadoCompra.CONFIRMADA));

        return "compras/orden";
    }

    // Igual que en los demas controladores, pero el estado invalido lleva dos argumentos
    // dentro del texto: la accion y el estado en el que esta la orden
    private String traducir(RuntimeException e, Locale locale) {

        Object[] argumentos = null;

        if (e instanceof EstadoCompraInvalidoException estadoInvalido) {
            argumentos = new Object[]{estadoInvalido.getAccion(), estadoInvalido.getEstado()};
        }

        return messageSource.getMessage(e.getMessage(), argumentos, e.getMessage(), locale);
    }
}
