package com.example.place.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("*")
                // 💡 [수정] 직접 만든 IpHandshakeInterceptor를 장착하여 진짜 IP를 낚아챕니다!
                .addInterceptors(new IpHandshakeInterceptor());
        // 🚨 [수정] SockJS 내부 에러 방지를 위해 .withSockJS()는 과감히 걷어냅니다.
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic");
    }
}