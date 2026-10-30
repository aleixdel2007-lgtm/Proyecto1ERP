package exception;

public class DepartamentoConTrabajadoresException extends RuntimeException {

    public DepartamentoConTrabajadoresException() {
        super("No se puede eliminar un departamento con trabajadores asociados");
    }
}
