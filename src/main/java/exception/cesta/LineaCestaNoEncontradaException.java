package exception.cesta;

public class LineaCestaNoEncontradaException extends RuntimeException {

    private final Long id;

    public LineaCestaNoEncontradaException(Long id) {
        super("cesta.error.lineaNoExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
