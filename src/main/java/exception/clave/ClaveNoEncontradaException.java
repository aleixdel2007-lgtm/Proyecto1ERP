package exception.clave;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClaveNoEncontradaException extends RuntimeException {

    private final Long id;

    public ClaveNoEncontradaException(Long id) {
        super("claves.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
