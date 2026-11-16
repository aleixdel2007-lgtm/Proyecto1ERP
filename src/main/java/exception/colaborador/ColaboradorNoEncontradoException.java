package exception.colaborador;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ColaboradorNoEncontradoException extends RuntimeException {

    private final Long id;

    public ColaboradorNoEncontradoException(Long id) {
        super("colaboradores.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
