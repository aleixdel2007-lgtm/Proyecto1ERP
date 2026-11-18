package exception.cliente;

public class MotivoInactivacionObligatorioException extends RuntimeException {

    public MotivoInactivacionObligatorioException() {
        super("clientes.error.motivoObligatorio");
    }
}
