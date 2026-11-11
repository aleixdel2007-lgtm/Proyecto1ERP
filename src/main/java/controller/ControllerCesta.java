/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.cesta.JuegoNoComprableException;
import exception.cesta.VideojuegoNoDisponibleException;
import model.ventas.Cesta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.CestaService;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerCesta {

    @Autowired
    private CestaService cestaService;

    @GetMapping("/tienda/cesta")
    public String verCesta(Authentication authentication, Model model) {
        Cesta cesta = cestaService.buscarOCrearCesta(authentication.getName());
        model.addAttribute("cesta", cesta);
        model.addAttribute("total", cestaService.calcularTotal(cesta));
        return "client/cesta";
    }

    @PostMapping("/tienda/cesta/anadir")
    public String anadirJuego(@RequestParam Long videojuegoId,
            Authentication authentication, RedirectAttributes flash) {
        try {
            cestaService.anadirVideojuego(authentication.getName(), videojuegoId);
        } catch (JuegoNoComprableException | VideojuegoNoDisponibleException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/tienda/cesta";
        }
        // flash: guarda el mensaje para la pagina siguiente, porque el redirect pierde el Model
        flash.addFlashAttribute("mensaje", "Juego añadido a la cesta");
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

}
