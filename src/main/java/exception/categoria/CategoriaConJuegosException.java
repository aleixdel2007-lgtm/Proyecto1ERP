package exception.categoria;

public class CategoriaConJuegosException extends RuntimeException {

    public CategoriaConJuegosException() {
        super("categorias.error.conJuegos");
    }
}
