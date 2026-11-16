package exception.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UsuarioNoEncontradoException extends RuntimeException {

    private final String referencia;

    public UsuarioNoEncontradoException(Long id) {
        super("usuarios.error.noExiste");
        this.referencia = String.valueOf(id);
    }

    public UsuarioNoEncontradoException(String email) {
        super("usuarios.error.noExiste");
        this.referencia = email;
    }

    public String getReferencia() {
        return referencia;
    }
}
