package exception.biblioteca;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DescargaNoDisponibleException extends RuntimeException {

    public DescargaNoDisponibleException() {
        super("biblioteca.error.sinArchivo");
    }
}
