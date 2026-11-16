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
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.PagoService;

import java.util.Locale;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerPago {

    @Autowired
    private PagoService pagoService;

    @Autowired
    private MessageSource messageSource;

    // Stripe devuelve aqui al cliente cuando ha pagado
    @GetMapping("/tienda/pago/exito")
    public String pagoRealizado(@RequestParam("session_id") String sessionId,
            Authentication authentication, RedirectAttributes flash, Locale locale) {
        try {
            Pedido pedido = pagoService.confirmarPago(authentication.getName(), sessionId);
            flash.addFlashAttribute("mensaje", messageSource.getMessage("pago.ok.realizado",
                    new Object[]{pedido.getNumero()}, locale));
            return "redirect:/tienda/biblioteca";
        } catch (PagoNoCompletadoException | PedidoNoEncontradoException | SinClavesDisponiblesException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/tienda/cesta";
        }
    }

    // Y aqui si ha pulsado "volver" en la pagina de Stripe
    @GetMapping("/tienda/pago/cancelado")
    public String pagoCancelado(RedirectAttributes flash, Locale locale) {
        flash.addFlashAttribute("error",
                messageSource.getMessage("pago.error.cancelado", null, locale));
        return "redirect:/tienda/cesta";
    }

    // El mensaje de la excepcion es la clave de traduccion. La de "sin claves" ademas
    // lleva el titulo del juego, que va como parametro {0} del texto.
    private String traducir(RuntimeException e, Locale locale) {
        Object[] args = e instanceof SinClavesDisponiblesException sinClaves
                ? new Object[]{sinClaves.getTitulo()} : null;
        return messageSource.getMessage(e.getMessage(), args, e.getMessage(), locale);
    }
}
