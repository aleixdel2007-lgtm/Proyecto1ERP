package exception.cliente;

public class ClienteConDatosAsociadosException extends RuntimeException {

    public ClienteConDatosAsociadosException() {
        super("clientes.error.conDatos");
    }
}
