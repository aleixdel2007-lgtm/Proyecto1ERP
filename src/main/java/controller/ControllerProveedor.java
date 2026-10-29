/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import dto.ProveedorFormulario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import model.aprovisionamiento.TipoProveedor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import exception.CifDuplicadoException;
import service.interfaces.ProveedorService;

/**
 *
 * @author PC
 */
@Controller
//Ruta por defecto
@RequestMapping("/compras/proveedores")
@RequiredArgsConstructor
public class ControllerProveedor {
    
    private final ProveedorService proveedorService;
    
    @ModelAttribute("tipos")
    public TipoProveedor[] tipos(){
        return TipoProveedor.values();
    }
    
    @GetMapping
    public String listarProveedores(Model model){
        model.addAttribute("proveedores", proveedorService.listar());
        return "compras/proveedores";
    }
    
    @GetMapping("/nuevo")
    public String nuevoProveedor(Model model){
        model.addAttribute("formulario", new ProveedorFormulario());
        return "compras/proveedor";
    }
    
    @GetMapping("/{id}/editar")
    public String editarProveedor(@PathVariable Long id,Model model){
        model.addAttribute("formulario",proveedorService.buscarFormulario(id));
        return "compras/proveedores";
    }
    
    @PostMapping
    public String guardarProveedor(@Valid @ModelAttribute("formulario")ProveedorFormulario formulario,BindingResult errores,RedirectAttributes flash){
        if (errores.hasErrors()){
            return "compras/proveedor";
        }
        
        try{
            proveedorService.guardarProveedor(formulario);
        }catch(CifDuplicadoException e){
            errores.rejectValue("cif", "cif.duplicado","Ya existe un proveedor con ese CIF");
            return "compras/proveedor";
        }
        flash.addFlashAttribute("mensaje","Proveedor guardado");
        return "redirect:/compras/proveedores";
    }
    
    @PostMapping("/{id}/activo")
    public String cambiarActivo(@PathVariable Long id,@RequestParam boolean activo, RedirectAttributes flash){
        proveedorService.cambiarActivo(id,activo);
        flash.addFlashAttribute("mensaje", activo ? "Proveedor reactivado" : "Proveedor dado de baja");
        return "redirect:/compras/proveedores";
    }
    
}
