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

    @GetMapping("/pedidos")
    public String listarPedidos(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) EstadoPedido estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            Model model) {

        model.addAttribute("pedidos", pedidoService.listarPedidos(texto, estado, desde, hasta));
        model.addAttribute("estados", EstadoPedido.values());

        model.addAttribute("pagados", pedidoService.contarPorEstado(EstadoPedido.PAGADO));
        model.addAttribute("pendientes", pedidoService.contarPorEstado(EstadoPedido.PENDIENTE_PAGO));
        model.addAttribute("reembolsados", pedidoService.contarPorEstado(EstadoPedido.REEMBOLSADO));

        model.addAttribute("texto", texto);
        model.addAttribute("estado", estado);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        return "ventas/pedidos";
    }

    @GetMapping("/pedido/{id}")
    public String detallePedido(@PathVariable Long id, Model model) {

        model.addAttribute("pedido", pedidoService.buscarPedidoXId(id));
        return "ventas/detallePedido";
    }

    @GetMapping("/facturas")
    public String listarFacturas(@RequestParam(required = false) String texto, Model model) {

        model.addAttribute("facturas", facturasService.listarFiltrado(texto));
        return "ventas/facturas";
    }

    @GetMapping("/facturas/{id}/pdf")
    public ResponseEntity<byte[]> descargarFacturaPdf(@PathVariable Long id) {

        byte[] pdf = facturasService.generarPdf(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"factura-" + id + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
