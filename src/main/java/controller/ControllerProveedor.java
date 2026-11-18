package controller;

import exception.cif.CifDuplicadoException;
import exception.cif.CifInvalidoException;
import exception.proveedor.ProveedorConReferenciasException;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.ProveedorService;

import java.util.Locale;

@Controller
@RequestMapping("/compras/proveedores")
@RequiredArgsConstructor
public class ControllerProveedor {

    private final ProveedorService proveedorService;
    private final MessageSource messageSource;

    // Listado filtrable por texto, pais y estado
    @GetMapping
    public String listarProveedores(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        model.addAttribute("proveedores", proveedorService.listarProveedores(texto, pais, activo));
        model.addAttribute("paises", proveedorService.listarPaises());

        // Filtros y pagina de vuelta, para que sigan marcados
        model.addAttribute("texto", texto);
        model.addAttribute("pais", pais);
        model.addAttribute("activo", activo);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "compras/proveedores";
    }

    // Alta y edicion comparten la misma vista
    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model) {
        model.addAttribute("proveedor", new Proveedor());
        return "compras/proveedor";
    }

    @GetMapping("/{id}/editar")
    public String editarProveedor(@PathVariable Long id, Model model) {
        model.addAttribute("proveedor", proveedorService.buscarProveedorXId(id));
        return "compras/proveedor";
    }

    // Sirve para crear y para editar: sin id es un proveedor nuevo
    @PostMapping
    public String guardarProveedor(@ModelAttribute("proveedor") Proveedor proveedor,
                                   Model model, RedirectAttributes flash, Locale locale) {

        try {
            // El servicio valida el formato del CIF y que no este repetido
            proveedorService.guardarProveedor(proveedor);

        } catch (CifInvalidoException | CifDuplicadoException e) {
            // Se vuelve al formulario con el error
            model.addAttribute("error", traducir(e, locale));
            return "compras/proveedor";
        }

        flash.addFlashAttribute("mensaje", messageSource.getMessage("proveedores.ok.guardado", null, locale));
        return "redirect:/compras/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarProveedor(@PathVariable Long id, RedirectAttributes flash,
                                    Locale locale) {

        try {
            proveedorService.eliminarProveedor(id);

        } catch (ProveedorConReferenciasException e) {
            // No se borra un proveedor que tiene ordenes de compra
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/compras/proveedores";
        }

        flash.addFlashAttribute("mensaje", messageSource.getMessage("proveedores.ok.eliminado", null, locale));

        return "redirect:/compras/proveedores";
    }

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }
}
