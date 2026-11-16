package exception.proveedor;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProveedorNoEncontradoException extends RuntimeException {

    private final Long id;

    public ProveedorNoEncontradoException(Long id) {
        super("proveedores.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
