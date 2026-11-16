package exception.cif;

public class CifDuplicadoException extends RuntimeException {

    public CifDuplicadoException() {
        super("proveedores.error.cifDuplicado");
    }
}
