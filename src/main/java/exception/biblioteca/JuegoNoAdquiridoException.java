package exception.biblioteca;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class JuegoNoAdquiridoException extends RuntimeException {

    private final Long videojuegoId;

    public JuegoNoAdquiridoException(Long videojuegoId) {
        super("biblioteca.error.noAdquirido");
        this.videojuegoId = videojuegoId;
    }

    public Long getVideojuegoId() {
        return videojuegoId;
    }
}
