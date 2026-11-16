package exception.clave;

import model.inventario.EstadoClave;

public class ClaveNoAnulableException extends RuntimeException {

    private final EstadoClave estado;

    public ClaveNoAnulableException(EstadoClave estado) {
        super("claves.error.noAnulable");
        this.estado = estado;
    }

    public EstadoClave getEstado() {
        return estado;
    }
}
