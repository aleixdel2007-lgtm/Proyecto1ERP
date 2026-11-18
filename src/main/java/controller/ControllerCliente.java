/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import exception.cliente.ClienteConDatosAsociadosException;
import exception.cliente.MotivoInactivacionObligatorioException;
import java.time.LocalDate;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String buscar,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        java.util.List<model.seguridad.Cliente> lista = clienteService.buscarFiltrado(buscar, pais, desde);
        model.addAttribute("clientes", lista);
        model.addAttribute("fotos", clienteService.listarImagenesPerfil(lista));
        model.addAttribute("paises", clienteService.listarPaises());
        model.addAttribute("buscar", buscar);
        model.addAttribute("pais", pais);
        model.addAttribute("desde", desde);
        model.addAttribute("pagina", Math.max(0, pagina));
        return "gestorCliente/listadoClientes";
    }

    // Activa o inactiva un cliente, segun el valor de activo. El motivo solo viaja al
    // inactivar, porque lo escribe el gestor en el pop-up; al activar no se pide nada y
    // llega nulo. Quien decide si es obligatorio es el servicio.
    @PostMapping("/clientes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            @RequestParam(required = false) String motivo,
            RedirectAttributes flash, Locale locale) {
        try {
            clienteService.cambiarEstado(id, activo, motivo);
        } catch (MotivoInactivacionObligatorioException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/clientes";
        }
        flash.addFlashAttribute("mensaje", messageSource.getMessage("clientes.ok.estadoActualizado", null, locale));
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes flash,
            Locale locale) {
        try {
            clienteService.eliminarCliente(id);
        } catch (ClienteConDatosAsociadosException e) {
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/clientes";
        }
        // flash: guarda el mensaje para la pagina siguiente, porque el redirect pierde el Model
        flash.addFlashAttribute("mensaje", messageSource.getMessage("clientes.ok.eliminado", null, locale));
        return "redirect:/clientes";
    }

    // publica: la usa un cliente inactivo desde el pop-up del login
    @PostMapping("/solicitar-activacion")
    public String solicitarActivacion(@RequestParam String email) {
        clienteService.solicitarActivacion(email);
        return "redirect:/login?solicitada";
    }

    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }

}
