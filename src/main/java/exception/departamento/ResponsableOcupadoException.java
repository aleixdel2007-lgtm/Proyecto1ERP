package exception.departamento;

public class ResponsableOcupadoException extends RuntimeException {

    public ResponsableOcupadoException() {
        super("departamentos.error.responsableOcupado");
    }
}
