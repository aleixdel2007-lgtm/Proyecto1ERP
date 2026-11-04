package exception;

public class CodigoPlataformaDuplicadoException extends RuntimeException {

    public CodigoPlataformaDuplicadoException() {
        super("Ya existe una plataforma con ese codigo");
    }
}
