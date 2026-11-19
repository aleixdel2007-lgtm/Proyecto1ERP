/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package config;

/**
 *
 * @author Sandy
 */
import model.seguridad.Rol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

// El portero de los canales del WebSocket. Los mensajes que llegan del navegador pasan por
// aqui antes de llegar al broker. Las URL de SecurityConfig no protegen los canales /topic/...,
// asi que la proteccion por rol se hace en este sitio.
@Component
public class GuardiaCanales implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(GuardiaCanales.class);

    @Override
    public Message<?> preSend(Message<?> mensaje, MessageChannel canal) {

        StompHeaderAccessor cabecera = MessageHeaderAccessor.getAccessor(mensaje, StompHeaderAccessor.class);

        if (cabecera == null) {
            return mensaje;
        }

        // Nadie envia mensajes desde el navegador: solo el servidor publica avisos.
        // Sin esto, cualquier usuario con sesion podria colar un aviso falso en un canal
        if (StompCommand.SEND.equals(cabecera.getCommand())) {
            log.warn("Mensaje bloqueado: un navegador intento publicar en {}", cabecera.getDestination());
            return null;
        }

        // Al suscribirse se comprueba el rol. El resto de mensajes (conectar, desconectar...) pasan
        if (StompCommand.SUBSCRIBE.equals(cabecera.getCommand())) {

            // El usuario es el que inicio sesion al abrir la pagina, y viaja con la conexion
            Authentication usuario = cabecera.getUser() instanceof Authentication a ? a : null;

            if (!puedeSuscribirse(usuario, cabecera.getDestination())) {
                log.warn("Suscripcion bloqueada: {} no puede entrar en {}",
                        usuario == null ? "nadie identificado" : usuario.getName(), cabecera.getDestination());
                // Devolver null descarta el mensaje: no queda suscrito y no recibe nada
                return null;
            }
        }
        return mensaje;
    }

    // Que roles pueden escuchar cada canal. Un canal que no este aqui no lo puede escuchar nadie
    private boolean puedeSuscribirse(Authentication usuario, String destino) {

        if (usuario == null || destino == null) {
            return false;
        }

        return switch (destino) {
            case "/topic/clientes" ->
                tieneRol(usuario, Rol.GESTOR_CLIENTES);
            case "/topic/productos" ->
                tieneRol(usuario, Rol.GESTOR_PRODUCTOS);
            default ->
                false;
        };
    }

    // Spring guarda los roles con "ROLE_" delante
    private boolean tieneRol(Authentication usuario, Rol rol) {
        return usuario.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + rol.name()));
    }
}
