package exception.departamento;

public class NombreDepartamentoDuplicadoException extends RuntimeException {

    public NombreDepartamentoDuplicadoException() {
        super("departamentos.error.nombreDuplicado");
    }
}
