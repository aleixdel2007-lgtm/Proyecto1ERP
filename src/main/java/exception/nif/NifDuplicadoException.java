package exception.nif;

public class NifDuplicadoException extends RuntimeException {

    public NifDuplicadoException() {
        super("empleados.error.nifDuplicado");
    }
}
