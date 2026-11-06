package controller;

import exception.email.EmailDuplicadoException;
import exception.empleado.EmpleadoConReferenciasException;
import exception.imagen.ImagenInvalidaException;
import exception.nif.NifDuplicadoException;
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
import org.springframework.web.multipart.MultipartFile;
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
    public String listarEmpleados(
            @RequestParam(required = false) String texto,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) Rol rol,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        List<Empleado> lista = empleadoService.listarEmpleados(texto, departamentoId, rol);
        model.addAttribute("empleados", lista);
        model.addAttribute("fotos", empleadoService.listarImagenesPerfil(lista));
        model.addAttribute("departamentos", empleadoService.listarDepartamentos());

        model.addAttribute("texto", texto);
        model.addAttribute("departamentoId", departamentoId);
        model.addAttribute("rol", rol);
        model.addAttribute("pagina", Math.max(0, pagina));

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
        model.addAttribute("imagenEmpleado", empleadoService.buscarImagenPerfil(id));

        return "empleados/formularioEditarEmp";
    }

    @PostMapping("/empleados/guardar")
    public String guardarEmpleado(
            @ModelAttribute("empleado") Empleado empleado,
            @RequestParam(required = false) Long departamentoId,
            @RequestParam(required = false) String password,
            @RequestParam(value = "foto", required = false) MultipartFile foto,
            Model model,
            RedirectAttributes flash) {

        Empleado guardado;

        try {
            guardado = empleadoService.guardarEmpleado(empleado, departamentoId, password);

        } catch (EmailDuplicadoException | NifDuplicadoException | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());
            model.addAttribute("departamentos", empleadoService.listarDepartamentos());

            if (empleado.getId() == null) {
                return "empleados/formularioNuevoEmp";
            }

            model.addAttribute("imagenEmpleado", empleadoService.buscarImagenPerfil(empleado.getId()));

            return "empleados/formularioEditarEmp";
        }

        if (foto != null && !foto.isEmpty()) {

            try {
                empleadoService.guardarImagenPerfil(guardado.getId(), foto);

            } catch (ImagenInvalidaException e) {
                flash.addFlashAttribute("error",
                        "Empleado guardado, pero la foto no: " + e.getMessage());
                return "redirect:/empleados";
            }
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
