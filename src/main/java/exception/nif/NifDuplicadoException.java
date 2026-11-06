package exception.nif;

public class NifDuplicadoException extends RuntimeException {

    public NifDuplicadoException() {
        super("Ya existe un empleado con ese NIF");
    }
}
