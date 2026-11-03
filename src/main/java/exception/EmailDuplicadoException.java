package exception;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException() {
        super("Ese correo ya esta en uso");
    }
}
