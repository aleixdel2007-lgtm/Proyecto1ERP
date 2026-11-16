package config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

// Configura los WebSockets de la aplicacion, Sin esta clase el starter de
// websocket esta en el pom pero no hace nada
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // La "puerta": la direccion a la que se conecta el navegador para abrir el WebSocket
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
    }

       // El cartero (broker): reparte los mensajes de los canales cuyo nombre empieza por
    // /topic, por ejemplo /topic/clientes. Lo que el servidor deja en uno de esos canales
    // le llega a todos los navegadores que estan suscritos a el
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }
}
