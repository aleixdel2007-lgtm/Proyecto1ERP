package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import service.interfaces.ClienteService;
import service.interfaces.VideojuegoService;

// Las campanitas salen en las paginas del panel para los roles que gestionan esos avisos.
// En vez de calcular los numeros en cada controller, se calculan aqui una vez y Spring los
// mete en el modelo de todas las paginas. Los numeros los da el service; aqui solo se
// decide a quien se le ensenan. Para el resto de roles devuelven 0.
@ControllerAdvice
@RequiredArgsConstructor
public class ControllerAdviceNotificaciones {

    private final ClienteService clienteService;
    private final VideojuegoService videojuegoService;

    // Solicitudes de activacion de cuenta pendientes (campana del gestor de clientes)
    @ModelAttribute("pendientesClientes")
    public long pendientesClientes(Authentication authentication) {
        return tieneRol(authentication, "GESTOR_CLIENTES")
                ? clienteService.contarSolicitudesPendientes() : 0;
    }

    // Juegos de colaboradores pendientes de revision (campana del gestor de productos)
    @ModelAttribute("pendientesJuegos")
    public long pendientesJuegos(Authentication authentication) {
        return tieneRol(authentication, "GESTOR_PRODUCTOS")
                ? videojuegoService.contarPendientesRevision() : 0;
    }

    // Vale si el usuario tiene ese rol o es ADMIN. Spring guarda los roles con "ROLE_" delante
    private boolean tieneRol(Authentication authentication, String rol) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol)
                            || a.getAuthority().equals("ROLE_ADMIN"));
    }
}