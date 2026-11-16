package exception.imagen;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImagenNoEncontradaException extends RuntimeException {

    private final Long id;

    public ImagenNoEncontradaException(Long id) {
        super("imagenes.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
