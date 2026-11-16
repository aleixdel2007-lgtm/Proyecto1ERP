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

    // Las dos excepciones que puede soltar el servicio llevan @ResponseStatus (400 y 409), asi
    // que sin capturarlas el cliente acaba en la Whitelabel Error Page y se sale de la tienda:
    // solo hay plantilla para 403, 404 y 500. Se atrapan aqui y se vuelve a la ficha con el
    // aviso traducido, igual que hace ControllerCesta al anadir un juego
    @PostMapping("/guardar")
    public String guardar(@RequestParam Long videojuegoId,
                          @RequestParam String titulo,
                          @RequestParam(required = false) String descripcion,
                          @RequestParam("valoracion") int puntuacion,
                          Authentication authentication,
                          RedirectAttributes flash,
                          Locale locale) {

        try {
            valoracionService.crear(authentication.getName(), videojuegoId, titulo, descripcion, puntuacion);

        } catch (ValoracionInvalidaException | ValoracionDuplicadaException e) {
            // El mensaje de la excepcion ES la clave de traduccion, no el texto
            flash.addFlashAttribute("error", traducir(e.getMessage(), locale));
        }

        return "redirect:/tienda/juego/" + videojuegoId;
    }

    // Las excepciones y los avisos llevan una clave de traduccion, no el texto: aqui se
    // convierte al idioma del usuario. Si la clave no existe se ensena la propia clave.
    private String traducir(String clave, Locale locale, Object... args) {
        return messageSource.getMessage(clave, args, clave, locale);
    }
}
