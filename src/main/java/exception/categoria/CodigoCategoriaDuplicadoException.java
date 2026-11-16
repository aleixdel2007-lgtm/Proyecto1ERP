package exception.categoria;

public class CodigoCategoriaDuplicadoException extends RuntimeException {

    public CodigoCategoriaDuplicadoException() {
        super("categorias.error.codigoDuplicado");
    }
}
