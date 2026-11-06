package exception.videojuego;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VideojuegoNoEncontradoException extends RuntimeException {

    public VideojuegoNoEncontradoException(Long id) {
        super("No existe el videojuego " + id);
    }
}
