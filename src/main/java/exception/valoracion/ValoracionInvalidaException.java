package exception.valoracion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ValoracionInvalidaException extends RuntimeException {

    public ValoracionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
