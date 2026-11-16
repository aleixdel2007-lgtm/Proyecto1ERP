package exception.empleado;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class EmpleadoNoEncontradoException extends RuntimeException {

    private final Long id;

    public EmpleadoNoEncontradoException(Long id) {
        super("empleados.error.noExiste");
        this.id = id;
    }

    public Long getId() {
        return id;
    }
}
