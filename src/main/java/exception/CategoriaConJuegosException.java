package exception;

public class CategoriaConJuegosException extends RuntimeException {

    public CategoriaConJuegosException() {
        super("No se puede eliminar una categoria que tiene juegos asignados");
    }
}
