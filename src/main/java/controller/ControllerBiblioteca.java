package controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import model.inventario.ClaveLicencia;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import service.interfaces.BibliotecaService;
import service.interfaces.BibliotecaService.ArchivoDescarga;

@Controller
@RequestMapping("/tienda")
@RequiredArgsConstructor
public class ControllerBiblioteca {

    private final BibliotecaService bibliotecaService;

    @GetMapping("/biblioteca")
    public String verBiblioteca(Authentication authentication, Model model) {

        List<ClaveLicencia> claves = bibliotecaService.listarBiblioteca(authentication.getName());

        model.addAttribute("claves", claves);
        model.addAttribute("portadas", bibliotecaService.listarPortadas(claves));
        model.addAttribute("versiones", bibliotecaService.listarVersionesActivas(claves));

        return "client/biblioteca";
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarJuego(@PathVariable Long id,
                                                   Authentication authentication) {

        // Quien puede bajarse esto lo decide el servicio, no este metodo: si el juego no es
        // gratis y no tienes una clave VENDIDA suya, prepararDescarga lanza y aqui no se
        // llega. El email sale de la sesion y nunca de la URL, asi que nadie se baja lo de otro
        ArchivoDescarga archivo = bibliotecaService.prepararDescarga(authentication.getName(), id);

        // Esta respuesta no es una pagina: es el archivo. attachment hace que el navegador lo
        // guarde en vez de intentar abrirlo, y el filename es el nombre con el que lo subio el
        // estudio, no el UUID con el que vive en el disco. El octet-stream evita que nada
        // intente interpretar el contenido, el contentLength es lo que pinta la barra de
        // progreso, y el cuerpo es el archivo leido del disco a trozos: nunca entero en memoria
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(archivo.tamanoBytes())
                .body(archivo.recurso());
    }
}
