package exception.imagen;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ImagenNoEncontradaException extends RuntimeException {

    public ImagenNoEncontradaException(Long id) {
        super("No existe la imagen " + id);
    }
}
