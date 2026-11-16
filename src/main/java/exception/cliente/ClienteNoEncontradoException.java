package exception.cliente;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ClienteNoEncontradoException extends RuntimeException {

    private final Long id;

    public ClienteNoEncontradoException(Long id) {
        super("clientes.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
