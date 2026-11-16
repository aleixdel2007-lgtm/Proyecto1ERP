package exception.precio;

public class PrecioInvalidoException extends RuntimeException {

    public PrecioInvalidoException() {
        super("precios.error.negativo");
    }
}
