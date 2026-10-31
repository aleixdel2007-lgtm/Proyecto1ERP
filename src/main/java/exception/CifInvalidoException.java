package exception;

public class CifInvalidoException extends RuntimeException {

    public CifInvalidoException() {
        super("El CIF no es valido: debe ser una letra, 7 digitos y el caracter de control");
    }
}
