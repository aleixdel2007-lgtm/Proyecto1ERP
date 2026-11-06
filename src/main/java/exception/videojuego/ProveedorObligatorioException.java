package exception.videojuego;

public class ProveedorObligatorioException extends RuntimeException {

    public ProveedorObligatorioException() {
        super("Hay que indicar el proveedor que suministra el juego");
    }
}
