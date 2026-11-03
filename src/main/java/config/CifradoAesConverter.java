package config;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

// Traductor que JPA llama solo: los campos marcados con @Convert se cifran al entrar en
// la base de datos y se descifran al salir, asi que el codigo de negocio siempre trabaja
// con el NIF o el IBAN en claro y no sabe que esto existe.
@Converter
public class CifradoAesConverter implements AttributeConverter<String, String> {

    private static final String VARIABLE_CLAVE = "LUDUSERP_AES_KEY";
    private static final String ALGORITMO = "AES/GCM/NoPadding";
    private static final int BYTES_IV = 12;
    private static final int BITS_TAG = 128;
    private static final int BYTES_CLAVE = 32;

    private static final SecureRandom ALEATORIO = new SecureRandom();

    // Cifra al guardar. Genera un IV aleatorio nuevo en cada llamada y lo deja delante del
    // texto cifrado, porque hace falta para descifrar y no es ningun secreto. Al cambiar el
    // IV cada vez, el mismo NIF se guarda distinto, y por eso sobre esta columna no sirven
    // ni UNIQUE ni las busquedas: de ahi que exista IndiceCiego con las columnas *_hash.
    @Override
    public String convertToDatabaseColumn(String enClaro) {
        if (enClaro == null) {
            return null;
        }
        try {
            byte[] iv = new byte[BYTES_IV];
            ALEATORIO.nextBytes(iv);

            Cipher cifrador = Cipher.getInstance(ALGORITMO);
            cifrador.init(Cipher.ENCRYPT_MODE, clave(), new GCMParameterSpec(BITS_TAG, iv));
            byte[] cifrado = cifrador.doFinal(enClaro.getBytes(StandardCharsets.UTF_8));

            byte[] salida = new byte[iv.length + cifrado.length];
            System.arraycopy(iv, 0, salida, 0, iv.length);
            System.arraycopy(cifrado, 0, salida, iv.length, cifrado.length);
            return Base64.getEncoder().encodeToString(salida);
        } catch (Exception e) {
            // Se corta la operacion: nunca se guarda el valor sin cifrar
            throw new IllegalStateException("No se ha podido cifrar el valor", e);
        }
    }

    // Descifra al leer. Separa los 12 primeros bytes, que son el IV con el que se cifro este
    // valor, y descifra el resto con la misma clave.
    @Override
    public String convertToEntityAttribute(String cifrado) {
        if (cifrado == null) {
            return null;
        }
        try {
            byte[] todo = Base64.getDecoder().decode(cifrado);
            byte[] iv = Arrays.copyOfRange(todo, 0, BYTES_IV);
            byte[] datos = Arrays.copyOfRange(todo, BYTES_IV, todo.length);

            Cipher descifrador = Cipher.getInstance(ALGORITMO);
            descifrador.init(Cipher.DECRYPT_MODE, clave(), new GCMParameterSpec(BITS_TAG, iv));
            return new String(descifrador.doFinal(datos), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // Falla si la clave no es la misma con la que se cifro, o si el dato se ha
            // manipulado: GCM firma el cifrado y comprueba esa firma al descifrar
            throw new IllegalStateException("No se ha podido descifrar el valor", e);
        }
    }

    // Lee la clave maestra de la variable de entorno y comprueba que mida los 32 bytes de
    // AES-256. Es la misma clave que usa IndiceCiego, y no esta en application.properties a
    // proposito: no debe viajar en el repositorio.
    private static SecretKeySpec clave() {
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
        return new SecretKeySpec(bytes, "AES");
    }
}
