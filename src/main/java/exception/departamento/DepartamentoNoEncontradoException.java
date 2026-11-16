package exception.departamento;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class DepartamentoNoEncontradoException extends RuntimeException {

    private final Long id;

    public DepartamentoNoEncontradoException(Long id) {
        super("departamentos.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
