package exception.imagen;

public class ImagenInvalidaException extends RuntimeException {

    public ImagenInvalidaException(String mensaje) {
        super(mensaje);
    }
}
