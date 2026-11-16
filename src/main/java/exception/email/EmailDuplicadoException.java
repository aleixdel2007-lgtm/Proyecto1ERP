package exception.email;

public class EmailDuplicadoException extends RuntimeException {

    public EmailDuplicadoException() {
        super("usuarios.error.correoDuplicado");
    }
}
