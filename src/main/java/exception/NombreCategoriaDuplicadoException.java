package exception;

public class NombreCategoriaDuplicadoException extends RuntimeException {

    public NombreCategoriaDuplicadoException() {
        super("Ya existe una categoria con ese nombre");
    }
}
