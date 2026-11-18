package config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    // El portero que decide quien puede suscribirse a cada canal
    private final GuardiaCanales guardia;

    // Direccion a la que se conecta el navegador para abrir el WebSocket
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
    }

    // Broker en memoria: lo que el servidor publica en un canal /topic/... llega a todos los suscritos
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
    }

    // Pone al portero en la cola de entrada: cada mensaje que llega del navegador pasa por el
    // antes de llegar al broker
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(guardia);
    }
}