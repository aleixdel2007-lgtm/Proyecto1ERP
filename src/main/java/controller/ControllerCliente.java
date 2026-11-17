package controller;

import exception.cliente.ClienteConDatosAsociadosException;
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

@Controller
public class ControllerCliente {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private MessageSource messageSource;

    // Listado filtrable por texto, pais y fecha de alta
    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String buscar,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {
        // Primero los clientes filtrados, y solo las fotos de esos
        java.util.List<model.seguridad.Cliente> lista = clienteService.buscarFiltrado(buscar, pais, desde);
        model.addAttribute("clientes", lista);
        model.addAttribute("fotos", clienteService.listarImagenesPerfil(lista));
        model.addAttribute("paises", clienteService.listarPaises());
        // Filtros y pagina de vuelta, para que sigan marcados
        model.addAttribute("buscar", buscar);
        model.addAttribute("pais", pais);
        model.addAttribute("desde", desde);
        model.addAttribute("pagina", Math.max(0, pagina));
        return "gestorCliente/listadoClientes";
    }

    // Activa o desactiva la cuenta de un cliente
    @PostMapping("/clientes/{id}/estado")
    public String cambiarEstado(@PathVariable Long id, @RequestParam boolean activo,
            RedirectAttributes flash, Locale locale) {
        // "activo" llega del boton: true activa la cuenta, false la desactiva
        clienteService.cambiarEstado(id, activo);
        flash.addFlashAttribute("mensaje", messageSource.getMessage("clientes.ok.estadoActualizado", null, locale));
        return "redirect:/clientes";
    }

    @PostMapping("/clientes/{id}/eliminar")
    public String eliminarCliente(@PathVariable Long id, RedirectAttributes flash,
            Locale locale) {
        try {
            clienteService.eliminarCliente(id);
        } catch (ClienteConDatosAsociadosException e) {
            // No se borra un cliente con pedidos u otros datos asociados
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/clientes";
        }
        // El flash sobrevive al redirect, el Model no
        flash.addFlashAttribute("mensaje", messageSource.getMessage("clientes.ok.eliminado", null, locale));
        return "redirect:/clientes";
    }

    // Publica: la usa un cliente inactivo desde el pop-up del login
    @PostMapping("/solicitar-activacion")
    public String solicitarActivacion(@RequestParam String email) {
        // Registra la solicitud para que la vea el gestor de clientes en la campanita
        clienteService.solicitarActivacion(email);
        return "redirect:/login?solicitada";
    }

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }

}
