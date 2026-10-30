package exception;

public class ResponsableOcupadoException extends RuntimeException {

    public ResponsableOcupadoException() {
        super("Ese trabajador ya es responsable de otro departamento");
    }
}
