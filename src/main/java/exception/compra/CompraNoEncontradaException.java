package exception.compra;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CompraNoEncontradaException extends RuntimeException {

    private final Long id;

    public CompraNoEncontradaException(Long id) {
        super("compras.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
