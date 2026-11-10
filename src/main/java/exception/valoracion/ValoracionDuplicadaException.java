package exception.valoracion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ValoracionDuplicadaException extends RuntimeException {

    public ValoracionDuplicadaException(Long videojuegoId) {
        super("Ya has valorado el videojuego " + videojuegoId);
    }
}
