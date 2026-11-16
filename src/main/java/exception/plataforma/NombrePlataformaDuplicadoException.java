package exception.plataforma;

public class NombrePlataformaDuplicadoException extends RuntimeException {

    public NombrePlataformaDuplicadoException() {
        super("plataformas.error.nombreDuplicado");
    }
}
