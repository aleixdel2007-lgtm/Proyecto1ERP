package exception;

public class ProveedorConCuentaException extends RuntimeException {

    public ProveedorConCuentaException() {
        super("No se puede eliminar un proveedor con cuenta de acceso vinculada");
    }
}
