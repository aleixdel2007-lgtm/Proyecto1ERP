package controller;

import exception.DepartamentoConTrabajadoresException;
import exception.NombreDepartamentoDuplicadoException;
import exception.ResponsableOcupadoException;
import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.DepartamentoService;

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

        Departamento departamento = new Departamento();
        model.addAttribute("departamento", departamento);

        List<Empleado> trabajadores = departamentoService.listarTrabajadores();
        model.addAttribute("trabajadores", trabajadores);

        return "departamentos/formularioNuevoDep";
    }

    @PostMapping("/departamentos/guardar")
    public String guardarDepartamento(
            @ModelAttribute("departamento") Departamento departamento,
            @RequestParam(required = false) Long responsableId,
            Model model) {

        try {
            departamentoService.guardarDepartamento(departamento, responsableId);

        } catch (NombreDepartamentoDuplicadoException | ResponsableOcupadoException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("trabajadores", departamentoService.listarTrabajadores());

            if (departamento.getId() == null) {
                return "departamentos/formularioNuevoDep";
            }
            return "departamentos/formularioEditarDep";
        }

        return "redirect:/departamentos";
    }

    @GetMapping("/departamentos/editar/{id}")
    public String editarDepartamento(@PathVariable Long id, Model model) {

        Departamento departamento = departamentoService.buscarDepartamentoXId(id);
        model.addAttribute("departamento", departamento);

        List<Empleado> trabajadores = departamentoService.listarTrabajadores();
        model.addAttribute("trabajadores", trabajadores);

        return "departamentos/formularioEditarDep";
    }

    @PostMapping("/departamentos/eliminar/{id}")
    public String eliminarDepartamento(@PathVariable Long id, RedirectAttributes flash) {

        try {
            departamentoService.eliminarDepartamento(id);

        } catch (DepartamentoConTrabajadoresException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/departamentos";
    }
}
