package exception.precio;

public class PrecioInvalidoException extends RuntimeException {

    public PrecioInvalidoException() {
        super("El precio no puede ser negativo");
    }
}
