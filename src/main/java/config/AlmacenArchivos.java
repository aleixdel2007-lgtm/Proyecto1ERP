package config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.file.Files;
import java.nio.file.Path;
import exception.biblioteca.DescargaNoDisponibleException;
import exception.imagen.ImagenInvalidaException;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AlmacenArchivos {

    // Prefijo web con el que WebConfig sirve lo que hay en disco
    private static final String RAIZ_WEB = "/uploads/";
    private static final String CARPETA_VIDEOJUEGOS = "videojuegos";
    private static final String CARPETA_PERFILES = "perfiles";
    // Tipos de imagen admitidos; el tipo lo declara el navegador, el nombre en disco siempre es un UUID
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/png", "png", "image/jpeg", "jpg", "image/webp", "webp");
    // Corte del slug para no pasar el limite de 260 caracteres de ruta de Windows
    private static final int LARGO_SLUG = 40;
    private static final String SLUG_POR_DEFECTO = "juego";

    // Carpeta de imagenes, relativa al proyecto (application.properties)
    @Value("${app.uploads.dir}")
    private String uploadsDir;

    // Datos de un ZIP guardado, para la fila de version_juego
    public record ZipGuardado(String nombreArchivo, String ruta, long tamanoBytes, String sha256) {
    }

    // Carpeta de los ZIP, fuera de uploads para que no se sirvan como recurso publico
    @Value("${app.juegos.dir}")
    private String juegosDir;

    // Comprueba que es un ZIP de verdad mirando su firma, no la extension ni el tipo
    public void validarZip(MultipartFile zip) {

        if (zip == null || zip.isEmpty()) {
            throw new IllegalArgumentException("juegos.error.zipObligatorio");
        }

        String nombre = zip.getOriginalFilename();

        if (nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException("juegos.error.zipExtension");
        }

        byte[] firma = new byte[4];

        try (InputStream in = zip.getInputStream()) {

            // Todo ZIP con contenido empieza por PK\3\4
            if (in.readNBytes(firma, 0, 4) < 4
                    || firma[0] != 'P' || firma[1] != 'K' || firma[2] != 3 || firma[3] != 4) {
                throw new IllegalArgumentException("juegos.error.zipInvalido");
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el ZIP", e);
        }
    }

    // Guarda el ZIP en <juegos>/<id del juego>/<uuid>.zip y calcula el SHA-256 mientras lo copia
    public ZipGuardado guardarZip(Long videojuegoId, MultipartFile zip) {

        Path carpeta = raizJuegos().resolve(String.valueOf(videojuegoId));
        // El nombre en disco es un UUID; el original solo se guarda para mostrarlo
        String nombreDisco = UUID.randomUUID() + ".zip";

        try {
            Files.createDirectories(carpeta);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

            // El DigestInputStream va calculando el hash segun se lee, sin segunda pasada
            try (InputStream in = new DigestInputStream(zip.getInputStream(), sha)) {
                Files.copy(in, carpeta.resolve(nombreDisco));
            }
            return new ZipGuardado(nombreVisible(zip.getOriginalFilename()),
                    videojuegoId + "/" + nombreDisco, zip.getSize(),
                    HexFormat.of().formatHex(sha.digest()));

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo guardar el ZIP del juego " + videojuegoId, e);
        }
    }

    // Abre el ZIP de una version para servirlo; quien puede bajarlo lo decide el servicio
    public Resource leerZip(String rutaArchivo) {

        Path archivo = rutaDeZip(rutaArchivo);

        // Ruta no valida o archivo borrado: para el cliente es lo mismo
        if (archivo == null || !Files.isRegularFile(archivo)) {
            throw new DescargaNoDisponibleException();
        }
        // Resource en vez de bytes: un ZIP puede pesar un giga y no debe cargarse en memoria
        return new FileSystemResource(archivo);
    }

    // Convierte la ruta guardada en la BD en ruta de disco, o null si no es de fiar
    private Path rutaDeZip(String rutaArchivo) {

        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            return null;
        }

        Path base = raizJuegos();
        // normalize resuelve los ".." de la ruta
        Path archivo = base.resolve(rutaArchivo).normalize();

        // Tras normalizar debe seguir dentro de la carpeta de juegos; si no, se podria servir cualquier archivo
        if (!archivo.startsWith(base)) {
            return null;
        }
        return archivo;
    }

    // Borra todos los ZIP de un juego; se usa si el guardado falla a medias
    public void borrarZips(Long videojuegoId) {

        Path carpeta = raizJuegos().resolve(String.valueOf(videojuegoId));

        if (!Files.isDirectory(carpeta)) {
            return;
        }

        try (Stream<Path> contenido = Files.walk(carpeta)) {

            // Orden inverso: los archivos antes que la carpeta que los contiene
            for (Path ruta : contenido.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(ruta);
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar los ZIP del juego " + videojuegoId, e);
        }
    }

    // Borra un solo ZIP a partir de su ruta relativa
    public void borrarZip(String rutaArchivo) {

        if (rutaArchivo == null || rutaArchivo.isBlank()) {
            return;
        }

        Path archivo = raizJuegos().resolve(rutaArchivo).normalize();

        // Nunca se borra nada fuera de la carpeta de juegos
        if (!archivo.startsWith(raizJuegos())) {
            throw new IllegalArgumentException("Ruta de ZIP fuera de la carpeta de juegos");
        }

        try {
            Files.deleteIfExists(archivo);

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar el ZIP " + rutaArchivo, e);
        }
    }

    // Deja solo el nombre del archivo, sin carpetas y sin caracteres peligrosos
    private String nombreVisible(String original) {

        String nombre = original == null ? "juego.zip" : original;
        // Algunos navegadores mandan la ruta entera: se queda lo que hay tras la ultima barra
        nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1);

        // Este nombre acaba en la cabecera Content-Disposition: una comilla la partiria
        nombre = nombre.replaceAll("[\\p{Cntrl}\"]", "").trim();

        if (nombre.isBlank()) {
            nombre = "juego.zip";
        }

        // 255 es el largo de la columna
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private Path raizJuegos() {
        return Path.of(juegosDir).toAbsolutePath().normalize();
    }

    // Escribe la imagen en la carpeta del juego y devuelve la ruta web que se guarda en la BD
    public String guardarImagen(Long videojuegoId, String titulo, MultipartFile archivo, String extension) {

        // Carpeta del juego, creada si es la primera imagen
        Path carpeta = carpetaDeVideojuego(videojuegoId, titulo);
        // El nombre lo pone un UUID: el del cliente nunca debe formar parte de una ruta
        String nombre = UUID.randomUUID() + "." + extension;

        escribir(carpeta, archivo, nombre);

        // Se usa el nombre real de la carpeta, que puede llevar un slug antiguo si el juego se renombro
        return RAIZ_WEB + CARPETA_VIDEOJUEGOS + "/" + carpeta.getFileName() + "/" + nombre;
    }

    // Guarda la foto de perfil y devuelve su ruta web; todas viven en la misma carpeta
    public String guardarImagenPerfil(Long usuarioId, MultipartFile archivo) {

        String extension = extensionDe(archivo);
        // El id delante permite saber de quien es mirando la carpeta
        String nombre = usuarioId + "-" + UUID.randomUUID() + "." + extension;

        escribir(raiz().resolve(CARPETA_PERFILES), archivo, nombre);

        return RAIZ_WEB + CARPETA_PERFILES + "/" + nombre;
    }

    // Valida que hay archivo, que su tipo es admitido y que no pesa mas de 2 MB; devuelve la extension
    public String extensionDe(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ImagenInvalidaException("imagenes.error.sinArchivo");
        }

        String extension = archivo.getContentType() == null
                ? null : EXTENSIONES.get(archivo.getContentType());

        if (extension == null) {
            throw new ImagenInvalidaException("imagenes.error.formato");
        }

        // El limite de multipart es mayor (por el ZIP), asi que el de las imagenes se comprueba aqui
        if (archivo.getSize() > 2L * 1024 * 1024) {
            throw new ImagenInvalidaException("imagenes.error.tamano");
        }
        return extension;
    }

    // Volcado al disco comun a imagenes y fotos de perfil
    private void escribir(Path carpeta, MultipartFile archivo, String nombre) {

        try {
            // Crea tambien uploads si falta: no viene en el repositorio (esta en .gitignore)
            Files.createDirectories(carpeta);
            archivo.transferTo(carpeta.resolve(nombre));

        } catch (IOException e) {
            // Se envuelve para que los servicios no arrastren excepciones checked
            throw new IllegalStateException("No se pudo guardar el archivo " + nombre, e);
        }
    }

    // Borra un archivo suelto a partir de la ruta web guardada en su fila
    public void borrar(String rutaWeb) {

        Path archivo = rutaDeDisco(rutaWeb);

        // Una ruta que no pasa la comprobacion no se toca
        if (archivo == null) {
            return;
        }

        try {
            // Si el archivo ya no esta, no es un error
            Files.deleteIfExists(archivo);

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar el archivo " + rutaWeb, e);
        }
    }

    // Borra la carpeta entera de un juego; las filas se borran solas, los archivos no
    public void borrarCarpeta(Long videojuegoId) {

        Path carpeta = buscarCarpeta(videojuegoId);

        // Un juego sin imagenes no tiene carpeta
        if (carpeta == null) {
            return;
        }

        try (Stream<Path> contenido = Files.walk(carpeta)) {

            // Orden inverso: no se puede borrar un directorio con contenido
            for (Path ruta : contenido.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(ruta);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo borrar la carpeta del videojuego " + videojuegoId, e);
        }
    }

    // Convierte un titulo en un nombre de carpeta valido en cualquier sistema (solo decoracion, la identidad es el id)
    public String slug(String titulo) {

        if (titulo == null || titulo.isBlank()) {
            return SLUG_POR_DEFECTO;
        }

        String limpio = Normalizer.normalize(titulo, Normalizer.Form.NFD)
                // Separa la letra de su acento y quita el acento
                .replaceAll("\\p{M}", "")
                // Locale.ROOT: con el idioma turco la I mayuscula no daria ascii
                .toLowerCase(Locale.ROOT)
                // Todo lo que no sea letra o numero pasa a guion: aqui mueren barras y puntos
                .replaceAll("[^a-z0-9]+", "-")
                // Sin guiones en los extremos
                .replaceAll("^-+|-+$", "");

        if (limpio.length() > LARGO_SLUG) {
            // Se recorta y se vuelven a quitar guiones por si el corte cayo en uno
            limpio = limpio.substring(0, LARGO_SLUG).replaceAll("-+$", "");
        }

        // Un titulo solo de kanji o emojis se queda vacio
        if (limpio.isEmpty()) {
            return SLUG_POR_DEFECTO;
        }
        return limpio;
    }

    // Devuelve la carpeta del juego, creandola la primera vez; el slug se calcula una sola vez
    private Path carpetaDeVideojuego(Long videojuegoId, String titulo) {

        Path existente = buscarCarpeta(videojuegoId);

        // Si ya existe se devuelve tal cual, aunque el juego se haya renombrado
        if (existente != null) {
            return existente;
        }

        // Primera imagen: carpeta "<id>-<slug>"
        Path nueva = raizVideojuegos().resolve(videojuegoId + "-" + slug(titulo));

        try {
            Files.createDirectories(nueva);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta del videojuego " + videojuegoId, e);
        }
        return nueva;
    }

    // Localiza la carpeta de un juego por su id, nunca por el slug, para poder renombrarlo sin tocar el disco
    private Path buscarCarpeta(Long videojuegoId) {

        Path raiz = raizVideojuegos();

        // Si nadie ha subido imagenes todavia, la raiz no existe
        if (!Files.isDirectory(raiz)) {
            return null;
        }

        // El patron "<id>-*" exige el guion, asi 318 no coincide con 3180-loquesea
        try (DirectoryStream<Path> carpetas = Files.newDirectoryStream(raiz, videojuegoId + "-*")) {

            for (Path carpeta : carpetas) {

                if (Files.isDirectory(carpeta)) {
                    return carpeta;
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la carpeta de videojuegos", e);
        }
        return null;
    }

    // Carpeta de subidas en absoluto, porque la ruta relativa se resolveria distinto segun la API
    private Path raiz() {
        return Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    private Path raizVideojuegos() {
        return raiz().resolve(CARPETA_VIDEOJUEGOS);
    }

    // Traduce una ruta web a ruta de disco, o null si no es de fiar; es la unica puerta para borrar
    private Path rutaDeDisco(String rutaWeb) {

        // Lo que no cuelgue de /uploads/ no es nuestro
        if (rutaWeb == null || !rutaWeb.startsWith(RAIZ_WEB)) {
            return null;
        }

        Path base = raiz();
        Path archivo = base.resolve(rutaWeb.substring(RAIZ_WEB.length())).normalize();

        // Tras normalizar debe seguir dentro de uploads; si no, una ruta con ".." borraria cualquier cosa
        if (!archivo.startsWith(base)) {
            return null;
        }
        return archivo;
    }
}
