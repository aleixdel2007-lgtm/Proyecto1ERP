/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Trabajador;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import service.interfaces.DepartamentoService;

/**
 *
 * @author Sandy
 */
@Controller
public class ControllerDepartamento {

    @Autowired
    private DepartamentoService departamentoService;

    @GetMapping("/departamentos")
    public String listarDepartamentos(Model model) {

        List<Departamento> lista = departamentoService.listarDepartamentos();
        model.addAttribute("departamentos", lista);

        return "departamentos/listado";
    }

    @GetMapping("/departamentos/nuevo")
    public String nuevoDepartamento(Model model) {

        Departamento departamento = new Departamento();//creo uno vacio para que el usuario lo llene 
        model.addAttribute("departamento", departamento);

        List<Trabajador> trabajadores = departamentoService.listarTrabajadores();
        model.addAttribute("trabajadores", trabajadores);

        return "departamentos/formularioNuevoDep";
    }

    @PostMapping("/departamentos/guardar")
    public String guardarDepartamento(
            @ModelAttribute("departamento") Departamento departamento,
            @RequestParam(required = false) Long responsableId) { //el required pq no es obligatorio tener responsable, peude ser null 

        departamentoService.guardarDepartamento(departamento, responsableId);

        return "redirect:/departamentos";
    }

    @GetMapping("/departamentos/editar/{id}")
    public String editarDepartamento(@PathVariable Long id, Model model) {

        Departamento departamento = departamentoService.buscarDepartamentoXId(id);

        model.addAttribute("departamento", departamento);

        List<Trabajador> trabajadores = departamentoService.listarTrabajadores();
        model.addAttribute("trabajadores", trabajadores);

        return "departamentos/formularioEditarDep";

    }

    @PostMapping("/departamentos/eliminar/{id}")
    public String eliminarDepartamento(@PathVariable Long id) {

        departamentoService.eliminarDepartamento(id);

        return "redirect:/departamentos";
    }

}
