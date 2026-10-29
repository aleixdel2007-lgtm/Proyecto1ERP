package exception;

public class CifDuplicadoException extends RuntimeException {

    public CifDuplicadoException() {
        super("Ya existe un proveedor con ese CIF");
    }
}
