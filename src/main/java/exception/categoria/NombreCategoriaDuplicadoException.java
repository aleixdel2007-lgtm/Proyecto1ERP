package exception.categoria;

public class NombreCategoriaDuplicadoException extends RuntimeException {

    public NombreCategoriaDuplicadoException() {
        super("categorias.error.nombreDuplicado");
    }
}
