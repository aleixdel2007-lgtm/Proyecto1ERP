package exception.compra;

public class LineaCompraInvalidaException extends RuntimeException {

    public LineaCompraInvalidaException(String motivo) {
        super(motivo);
    }
}
