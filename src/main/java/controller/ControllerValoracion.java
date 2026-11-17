package controller;

import exception.valoracion.ValoracionDuplicadaException;
import exception.valoracion.ValoracionInvalidaException;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import service.interfaces.ValoracionService;

@Controller
@RequestMapping("/valoraciones")
public class ControllerValoracion {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private MessageSource messageSource;

    // Las excepciones se capturan para no acabar en la pagina de error y volver a la ficha con el aviso
    @PostMapping("/guardar")
    public String guardar(@RequestParam Long videojuegoId,
                          @RequestParam String titulo,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam("valoracion") int puntuacion,
                          Authentication authentication,
                          RedirectAttributes flash,
                          Locale locale) {

        try {
            // El cliente sale de la sesion; el servicio comprueba que no haya valorado ya este juego
            valoracionService.crear(authentication.getName(), videojuegoId, titulo, descripcion, puntuacion);

        } catch (ValoracionInvalidaException | ValoracionDuplicadaException e) {
            // El mensaje de la excepcion es la clave de traduccion, no el texto
            flash.addFlashAttribute("error", traducir(e.getMessage(), locale));
        }

        return "redirect:/tienda/juego/" + videojuegoId;
    }

    // Convierte una clave i18n al idioma del usuario; si no existe devuelve la propia clave
    private String traducir(String clave, Locale locale, Object... args) {
        return messageSource.getMessage(clave, args, clave, locale);
    }
}
