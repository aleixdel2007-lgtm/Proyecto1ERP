package exception.compra;

import model.aprovisionamiento.EstadoCompra;

public class EstadoCompraInvalidoException extends RuntimeException {

    private final String accion;
    private final EstadoCompra estado;

    public EstadoCompraInvalidoException(String accion, EstadoCompra estado) {
        super("compras.error.estadoInvalido");
        this.accion = accion;
        this.estado = estado;
    }

    public String getAccion() {
        return accion;
    }

    public EstadoCompra getEstado() {
        return estado;
    }
}
