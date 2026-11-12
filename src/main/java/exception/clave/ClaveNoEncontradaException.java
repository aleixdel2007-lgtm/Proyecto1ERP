package exception.clave;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClaveNoEncontradaException extends RuntimeException {

    public ClaveNoEncontradaException(Long id) {
        super("No existe la clave de licencia " + id);
    }
}
