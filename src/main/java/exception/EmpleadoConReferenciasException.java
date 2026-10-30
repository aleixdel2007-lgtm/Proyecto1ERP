package exception;

public class EmpleadoConReferenciasException extends RuntimeException {

    public EmpleadoConReferenciasException() {
        super("No se puede eliminar este empleado porque tiene datos asociados");
    }
}
