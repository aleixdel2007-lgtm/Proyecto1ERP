package controller;

import exception.departamento.DepartamentoConTrabajadoresException;
import exception.departamento.NombreDepartamentoDuplicadoException;
import exception.departamento.ResponsableOcupadoException;
import java.util.List;
import java.util.Locale;
import model.seguridad.Departamento;
import model.seguridad.Empleado;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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

    @Autowired
    private MessageSource messageSource;

    @GetMapping("/departamentos")
    public String listarDepartamentos(
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        List<Departamento> lista = departamentoService.listarDepartamentos();
        model.addAttribute("departamentos", lista);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "departamentos/listado";
    }

    @GetMapping("/departamentos/nuevo")
    public String nuevoDepartamento(Model model) {

        Departamento departamento = new Departamento();
        model.addAttribute("departamento", departamento);

        // Candidatos a responsable del departamento
        List<Empleado> trabajadores = departamentoService.listarTrabajadores();
        model.addAttribute("trabajadores", trabajadores);

        return "departamentos/formularioNuevoDep";
    }

    // Sirve para crear y para editar: sin id es un departamento nuevo
    @PostMapping("/departamentos/guardar")
    public String guardarDepartamento(
            @ModelAttribute("departamento") Departamento departamento,
            @RequestParam(required = false) Long responsableId,
            Model model,
            Locale locale) {

        try {
            // El servicio comprueba que el nombre no se repita y que el responsable no lo sea de otro
            departamentoService.guardarDepartamento(departamento, responsableId);

        } catch (NombreDepartamentoDuplicadoException | ResponsableOcupadoException e) {

            // Se vuelve al formulario con el error, el que corresponda segun sea alta o edicion
            model.addAttribute("error", traducir(e, locale));
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
    public String eliminarDepartamento(@PathVariable Long id, RedirectAttributes flash,
            Locale locale) {

        try {
            departamentoService.eliminarDepartamento(id);

        } catch (DepartamentoConTrabajadoresException e) {
            // No se borra un departamento que aun tiene trabajadores
            flash.addFlashAttribute("error", traducir(e, locale));
        }

        return "redirect:/departamentos";
    }

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }
}
