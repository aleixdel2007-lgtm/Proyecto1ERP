package exception.precio;

public class PrecioDescuentoInvalidoException extends RuntimeException {

    public PrecioDescuentoInvalidoException() {
        super("El precio con descuento tiene que ser menor que el precio estandar");
    }
}
