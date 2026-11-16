package exception.pedido;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNoEncontradoException extends RuntimeException {

    private final Long id;

    public PedidoNoEncontradoException(Long id) {
        super("pedidos.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
