package com.example.place.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // 이 애노테이션이 스프링 부트의 웹소켓 브로커 기능을 활성화합니다.
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")
                .setAllowedOriginPatterns("*")
                // 💡 [이 한 줄 추가!] 접속 시 손님의 IP 주소를 가로채서 웹소켓 세션에 쑤셔 넣습니다.
                .addInterceptors(new org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 2. 클라이언트가 서버로 메시지를 보낼 때(Publish) 주소 앞에 붙여야 하는 접두사(Prefix)를 설정합니다.
        // 예를 들어 채팅을 보낼 때 주소는 /app/chat 형식이 됩니다.
        registry.setApplicationDestinationPrefixes("/app");

        // 3. 서버가 메세지를 방송(Broadcast)할 때 유저들이 구독(Subscribe)할 주소의 접두사를 설정합니다.
        // 단톡방에 입장한 유저들은 /topic/room 이라는 주소를 귀 기울여 듣게(구독) 됩니다.
        // 우선은 가볍게 내장된 인메모리 심플 브로커를 켭니다.
        registry.enableSimpleBroker("/topic");
    }
}