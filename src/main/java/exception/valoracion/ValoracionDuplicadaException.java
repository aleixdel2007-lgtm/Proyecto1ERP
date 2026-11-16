package exception.valoracion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ValoracionDuplicadaException extends RuntimeException {

    private final Long videojuegoId;

    public ValoracionDuplicadaException(Long videojuegoId) {
        super("valoraciones.error.duplicada");
        this.videojuegoId = videojuegoId;
    }

    public Long getVideojuegoId() {
        return videojuegoId;
    }
}
