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

    // Juegos que ha comprado el usuario, con su portada y su version descargable
    @GetMapping("/biblioteca")
    public String verBiblioteca(Authentication authentication, Model model) {

        // Solo las claves vendidas al usuario de la sesion
        List<ClaveLicencia> claves = bibliotecaService.listarBiblioteca(authentication.getName());

        model.addAttribute("claves", claves);
        // Portada y version descargable de cada juego, para pintar su tarjeta
        model.addAttribute("portadas", bibliotecaService.listarPortadas(claves));
        model.addAttribute("versiones", bibliotecaService.listarVersionesActivas(claves));

        return "client/biblioteca";
    }

    // Sirve el ZIP del juego
    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarJuego(@PathVariable Long id,
                                                   Authentication authentication) {

        // El servicio decide si puede bajarlo (gratis o clave vendida); el email sale de la sesion
        ArchivoDescarga archivo = bibliotecaService.prepararDescarga(authentication.getName(), id);

        // attachment fuerza guardar; contentLength pinta la barra de progreso del navegador
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + archivo.nombreArchivo() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(archivo.tamanoBytes())
                .body(archivo.recurso());
    }
}
