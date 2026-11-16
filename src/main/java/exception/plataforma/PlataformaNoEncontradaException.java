package exception.plataforma;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlataformaNoEncontradaException extends RuntimeException {

    private final Long id;

    public PlataformaNoEncontradaException(Long id) {
        super("plataformas.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
