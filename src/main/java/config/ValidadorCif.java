package config;

public final class ValidadorCif {

    // Letras con las que puede empezar un CIF segun el tipo de organizacion
    private static final String LETRAS_INICIALES = "ABCDEFGHJNPQRSUVW";
    // Digito de control (0-9) -> su letra
    private static final String LETRAS_CONTROL = "JABCDEFGHI";
    // Organizaciones cuyo control es siempre una letra
    private static final String SOLO_LETRA = "PQRSNW";
    // Organizaciones cuyo control es siempre un digito
    private static final String SOLO_DIGITO = "ABEH";

    private ValidadorCif() {
    }

    // Deja el CIF en su forma canonica: sin espacios ni guiones y en mayusculas
    public static String normalizar(String cif) {

        if (cif == null) {
            return null;
        }
        return cif.trim().replace(" ", "").replace("-", "").toUpperCase();
    }

    // Comprueba la forma (letra + 7 digitos + control) y que el control cuadre con el calculo
    public static boolean esValido(String cif) {

        String limpio = normalizar(cif);

        // Debe medir 9 caracteres
        if (limpio == null || limpio.length() != 9) {
            return false;
        }

        char inicial = limpio.charAt(0);

        if (LETRAS_INICIALES.indexOf(inicial) < 0) {
            return false;
        }

        // Los 7 caracteres centrales deben ser digitos
        String digitos = limpio.substring(1, 8);

        if (!digitos.matches("\\d{7}")) {
            return false;
        }

        char control = limpio.charAt(8);
        // El control esperado, en sus dos formas posibles: letra y digito
        int calculado = digitoDeControl(digitos);
        char letra = LETRAS_CONTROL.charAt(calculado);
        char digito = Character.forDigit(calculado, 10);

        if (SOLO_LETRA.indexOf(inicial) >= 0) {
            return control == letra;
        }

        if (SOLO_DIGITO.indexOf(inicial) >= 0) {
            return control == digito;
        }

        // El resto de organizaciones admite cualquiera de los dos
        return control == letra || control == digito;
    }

    // Suma los digitos de posicion par duplicados (restando 9 si pasan de 9) y los impares tal cual,
    // y devuelve lo que falta para cerrar la decena
    private static int digitoDeControl(String digitos) {

        int suma = 0;

        for (int i = 0; i < 7; i++) {

            int d = Character.getNumericValue(digitos.charAt(i));

            // Posiciones 1, 3, 5 y 7 contando desde 1 (indices pares)
            if (i % 2 == 0) {
                d = d * 2;

                if (d > 9) {
                    d = d - 9;
                }
            }
            suma = suma + d;
        }
        // El segundo % 10 hace que una suma terminada en 0 de control 0 y no 10
        return (10 - (suma % 10)) % 10;
    }
}
