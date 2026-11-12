package exception.clave;

import model.inventario.EstadoClave;

public class ClaveNoAnulableException extends RuntimeException {

    public ClaveNoAnulableException(EstadoClave estado) {
        super("Solo se puede anular una clave DISPONIBLE, y esta esta " + estado);
    }
}
