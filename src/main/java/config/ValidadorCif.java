package config;

public final class ValidadorCif {

    // Las letras con las que puede empezar un CIF segun el tipo de organizacion
    private static final String LETRAS_INICIALES = "ABCDEFGHJNPQRSUVW";
    // Tabla que convierte el digito calculado en su letra de control
    private static final String LETRAS_CONTROL = "JABCDEFGHI";
    // Organizaciones cuyo control es obligatoriamente una letra
    private static final String SOLO_LETRA = "PQRSNW";
    // Organizaciones cuyo control es obligatoriamente un digito
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

        if (limpio == null || limpio.length() != 9) {
            return false;
        }

        char inicial = limpio.charAt(0);

        if (LETRAS_INICIALES.indexOf(inicial) < 0) {
            return false;
        }

        String digitos = limpio.substring(1, 8);

        if (!digitos.matches("\\d{7}")) {
            return false;
        }

        char control = limpio.charAt(8);
        int calculado = digitoDeControl(digitos);
        char letra = LETRAS_CONTROL.charAt(calculado);
        char digito = Character.forDigit(calculado, 10);

        if (SOLO_LETRA.indexOf(inicial) >= 0) {
            return control == letra;
        }

        if (SOLO_DIGITO.indexOf(inicial) >= 0) {
            return control == digito;
        }

        return control == letra || control == digito;
    }

    // Suma los digitos de posicion par tal cual y los de posicion impar duplicados,
    // y devuelve lo que falta para cerrar la decena
    private static int digitoDeControl(String digitos) {

        int suma = 0;

        for (int i = 0; i < 7; i++) {

            int d = Character.getNumericValue(digitos.charAt(i));

            if (i % 2 == 0) {
                d = d * 2;

                if (d > 9) {
                    d = d - 9;
                }
            }
            suma = suma + d;
        }
        return (10 - (suma % 10)) % 10;
    }
}
