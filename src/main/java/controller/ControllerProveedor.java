package controller;

import exception.CifDuplicadoException;
import exception.ProveedorConCuentaException;
import exception.ProveedorConReferenciasException;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.Proveedor;
import model.aprovisionamiento.TipoProveedor;
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

@Controller
@RequestMapping("/compras/proveedores")
@RequiredArgsConstructor
public class ControllerProveedor {

    private final ProveedorService proveedorService;

    @ModelAttribute("tipos")
    public TipoProveedor[] tipos() {
        return TipoProveedor.values();
    }

    @GetMapping
    public String listarProveedores(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) String pais,
            @RequestParam(required = false) Boolean activo,
            Model model) {

        model.addAttribute("proveedores", proveedorService.listarProveedores(texto, pais, activo));
        model.addAttribute("paises", proveedorService.listarPaises());

        model.addAttribute("texto", texto);
        model.addAttribute("pais", pais);
        model.addAttribute("activo", activo);

        return "compras/proveedores";
    }

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

    @PostMapping
    public String guardarProveedor(@ModelAttribute("proveedor") Proveedor proveedor,
                                   Model model, RedirectAttributes flash) {

        try {
            proveedorService.guardarProveedor(proveedor);

        } catch (CifDuplicadoException e) {
            model.addAttribute("error", e.getMessage());
            return "compras/proveedor";
        }

        flash.addFlashAttribute("mensaje", "Proveedor guardado");
        return "redirect:/compras/proveedores";
    }

    @PostMapping("/{id}/eliminar")
    public String eliminarProveedor(@PathVariable Long id, RedirectAttributes flash) {

        try {
            proveedorService.eliminarProveedor(id);

        } catch (ProveedorConCuentaException | ProveedorConReferenciasException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/compras/proveedores";
        }

        flash.addFlashAttribute("mensaje", "Proveedor eliminado");

        return "redirect:/compras/proveedores";
    }
}
