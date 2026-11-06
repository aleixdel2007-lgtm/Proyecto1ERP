package exception.departamento;

public class NombreDepartamentoDuplicadoException extends RuntimeException {

    public NombreDepartamentoDuplicadoException() {
        super("Ya existe un departamento con ese nombre");
    }
}
