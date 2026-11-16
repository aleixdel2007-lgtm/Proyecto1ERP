package exception.categoria;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CategoriaNoEncontradaException extends RuntimeException {

    private final Long id;

    public CategoriaNoEncontradaException(Long id) {
        super("categorias.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
