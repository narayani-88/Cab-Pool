package com.cabpool.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Topic for public / group broadcasts, queue for individual rider targeted messages
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Standard WebSocket & SockJS fallback for frontend web/mobile clients
        registry.addEndpoint("/ws-cabpool")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // Plain WebSocket endpoint without SockJS wrapper (convenient for testing via wscat/Postman)
        registry.addEndpoint("/ws-cabpool/raw")
                .setAllowedOriginPatterns("*");
    }
}
