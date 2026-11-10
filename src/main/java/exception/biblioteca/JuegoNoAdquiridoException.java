package exception.biblioteca;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class JuegoNoAdquiridoException extends RuntimeException {

    public JuegoNoAdquiridoException(Long videojuegoId) {
        super("El videojuego " + videojuegoId + " no esta en tu biblioteca");
    }
}
