package com.example.place.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
public class ChatController {

    // 1. 규격에 유저 식별용 token 필드 추가
    public record ChatMessage(String sender, String content, String timestamp, String token) {}

    @MessageMapping("/chat")
    @SendTo("/topic/room")
    public ChatMessage broadcastMessage(ChatMessage message) {

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));
        String ip = "127.0.0.1"; // 실제 배포 시 공인 IP 파싱

        // 2. 로그에 유저 고유 토큰(token)을 같이 박아버리기!
        // 예: [usr_x82fba91] 익명1 : 안녕
        System.out.println("🚨 [접속기록] [" + currentTime + "] IP: " + ip + " | 토큰: [" + message.token() + "] | 보낸이: " + message.sender() + " | 내용: " + message.content());

        return new ChatMessage(message.sender(), message.content(), currentTime, message.token());
    }
}