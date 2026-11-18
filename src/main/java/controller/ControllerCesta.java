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
        // Si el usuario aun no tiene cesta se le crea una vacia
        Cesta cesta = cestaService.buscarOCrearCesta(authentication.getName());
        model.addAttribute("cesta", cesta);
        // El total lo calcula el servicio, la vista no suma
        model.addAttribute("total", cestaService.calcularTotal(cesta));
        return "client/cesta";
    }

    @PostMapping("/tienda/cesta/anadir")
    public String anadirJuego(@RequestParam Long videojuegoId,
            Authentication authentication, RedirectAttributes flash, Locale locale) {
        try {
            // La cesta es la del usuario de la sesion, nunca la de un id recibido
            cestaService.anadirVideojuego(authentication.getName(), videojuegoId);
        } catch (JuegoNoComprableException | VideojuegoNoDisponibleException e) {
            flash.addFlashAttribute("error", traducir(e.getMessage(), locale));
            return "redirect:/tienda/cesta";
        }
        // El flash sobrevive al redirect, el Model no
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

    // Empieza el pago: crea la sesion de Stripe y manda al usuario alli
    @PostMapping("/tienda/cesta/tramitar")
    public String tramitarPedido(Authentication authentication, RedirectAttributes flash, Locale locale) {
        try {
            String urlPago = pagoService.iniciarPago(authentication.getName());
            // Redirect a una URL externa (Stripe)
            return "redirect:" + urlPago;
        } catch (CestaVaciaException | VideojuegoNoDisponibleException
                | SinClavesDisponiblesException | PagoNoCompletadoException e) {
            flash.addFlashAttribute("error", mensajeDeError(e, locale));
            return "redirect:/tienda/cesta";
        }
    }

    // La excepcion de "sin claves" necesita ademas el titulo del juego como argumento
    private String mensajeDeError(RuntimeException e, Locale locale) {
        if (e instanceof SinClavesDisponiblesException sinClaves) {
            return traducir(sinClaves.getMessage(), locale, sinClaves.getTitulo());
        }
        return traducir(e.getMessage(), locale);
    }

    // Convierte una clave i18n al idioma del usuario; si no existe devuelve la propia clave
    private String traducir(String clave, Locale locale, Object... args) {
        return messageSource.getMessage(clave, args, clave, locale);
    }
}
