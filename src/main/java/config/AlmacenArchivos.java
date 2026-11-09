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
import exception.imagen.ImagenInvalidaException;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class AlmacenArchivos {

    // Prefijo con el que WebConfig sirve lo que hay en disco, y con el que empieza
    // todo lo que se guarda en imagen_videojuego.ruta
    private static final String RAIZ_WEB = "/uploads/";
    // Carpeta que agrupa las de cada juego, dentro de app.uploads.dir
    private static final String CARPETA_VIDEOJUEGOS = "videojuegos";
    // Carpeta de las fotos de perfil, la misma que ya usaba ControllerHome
    private static final String CARPETA_PERFILES = "perfiles";
    // Lo unico que se admite subir. El tipo lo declara el navegador, asi que esto es un
    // filtro de cortesia, no una garantia: lo que de verdad protege es que el nombre del
    // archivo lo pone un UUID y nunca el que venga de fuera
    private static final Map<String, String> EXTENSIONES = Map.of(
            "image/png", "png", "image/jpeg", "jpg", "image/webp", "webp");
    // Corte del slug. Windows no admite rutas de mas de 260 caracteres y el titulo
    // puede medir 150, asi que sin el corte una ruta larga no se podria crear
    private static final int LARGO_SLUG = 40;
    // Para los titulos de los que no sale ni una letra ni un numero utilizables
    private static final String SLUG_POR_DEFECTO = "juego";

    // Carpeta raiz de las subidas, de application.properties. Es relativa al proyecto
    @Value("${app.uploads.dir}")
    private String uploadsDir;

    // Lo que queda apuntado de un ZIP guardado: lo que hay que meter en version_juego
    public record ZipGuardado(String nombreArchivo, String ruta, long tamanoBytes, String sha256) {
    }

    // Carpeta de los ZIP, FUERA de app.uploads.dir: WebConfig sirve todo lo que cuelga de
    // /uploads/, asi que un ZIP ahi lo podria bajar cualquiera sin haber comprado el juego
    @Value("${app.juegos.dir}")
    private String juegosDir;

    // Comprueba que lo subido parece un ZIP de verdad. La extension y el tipo los declara el
    // navegador, asi que lo que cuenta es la firma: todo ZIP con contenido empieza por PK\3\4
    public void validarZip(MultipartFile zip) {

        if (zip == null || zip.isEmpty()) {
            throw new IllegalArgumentException("Selecciona el archivo ZIP del juego");
        }

        String nombre = zip.getOriginalFilename();

        if (nombre == null || !nombre.toLowerCase(Locale.ROOT).endsWith(".zip")) {
            throw new IllegalArgumentException("El juego tiene que ser un archivo .zip");
        }

        byte[] firma = new byte[4];

        try (InputStream in = zip.getInputStream()) {

            if (in.readNBytes(firma, 0, 4) < 4
                    || firma[0] != 'P' || firma[1] != 'K' || firma[2] != 3 || firma[3] != 4) {
                throw new IllegalArgumentException("El archivo no es un ZIP valido");
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el ZIP", e);
        }
    }

    // Guarda el ZIP en juegos-privados/<id del juego>/ y calcula el tamano y el SHA-256 al
    // mismo tiempo que lo copia, sin leer el archivo dos veces. El nombre en disco lo pone
    // un UUID; el que traia el cliente solo se guarda para mostrarlo
    public ZipGuardado guardarZip(Long videojuegoId, MultipartFile zip) {

        Path carpeta = raizJuegos().resolve(String.valueOf(videojuegoId));
        String nombreDisco = UUID.randomUUID() + ".zip";

        try {
            Files.createDirectories(carpeta);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");

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

    // Borra todos los ZIP de un juego. Se usa si falla el guardado a medias
    public void borrarZips(Long videojuegoId) {

        Path carpeta = raizJuegos().resolve(String.valueOf(videojuegoId));

        if (!Files.isDirectory(carpeta)) {
            return;
        }

        try (Stream<Path> contenido = Files.walk(carpeta)) {

            for (Path ruta : contenido.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(ruta);
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar los ZIP del juego " + videojuegoId, e);
        }
    }

    // Solo el nombre, sin carpetas (Internet Explorer manda la ruta entera), y cortado a la
    // longitud de la columna
    private String nombreVisible(String original) {

        String nombre = original == null ? "juego.zip" : original;
        nombre = nombre.substring(Math.max(nombre.lastIndexOf('/'), nombre.lastIndexOf('\\')) + 1);

        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private Path raizJuegos() {
        return Path.of(juegosDir).toAbsolutePath().normalize();
    }

    // Escribe la imagen en la carpeta del juego y devuelve la ruta web que se guarda
    // en la base de datos. El titulo solo se usa si la carpeta todavia no existe
    public String guardarImagen(Long videojuegoId, String titulo, MultipartFile archivo, String extension) {

        // Carpeta del juego, creandola si es la primera imagen
        Path carpeta = carpetaDeVideojuego(videojuegoId, titulo);
        // El nombre lo pone un UUID y nunca el nombre que traiga el cliente: ese nombre
        // es texto de fuera y no debe acabar formando parte de una ruta
        String nombre = UUID.randomUUID() + "." + extension;

        escribir(carpeta, archivo, nombre);

        // La ruta web se monta con el nombre real de la carpeta, que puede llevar un slug
        // viejo si el juego se ha renombrado: manda lo que hay en disco
        return RAIZ_WEB + CARPETA_VIDEOJUEGOS + "/" + carpeta.getFileName() + "/" + nombre;
    }

    // Guarda la foto de perfil de un usuario y devuelve su ruta web. Todas viven juntas
    // en la misma carpeta: a diferencia de los juegos, un usuario tiene una sola foto y
    // no hay nada que agrupar
    public String guardarImagenPerfil(Long usuarioId, MultipartFile archivo) {

        String extension = extensionDe(archivo);
        // El id delante para poder reconocer de quien es mirando la carpeta
        String nombre = usuarioId + "-" + UUID.randomUUID() + "." + extension;

        escribir(raiz().resolve(CARPETA_PERFILES), archivo, nombre);

        return RAIZ_WEB + CARPETA_PERFILES + "/" + nombre;
    }

    // Comprueba que hay archivo y que es de un tipo admitido, y devuelve su extension.
    // Vive aqui y no en cada servicio para que no haya dos listas de tipos que se puedan
    // desincronizar
    public String extensionDe(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            throw new ImagenInvalidaException("Selecciona un archivo");
        }

        String extension = archivo.getContentType() == null
                ? null : EXTENSIONES.get(archivo.getContentType());

        if (extension == null) {
            throw new ImagenInvalidaException("La imagen tiene que ser JPG, PNG o WEBP");
        }

        // El tope de multipart ya no es de 2 MB (lo subio el ZIP del juego), asi que el
        // limite de las imagenes se comprueba aqui
        if (archivo.getSize() > 2L * 1024 * 1024) {
            throw new ImagenInvalidaException("La imagen no puede pasar de 2 MB");
        }
        return extension;
    }

    // El volcado al disco, comun a las dos. La carpeta es absoluta, asi que transferTo no
    // tiene que resolver nada contra el directorio de trabajo ni contra el temporal
    private void escribir(Path carpeta, MultipartFile archivo, String nombre) {

        try {
            // createDirectories tambien crea uploads si falta, que es lo que hace que esto
            // funcione en un clon recien bajado: uploads esta en el .gitignore
            Files.createDirectories(carpeta);
            archivo.transferTo(carpeta.resolve(nombre));

        } catch (IOException e) {
            // El IOException se envuelve aqui para que el service no arrastre checked
            // exceptions, igual que hace FacturaServiceImpl al generar el PDF
            throw new IllegalStateException("No se pudo guardar el archivo " + nombre, e);
        }
    }

    // Borra un archivo suelto a partir de la ruta web que tiene guardada su fila
    public void borrar(String rutaWeb) {

        // Traduce la ruta web a ruta de disco, o null si no es de fiar
        Path archivo = rutaDeDisco(rutaWeb);

        // Una ruta que no pasa la comprobacion no se toca
        if (archivo == null) {
            return;
        }

        try {
            // deleteIfExists y no delete: si el archivo ya no esta, no es un error
            Files.deleteIfExists(archivo);

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo borrar el archivo " + rutaWeb, e);
        }
    }

    // Borra la carpeta entera de un juego con todo lo que tenga dentro. Se llama al
    // eliminar el videojuego, porque las filas se van con el orphanRemoval pero los
    // archivos no se irian solos
    public void borrarCarpeta(Long videojuegoId) {

        // Busca la carpeta por el id, no por el slug
        Path carpeta = buscarCarpeta(videojuegoId);

        // Un juego sin ninguna imagen no tiene carpeta, y no pasa nada
        if (carpeta == null) {
            return;
        }

        try (Stream<Path> contenido = Files.walk(carpeta)) {

            // El orden inverso pone los archivos antes que la carpeta que los contiene:
            // un directorio no se puede borrar mientras tenga algo dentro
            for (Path ruta : contenido.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(ruta);
            }

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo borrar la carpeta del videojuego " + videojuegoId, e);
        }
    }

    // Convierte el titulo en algo que pueda ser un nombre de carpeta en cualquier
    // sistema. Es decoracion para poder leer la carpeta: la identidad es el id
    public String slug(String titulo) {

        // Sin titulo no hay nada que convertir
        if (titulo == null || titulo.isBlank()) {
            return SLUG_POR_DEFECTO;
        }

        String limpio = Normalizer.normalize(titulo, Normalizer.Form.NFD)
                // NFD separa la letra de su acento y aqui se tira el acento: la n con
                // virgulilla se queda en n, y la a con tilde en a
                .replaceAll("\\p{M}", "")
                // Locale.ROOT a proposito: con el idioma del sistema en turco, la I
                // mayuscula se convierte en una i sin punto y el nombre deja de ser ascii
                .toLowerCase(Locale.ROOT)
                // Todo lo que no sea letra o numero pasa a guion, seguidos incluidos.
                // Aqui mueren los dos puntos, las barras y los puntos, que son los que
                // romperian la ruta o permitirian salirse de la carpeta
                .replaceAll("[^a-z0-9]+", "-")
                // Guiones de los extremos fuera
                .replaceAll("^-+|-+$", "");

        // Corte por el limite de ruta de Windows
        if (limpio.length() > LARGO_SLUG) {
            // Y otro recorte de guiones, por si el corte ha caido justo en uno
            limpio = limpio.substring(0, LARGO_SLUG).replaceAll("-+$", "");
        }

        // Un titulo escrito solo con kanji o con emojis se queda sin nada utilizable
        if (limpio.isEmpty()) {
            return SLUG_POR_DEFECTO;
        }
        return limpio;
    }

    // Devuelve la carpeta del juego, creandola la primera vez. Aqui se cumple la regla
    // de que el slug se calcula UNA sola vez: si la carpeta ya existe se devuelve tal
    // como esta, con el slug que tuviera, aunque el juego se haya renombrado despues
    private Path carpetaDeVideojuego(Long videojuegoId, String titulo) {

        Path existente = buscarCarpeta(videojuegoId);

        if (existente != null) {
            return existente;
        }

        // Primera imagen del juego: se estrena la carpeta con el slug de ahora
        Path nueva = raizVideojuegos().resolve(videojuegoId + "-" + slug(titulo));

        try {
            // createDirectories crea tambien uploads y uploads/videojuegos si faltan,
            // que es lo que hace que esto funcione en un clon recien bajado: uploads
            // esta en el .gitignore y no viene en el repositorio
            Files.createDirectories(nueva);

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta del videojuego " + videojuegoId, e);
        }
        return nueva;
    }

    // Localiza la carpeta de un juego por su id y nunca por el slug, que es la regla
    // que permite renombrar el juego sin tocar el disco ni las filas
    private Path buscarCarpeta(Long videojuegoId) {

        Path raiz = raizVideojuegos();

        // Si nadie ha subido todavia ninguna imagen, la raiz no existe
        if (!Files.isDirectory(raiz)) {
            return null;
        }

        // El patron exige el id y el guion, asi que 318-* no se traga 3180-loquesea:
        // en esa el cuarto caracter es un 0 y no el guion
        try (DirectoryStream<Path> carpetas = Files.newDirectoryStream(raiz, videojuegoId + "-*")) {

            for (Path carpeta : carpetas) {

                if (Files.isDirectory(carpeta)) {
                    return carpeta;
                }
            }

        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la carpeta de videojuegos", e);
        }
        // No hay carpeta para ese id
        return null;
    }

    // La carpeta de subidas. Solo calcula la ruta, no toca el disco. Se hace absoluta
    // porque app.uploads.dir es relativa, y una ruta relativa la resuelve cada API contra
    // un sitio distinto
    private Path raiz() {
        return Path.of(uploadsDir).toAbsolutePath().normalize();
    }

    // Carpeta que agrupa las de todos los juegos
    private Path raizVideojuegos() {
        return raiz().resolve(CARPETA_VIDEOJUEGOS);
    }

    // Traduce una ruta web a la ruta real en disco, o devuelve null si no se puede
    // confiar en ella. Es la unica puerta por la que se borra algo
    private Path rutaDeDisco(String rutaWeb) {

        // Lo que no cuelgue de /uploads/ no es nuestro
        if (rutaWeb == null || !rutaWeb.startsWith(RAIZ_WEB)) {
            return null;
        }

        Path base = raiz();
        // normalize resuelve los .. que pudiera traer la ruta
        Path archivo = base.resolve(rutaWeb.substring(RAIZ_WEB.length())).normalize();

        // Y esta es la comprobacion que cuenta: despues de normalizar, el archivo tiene
        // que seguir estando dentro de uploads. Sin ella, una ruta con .. en la base de
        // datos podria borrar cualquier cosa del disco
        if (!archivo.startsWith(base)) {
            return null;
        }
        return archivo;
    }
}
