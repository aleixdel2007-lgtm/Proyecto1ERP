package controller;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import model.ventas.EstadoPedido;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.interfaces.FacturasService;
import service.interfaces.PedidoService;

@Controller
@RequestMapping("/ventas")
@RequiredArgsConstructor
public class ControllerVentas {

    private final PedidoService pedidoService;
    private final FacturasService facturasService;

    // Listado de pedidos con filtros y un contador por estado
    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        model.addAttribute("pedidos", pedidoService.listarPedidos(texto, estado, desde, hasta));
        model.addAttribute("estados", EstadoPedido.values());

        // Contadores de la cabecera
        model.addAttribute("pagados", pedidoService.contarPorEstado(EstadoPedido.PAGADO));
        model.addAttribute("pendientes", pedidoService.contarPorEstado(EstadoPedido.PENDIENTE_PAGO));
        model.addAttribute("reembolsados", pedidoService.contarPorEstado(EstadoPedido.REEMBOLSADO));

        // Filtros de vuelta, para que sigan marcados
        model.addAttribute("texto", texto);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "ventas/pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String detallePedido(@PathVariable Long id, Model model) {

        model.addAttribute("pedido", pedidoService.buscarPedidoXId(id));
        return "ventas/detallePedido";
    }

    @GetMapping("/facturas")
    public String listarFacturas(
            @RequestParam(required = false) String texto,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        model.addAttribute("facturas", facturasService.listarFiltrado(texto));

        model.addAttribute("texto", texto);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "ventas/facturas";
    }

    // Genera el PDF de la factura; inline lo abre en el navegador en vez de descargarlo
    @GetMapping("/facturas/{id}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable Long id) {

        // El servicio devuelve el PDF como bytes y se envia tal cual en la respuesta
        byte[] pdf = facturasService.generarPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
