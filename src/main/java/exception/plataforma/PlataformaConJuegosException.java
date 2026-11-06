package exception.plataforma;

public class PlataformaConJuegosException extends RuntimeException {

    public PlataformaConJuegosException() {
        super("No se puede eliminar una plataforma que tiene juegos asignados");
    }
}
