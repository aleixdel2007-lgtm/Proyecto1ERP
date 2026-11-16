package exception.pedido;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class PedidoNoEncontradoException extends RuntimeException {

    private final Long id;

    public PedidoNoEncontradoException(Long id) {
        // El mensaje es la clave de traduccion, como en el resto de excepciones. Quien la
        // captura en la tienda (ControllerPago) usa ademas pago.error.pedidoNoExiste, que
        // lleva el id como {0}; en el panel basta con esta.
        super("pedidos.error.noExiste");
        this.id = id;
    }

    // Para que quien lo captura pueda escribir el aviso en el idioma del usuario
    public Long getId() {
        return id;
    }
}
