package exception.videojuego;

public class VideojuegoConReferenciasException extends RuntimeException {

    public VideojuegoConReferenciasException() {
        super("No se puede eliminar este videojuego porque tiene claves o pedidos asociados");
    }
}
