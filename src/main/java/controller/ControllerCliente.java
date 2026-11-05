/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.ClienteConDatosAsociadosException;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.ClienteService;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerCliente {

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String buscar,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        model.addAttribute("clientes", clienteService.buscarFiltrado(buscar, pais, desde));
        model.addAttribute("paises", clienteService.listarPaises());
        model.addAttribute("buscar", buscar);
        model.addAttribute("pais", pais);
        model.addAttribute("desde", desde);
        model.addAttribute("pagina", Math.max(0, pagina));
        return "gestorCliente/listadoClientes";
    }

    @PostMapping("/clientes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes flash) {
        clienteService.cambiarEstado(id, activo);
        flash.addFlashAttribute("mensaje", "Estado del cliente actualizado");
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes flash) {
        try {
            clienteService.eliminarCliente(id);
        } catch (ClienteConDatosAsociadosException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/clientes";
        }
        // flash: guarda el mensaje para la pagina siguiente, porque el redirect pierde el Model
        flash.addFlashAttribute("mensaje", "Cliente eliminado");
        return "redirect:/clientes";
    }

    // publica: la usa un cliente inactivo desde el pop-up del login
    @PostMapping("/solicitar-activacion")
    public String solicitarActivacion(@RequestParam String email) {
        clienteService.solicitarActivacion(email);
        return "redirect:/login?solicitada";
    }

}
