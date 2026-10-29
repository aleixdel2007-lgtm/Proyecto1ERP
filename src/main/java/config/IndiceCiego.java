package config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class IndiceCiego {

    // Nombre de la variable de entorno que guarda la clave maestra
    private static final String VARIABLE_CLAVE = "LUDUSERP_AES_KEY";
    // HMAC con SHA-256: hash determinista que necesita la clave para calcularse
    private static final String ALGORITMO = "HmacSHA256";
    // La clave debe medir 32 bytes (256 bits) una vez decodificada del Base64
    private static final int BYTES_CLAVE = 32;

    private IndiceCiego() {
    }

    public static String hash(String enClaro) {
        if (enClaro == null) {
            return null;
        }
        try {
            // Obtiene una instancia del algoritmo HMAC-SHA256
            Mac mac = Mac.getInstance(ALGORITMO);
            // La inicializa con la clave maestra
            mac.init(new SecretKeySpec(clave(), ALGORITMO));
            // Normaliza el valor (sin espacios y en mayusculas) y calcula el HMAC
            byte[] resumen = mac.doFinal(enClaro.trim().toUpperCase().getBytes(StandardCharsets.UTF_8));

            // Reserva espacio para el texto: dos caracteres hexadecimales por byte
            StringBuilder hexadecimal = new StringBuilder(resumen.length * 2);
            // Recorre cada byte del resumen
            for (byte b : resumen) {
                // Lo convierte en dos caracteres hexadecimales y lo anade
                hexadecimal.append(String.format("%02x", b));
            }
            // Devuelve los 64 caracteres que se guardan en la columna *_hash
            return hexadecimal.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se ha podido calcular el indice ciego", e);
        }
    }

    private static byte[] clave() {
        // Lee la clave en Base64 de la variable de entorno
        String valor = System.getenv(VARIABLE_CLAVE);
        // Si no existe o esta vacia, no se puede continuar
        if (valor == null || valor.isBlank()) {
            // Avisa de que falta la variable y de que formato debe tener
            throw new IllegalStateException("Falta la variable de entorno " + VARIABLE_CLAVE
                    + " con la clave AES de " + BYTES_CLAVE + " bytes codificada en Base64");
        }
        // Decodifica el Base64 a los bytes reales de la clave
        byte[] bytes = Base64.getDecoder().decode(valor);
        // Comprueba que mida exactamente 32 bytes
        if (bytes.length != BYTES_CLAVE) {
            // Si no, avisa indicando cuanto mide en realidad
            throw new IllegalStateException(VARIABLE_CLAVE + " debe medir " + BYTES_CLAVE
                    + " bytes (AES-256) codificados en Base64, y mide " + bytes.length);
        }
        // Devuelve la clave lista para usarse en el HMAC
        return bytes;
    }
}
