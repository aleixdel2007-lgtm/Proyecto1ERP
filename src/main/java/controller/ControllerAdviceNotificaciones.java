package controller;

import dao.ClienteDao;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdviceNotificaciones {

    private final ClienteDao clienteDao;

    // Numero de la campanita al cargar la pagina, leido de la BD
    @ModelAttribute("pendientesClientes")
    public long pendientesClientes(Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return 0;
        }

        // Solo el gestor de clientes y el admin ven estos avisos
        boolean puedeVerlo = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_GESTOR_CLIENTES")
                            || a.getAuthority().equals("ROLE_ADMIN"));

        return puedeVerlo ? clienteDao.contarSolicitudesPendientes() : 0;
    }
}
