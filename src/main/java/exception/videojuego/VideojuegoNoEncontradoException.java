package exception.videojuego;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VideojuegoNoEncontradoException extends RuntimeException {

    private final Long id;

    public VideojuegoNoEncontradoException(Long id) {
        super("videojuegos.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
