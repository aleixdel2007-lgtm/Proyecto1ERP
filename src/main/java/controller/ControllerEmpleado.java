package controller;

import exception.EmailDuplicadoException;
import exception.EmpleadoConReferenciasException;
import exception.NifDuplicadoException;
import java.util.List;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import model.seguridad.Rol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.EmpleadoService;

@Controller
public class ControllerEmpleado {

    @Autowired
    private EmpleadoService empleadoService;

    @ModelAttribute("rolesDisponibles")
    public Rol[] rolesDisponibles() {
        return new Rol[]{Rol.GESTOR_PRODUCTOS, Rol.GESTOR_CLIENTES, Rol.GESTOR_INVENTARIO, Rol.ADMIN};
    }

    @GetMapping("/empleados")
    public String listarEmpleados(Model model) {

        List<Empleado> lista = empleadoService.listarEmpleados();
        model.addAttribute("empleados", lista);

        return "empleados/listado";
    }

    @GetMapping("/empleados/nuevo")
    public String nuevoEmpleado(Model model) {

        Empleado empleado = new Empleado();
        model.addAttribute("empleado", empleado);

        List<Departamento> departamentos = empleadoService.listarDepartamentos();
        model.addAttribute("departamentos", departamentos);

        return "empleados/formularioNuevoEmp";
    }

    @GetMapping("/empleados/editar/{id}")
    public String editarEmpleado(@PathVariable Long id, Model model) {

        Empleado empleado = empleadoService.buscarEmpleadoXId(id);
        model.addAttribute("empleado", empleado);

        List<Departamento> departamentos = empleadoService.listarDepartamentos();
        model.addAttribute("departamentos", departamentos);

        return "empleados/formularioEditarEmp";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(
            @ModelAttribute("empleado") Empleado empleado,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String password,
            Model model,
            RedirectAttributes flash) {

        try {
            empleadoService.guardarEmpleado(empleado, departamentoId, password);

        } catch (EmailDuplicadoException | NifDuplicadoException | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("departamentos", empleadoService.listarDepartamentos());

            if (empleado.getId() == null) {
                return "empleados/formularioNuevoEmp";
            }
            return "empleados/formularioEditarEmp";
        }

        flash.addFlashAttribute("mensaje", "Empleado guardado correctamente");

        return "redirect:/empleados";
    }

    @PostMapping("/empleados/eliminar/{id}")
    public String eliminarEmpleado(@PathVariable Long id, RedirectAttributes flash) {

        try {
            empleadoService.eliminarEmpleado(id);

        } catch (EmpleadoConReferenciasException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/empleados";
        }

        flash.addFlashAttribute("mensaje", "Empleado eliminado correctamente");

        return "redirect:/empleados";
    }
}
