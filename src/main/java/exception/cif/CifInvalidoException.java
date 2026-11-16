package exception.cif;

public class CifInvalidoException extends RuntimeException {

    public CifInvalidoException() {
        super("proveedores.error.cifInvalido");
    }
}
