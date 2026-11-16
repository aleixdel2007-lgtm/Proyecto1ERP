package exception.precio;

public class PrecioDescuentoInvalidoException extends RuntimeException {

    public PrecioDescuentoInvalidoException() {
        super("precios.error.descuentoInvalido");
    }
}
