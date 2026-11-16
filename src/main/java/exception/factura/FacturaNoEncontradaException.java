package exception.factura;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FacturaNoEncontradaException extends RuntimeException {

    private final Long id;

    public FacturaNoEncontradaException(Long id) {
        super("facturas.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
