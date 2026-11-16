package exception.empleado;

public class EmpleadoConReferenciasException extends RuntimeException {

    public EmpleadoConReferenciasException() {
        super("empleados.error.conReferencias");
    }
}
