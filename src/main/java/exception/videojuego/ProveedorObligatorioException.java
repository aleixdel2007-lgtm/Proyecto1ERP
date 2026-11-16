package exception.videojuego;

public class ProveedorObligatorioException extends RuntimeException {

    public ProveedorObligatorioException() {
        super("videojuegos.error.proveedorObligatorio");
    }
}
