package exception;

public class NombrePlataformaDuplicadoException extends RuntimeException {

    public NombrePlataformaDuplicadoException() {
        super("Ya existe una plataforma con ese nombre");
    }
}
