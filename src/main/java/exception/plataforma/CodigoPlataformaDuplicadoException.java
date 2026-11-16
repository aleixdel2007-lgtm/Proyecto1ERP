package exception.plataforma;

public class CodigoPlataformaDuplicadoException extends RuntimeException {

    public CodigoPlataformaDuplicadoException() {
        super("plataformas.error.codigoDuplicado");
    }
}
