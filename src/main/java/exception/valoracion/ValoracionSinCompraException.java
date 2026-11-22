package exception.valoracion;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class ValoracionSinCompraException extends RuntimeException {

    private final Long videojuegoId;

    public ValoracionSinCompraException(Long videojuegoId) {
        super("valoraciones.error.sinCompra");
        this.videojuegoId = videojuegoId;
    }

    public Long getVideojuegoId() {
        return videojuegoId;
    }
}
