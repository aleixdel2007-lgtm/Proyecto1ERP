package controller;

import exception.clave.ClaveNoAnulableException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import model.inventario.ClaveLicencia;
import model.inventario.EstadoClave;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.ClaveLicenciaService;

@Controller
@RequestMapping("/inventario")
@RequiredArgsConstructor
public class ControllerClaveLicencia {

    private final ClaveLicenciaService claveLicenciaService;
    private final MessageSource messageSource;

    // Listado con tres filtros y un contador por estado
    @GetMapping("/claves")
    public String listarClaves(
            @RequestParam(required = false) String codigo,
            @RequestParam(required = false) Long videojuegoId,
            @RequestParam(required = false) EstadoClave estado,
            @RequestParam(defaultValue = "0") int pagina,
            Model model) {

        List<ClaveLicencia> claves = claveLicenciaService.listarClaves(codigo, videojuegoId, estado);

        model.addAttribute("claves", claves);
        model.addAttribute("videojuegos", claveLicenciaService.listarVideojuegos());
        model.addAttribute("estados", EstadoClave.values());

        // Contadores de la cabecera
        model.addAttribute("disponibles", claveLicenciaService.contarPorEstado(EstadoClave.DISPONIBLE));
        model.addAttribute("reservadas", claveLicenciaService.contarPorEstado(EstadoClave.RESERVADA));
        model.addAttribute("vendidas", claveLicenciaService.contarPorEstado(EstadoClave.VENDIDA));
        model.addAttribute("anuladas", claveLicenciaService.contarPorEstado(EstadoClave.ANULADA));

        // Filtros y pagina de vuelta, para que sigan marcados
        model.addAttribute("codigo", codigo);
        model.addAttribute("videojuegoId", videojuegoId);
        model.addAttribute("estado", estado);
        model.addAttribute("pagina", Math.max(0, pagina));

        return "inventario/claves";
    }

    // Pantalla que pide el motivo antes de anular
    @GetMapping("/claves/{id}/anular")
    public String formularioAnular(@PathVariable Long id, Model model) {

        model.addAttribute("clave", claveLicenciaService.buscarClaveXId(id));

        return "inventario/anularClave";
    }

    @PostMapping("/claves/{id}/anular")
    public String anularClave(@PathVariable Long id,
                              @RequestParam(required = false) String motivo,
                              RedirectAttributes flash,
                              Locale locale) {

        try {
            // El servicio solo deja anular claves DISPONIBLES y exige un motivo
            claveLicenciaService.anularClave(id, motivo);

        } catch (ClaveNoAnulableException e) {
            // La clave ya no esta disponible: volver al formulario no lo arregla, se va al listado
            flash.addFlashAttribute("error", messageSource.getMessage(e.getMessage(),
                    new Object[]{e.getEstado()}, e.getMessage(), locale));
            return "redirect:/inventario/claves";

        } catch (IllegalArgumentException e) {
            // Dato mal escrito (motivo): se vuelve al formulario para corregirlo
            flash.addFlashAttribute("error", traducir(e, locale));
            return "redirect:/inventario/claves/{id}/anular";
        }

        flash.addFlashAttribute("mensaje", messageSource.getMessage("claves.ok.anulada", null, locale));

        return "redirect:/inventario/claves";
    }

    // El mensaje de la excepcion es una clave i18n: se traduce al idioma del usuario
    private String traducir(RuntimeException e, Locale locale) {
        return messageSource.getMessage(e.getMessage(), null, e.getMessage(), locale);
    }
}
