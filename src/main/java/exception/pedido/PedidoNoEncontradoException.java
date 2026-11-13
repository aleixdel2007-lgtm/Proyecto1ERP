package exception.pedido;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNoEncontradoException extends RuntimeException {

    private final Long id;

    public PedidoNoEncontradoException(Long id) {
        super("No existe el pedido " + id);
        this.id = id;
    }

    // Para que quien lo captura pueda escribir el aviso en el idioma del usuario
    public Long getId() {
        return id;
    }
}
