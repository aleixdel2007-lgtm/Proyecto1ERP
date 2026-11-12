/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.pago.PagoNoCompletadoException;
import exception.pago.SinClavesDisponiblesException;
import exception.pedido.PedidoNoEncontradoException;
import model.ventas.Pedido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.PagoService;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerPago {

    @Autowired
    private PagoService pagoService;

    // Stripe devuelve aqui al cliente cuando ha pagado
    @GetMapping("/tienda/pago/exito")
    public String pagoRealizado(@RequestParam("session_id") String sessionId,
            Authentication authentication, RedirectAttributes flash) {
        try {
            Pedido pedido = pagoService.confirmarPago(authentication.getName(), sessionId);
            flash.addFlashAttribute("mensaje",
                    "Pago realizado. Pedido " + pedido.getNumero() + ": ya tienes tus juegos en la biblioteca");
            return "redirect:/tienda/biblioteca";
        } catch (PagoNoCompletadoException | PedidoNoEncontradoException | SinClavesDisponiblesException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/tienda/cesta";
        }
    }

    // Y aqui si ha pulsado "volver" en la pagina de Stripe
    @GetMapping("/tienda/pago/cancelado")
    public String pagoCancelado(RedirectAttributes flash) {
        flash.addFlashAttribute("error", "Has cancelado el pago. Tu pedido sigue pendiente");
        return "redirect:/tienda/cesta";
    }
}
