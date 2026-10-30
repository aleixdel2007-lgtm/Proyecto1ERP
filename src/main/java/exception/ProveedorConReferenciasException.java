package exception;

public class ProveedorConReferenciasException extends RuntimeException {

    public ProveedorConReferenciasException() {
        super("No se puede eliminar este proveedor porque tiene datos asociados");
    }
}
