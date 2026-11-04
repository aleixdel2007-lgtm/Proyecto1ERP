package controller;


import dao.ImagenUsuarioDao;
import dao.UsuarioDao;
import lombok.RequiredArgsConstructor;
import model.seguridad.Cliente;
import model.seguridad.ImagenUsuario;
import model.seguridad.Rol;
import model.seguridad.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class ControllerHome {

    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/png", "png", "image/jpeg", "jpg", "image/webp", "webp");

    private final UsuarioDao usuarioDao;
    private final ImagenUsuarioDao imagenUsuarioDao;

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    @GetMapping("/home")
    public String home(Authentication authentication, Model model) {
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElseThrow();

        if (usuario.getRol() == Rol.CLIENTE) {
            return "redirect:/tienda";
        }
        model.addAttribute("usuario", usuario);
        return "home";
    }

    @GetMapping("/tienda")
    public String tienda() {
        return "client/tienda";
    }

    @GetMapping("/user/profile")
    public String profile(Authentication authentication, Model model) {
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElse(null);
        if (usuario == null) {
            // Sesion de un usuario que ya no existe en la base de datos: a iniciar sesion de nuevo
            return "redirect:/login";
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("cliente", usuario instanceof Cliente c ? c : null);
        model.addAttribute("imagen", imagenUsuarioDao.findById(usuario.getId()).orElse(null));
        return "clients/configuracionUsuario";
    }

    @PostMapping("/user/profile/imagen")
    public String cambiarImagen(Authentication authentication,
                                @RequestParam("imagen") MultipartFile archivo,
                                @RequestParam(value = "textoAlternativo", required = false) String textoAlternativo,
                                RedirectAttributes flash) throws IOException {
        Usuario usuario = usuarioDao.findByEmail(authentication.getName()).orElseThrow();

        String extension = archivo.getContentType() == null ? null : EXTENSIONES.get(archivo.getContentType());
        if (archivo.isEmpty() || extension == null) {
            flash.addFlashAttribute("errorImagen", "Sube una imagen PNG, JPG o WEBP.");
            return "redirect:/user/profile";
        }

        Path carpeta = Path.of(uploadsDir, "perfiles");
        Files.createDirectories(carpeta);
        String nombre = usuario.getId() + "-" + UUID.randomUUID() + "." + extension;
        archivo.transferTo(carpeta.resolve(nombre));

        ImagenUsuario imagen = imagenUsuarioDao.findById(usuario.getId()).orElseGet(() -> {
            ImagenUsuario nueva = new ImagenUsuario();
            nueva.setUsuario(usuario);
            return nueva;
        });
        String rutaAnterior = imagen.getRutaImagen();
        imagen.setRutaImagen("/uploads/perfiles/" + nombre);
        imagen.setTextoAlternativo(textoAlternativo == null || textoAlternativo.isBlank()
                ? "Foto de perfil de " + usuario.getNombre() : textoAlternativo.trim());
        imagenUsuarioDao.save(imagen);

        if (rutaAnterior != null) {
            Files.deleteIfExists(Path.of(uploadsDir, rutaAnterior.substring("/uploads/".length())));
        }
        flash.addFlashAttribute("okImagen", "Foto actualizada.");
        return "redirect:/user/profile";
    }
}
