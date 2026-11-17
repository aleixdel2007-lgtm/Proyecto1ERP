package controller;

import dao.ClienteDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

// La campanita sale en todas las paginas del panel, asi que el numero de avisos pendientes
// se pone en el modelo aqui, igual que la foto de perfil
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdviceNotificaciones {

    private final ClienteDao clienteDao;

    // Numero que sale en la campanita al cargar la pagina (viene de la BD, no del WebSocket)
    @ModelAttribute("pendientesClientes")
    public long pendientesClientes(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }

        boolean puedeVerlo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR_CLIENTES")
                            || a.getAuthority().equals("ROLE_ADMIN"));

        return puedeVerlo ? clienteDao.contarSolicitudesPendientes() : 0;
    }
}