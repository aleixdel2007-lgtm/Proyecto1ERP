package exception.categoria;

public class CodigoCategoriaDuplicadoException extends RuntimeException {

    public CodigoCategoriaDuplicadoException() {
        super("Ya existe una categoria con ese codigo");
    }
}
