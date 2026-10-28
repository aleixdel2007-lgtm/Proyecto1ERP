package config;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class IndiceCiego {

    private static final String VARIABLE_CLAVE = "LUDUSERP_AES_KEY";
    private static final String ALGORITMO = "HmacSHA256";
    private static final int BYTES_CLAVE = 32;

    private IndiceCiego() {
    }

    public static String hash(String enClaro) {
        if (enClaro == null) {
            return null;
        }
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(clave(), ALGORITMO));
            byte[] resumen = mac.doFinal(enClaro.trim().toUpperCase().getBytes(StandardCharsets.UTF_8));

            StringBuilder hexadecimal = new StringBuilder(resumen.length * 2);
            for (byte b : resumen) {
                hexadecimal.append(String.format("%02x", b));
            }
            return hexadecimal.toString();
        } catch (Exception e) {
            throw new IllegalStateException("No se ha podido calcular el indice ciego", e);
        }
    }

    private static byte[] clave() {
        String valor = System.getenv(VARIABLE_CLAVE);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta la variable de entorno " + VARIABLE_CLAVE
                    + " con la clave AES de " + BYTES_CLAVE + " bytes codificada en Base64");
        }
        byte[] bytes = Base64.getDecoder().decode(valor);
        if (bytes.length != BYTES_CLAVE) {
            throw new IllegalStateException(VARIABLE_CLAVE + " debe medir " + BYTES_CLAVE
                    + " bytes (AES-256) codificados en Base64, y mide " + bytes.length);
        }
        return bytes;
    }
}
