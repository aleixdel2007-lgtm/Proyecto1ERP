package exception.departamento;

public class DepartamentoConTrabajadoresException extends RuntimeException {

    public DepartamentoConTrabajadoresException() {
        super("departamentos.error.conTrabajadores");
    }
}
