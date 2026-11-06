package exception.colaborador;

public class ColaboradorConJuegosException extends RuntimeException {

    public ColaboradorConJuegosException() {
        super("No se puede eliminar un colaborador con juegos publicados");
    }
}
