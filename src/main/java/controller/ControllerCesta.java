/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.cesta.CestaVaciaException;
import exception.cesta.JuegoNoComprableException;
import exception.cesta.VideojuegoNoDisponibleException;
import exception.pago.PagoNoCompletadoException;
import exception.pago.SinClavesDisponiblesException;
import model.ventas.Cesta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.CestaService;
import service.interfaces.PagoService;

import java.util.Locale;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerCesta {

    @Autowired
    private CestaService cestaService;

    @Autowired
    private PagoService pagoService;

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/tienda/cesta")
    public String verCesta(Authentication authentication, Model model) {
        Cesta cesta = cestaService.buscarOCrearCesta(authentication.getName());
        model.addAttribute("cesta", cesta);
        model.addAttribute("total", cestaService.calcularTotal(cesta));
        return "client/cesta";
    }

    @PostMapping("/tienda/cesta/anadir")
    public String anadirJuego(@RequestParam Long videojuegoId,
            Authentication authentication, RedirectAttributes flash, Locale locale) {
        try {
            cestaService.anadirVideojuego(authentication.getName(), videojuegoId);
        } catch (JuegoNoComprableException | VideojuegoNoDisponibleException e) {
            flash.addFlashAttribute("error", traducir(e.getMessage(), locale));
            return "redirect:/tienda/cesta";
        }
        // flash: guarda el mensaje para la pagina siguiente, porque el redirect pierde el Model
        flash.addFlashAttribute("mensaje", traducir("cesta.ok.anadido", locale));
        return "redirect:/tienda/cesta";
    }

    @PostMapping("/tienda/cesta/linea/{id}/quitar")
    public String quitarLinea(@PathVariable Long id, Authentication authentication) {
        cestaService.quitarLinea(authentication.getName(), id);
        return "redirect:/tienda/cesta";
    }

    @PostMapping("/tienda/cesta/vaciar")
    public String vaciarCesta(Authentication authentication) {
        cestaService.vaciarCesta(authentication.getName());
        return "redirect:/tienda/cesta";
    }

    @PostMapping("/tienda/cesta/tramitar")
    public String tramitarPedido(Authentication authentication, RedirectAttributes flash, Locale locale) {
        try {
            String urlPago = pagoService.iniciarPago(authentication.getName());
            // "redirect:" con una direccion externa: el navegador se va a la pagina de Stripe
            return "redirect:" + urlPago;
        } catch (CestaVaciaException | VideojuegoNoDisponibleException
                | SinClavesDisponiblesException | PagoNoCompletadoException e) {
            // Las de la cesta llevan una clave y se traducen; las de pago aun llevan el texto
            // en castellano, y traducir() lo devuelve tal cual al no existir esa clave
            flash.addFlashAttribute("error", traducir(e.getMessage(), locale));
            return "redirect:/tienda/cesta";
        }
    }

    // Las excepciones y los avisos llevan una clave de traduccion, no el texto: aqui se
    // convierte al idioma del usuario. Si la clave no existe se enseña la propia clave.
    private String traducir(String clave, Locale locale, Object... args) {
        return messageSource.getMessage(clave, args, clave, locale);
    }
}
