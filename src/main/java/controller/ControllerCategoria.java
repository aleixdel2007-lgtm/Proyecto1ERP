package controller;

import exception.CategoriaConJuegosException;
import exception.CodigoCategoriaDuplicadoException;
import exception.NombreCategoriaDuplicadoException;
import model.catalogo.Categoria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.CategoriaService;

@Controller
public class ControllerCategoria {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping("/catalogo/categorias")
    public String listarCategorias(
            @RequestParam(required = false) String texto,
            Model model) {

        model.addAttribute("categorias", categoriaService.listarCategorias(texto));
        model.addAttribute("juegos", categoriaService.contarJuegos());

        model.addAttribute("texto", texto);

        return "catalogo/categorias";
    }

    @GetMapping("/catalogo/categorias/nuevo")
    public String nuevaCategoria(Model model) {

        model.addAttribute("categoria", new Categoria());

        return "catalogo/formularioNuevoCat";
    }

    @GetMapping("/catalogo/categorias/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {

        model.addAttribute("categoria", categoriaService.buscarCategoriaXId(id));
        model.addAttribute("juegosAsignados", categoriaService.contarJuegosXCategoria(id));

        return "catalogo/formularioEditarCat";
    }

    @PostMapping("/catalogo/categorias/guardar")
    public String guardarCategoria(
            @ModelAttribute("categoria") Categoria categoria,
            Model model,
            RedirectAttributes flash) {

        try {
            categoriaService.guardarCategoria(categoria);

        } catch (CodigoCategoriaDuplicadoException | NombreCategoriaDuplicadoException
                | IllegalArgumentException e) {

            model.addAttribute("error", e.getMessage());

            if (categoria.getId() == null) {
                return "catalogo/formularioNuevoCat";
            }

            model.addAttribute("juegosAsignados",
                    categoriaService.contarJuegosXCategoria(categoria.getId()));

            return "catalogo/formularioEditarCat";
        }

        flash.addFlashAttribute("mensaje", "Categoria guardada correctamente");

        return "redirect:/catalogo/categorias";
    }

    @PostMapping("/catalogo/categorias/eliminar/{id}")
    public String eliminarCategoria(@PathVariable Long id, RedirectAttributes flash) {

        try {
            categoriaService.eliminarCategoria(id);

        } catch (CategoriaConJuegosException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/catalogo/categorias";
        }

        flash.addFlashAttribute("mensaje", "Categoria eliminada correctamente");

        return "redirect:/catalogo/categorias";
    }
}
