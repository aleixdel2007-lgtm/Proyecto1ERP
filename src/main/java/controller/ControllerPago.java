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
                    new Object[]{String.valueOf(pedido.getNumero())}, locale));
            return "redirect:/tienda/biblioteca";
        } catch (PagoNoCompletadoException | PedidoNoEncontradoException | SinClavesDisponiblesException e) {
            flash.addFlashAttribute("error", mensajeDeError(e, locale));
            return "redirect:/tienda/cesta";
        }
    }

    // Y aqui si ha pulsado "volver" en la pagina de Stripe
    @GetMapping("/tienda/pago/cancelado")
    public String pagoCancelado(RedirectAttributes flash, Locale locale) {
        flash.addFlashAttribute("error", messageSource.getMessage("pago.error.cancelado", null, locale));
        return "redirect:/tienda/cesta";
    }

    // Las excepciones llevan una clave de traduccion como mensaje. Dos necesitan ademas un
    // dato para el {0} del texto: el titulo del juego y el id del pedido.
    private String mensajeDeError(RuntimeException e, Locale locale) {
        if (e instanceof SinClavesDisponiblesException sinClaves) {
            return messageSource.getMessage(sinClaves.getMessage(),
                    new Object[]{sinClaves.getTitulo()}, locale);
        }
        if (e instanceof PedidoNoEncontradoException noEncontrado) {
            return messageSource.getMessage("pago.error.pedidoNoExiste",
                    new Object[]{String.valueOf(noEncontrado.getId())}, locale);
        }
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }
}
