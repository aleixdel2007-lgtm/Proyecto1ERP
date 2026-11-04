package exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlataformaNoEncontradaException extends RuntimeException {

    public PlataformaNoEncontradaException(Long id) {
        super("No existe la plataforma " + id);
    }
}
