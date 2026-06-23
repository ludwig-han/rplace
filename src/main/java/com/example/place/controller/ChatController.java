package com.example.place.controller;

import com.example.place.entity.ChatMessageEntity;
import com.example.place.repository.ChatRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final ChatRepository chatRepository;

    public ChatController(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public record ChatMessage(String sender, String content, String timestamp, String token) {}
    public record ChatHistoryResponse(String sender, String content, String timestamp, String token) {}

    @MessageMapping("/chat")
    @SendTo("/topic/room")
    public ChatMessage broadcastMessage(ChatMessage message, @Header("simpSessionAttributes") Map<String, Object> sessionAttributes) {

        // 1. DB 영구 저장
        ChatMessageEntity entity = new ChatMessageEntity(message.sender(), message.content(), message.token());
        chatRepository.save(entity);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // 2. ✨ 인터셉터가 넣어둔 유저의 진짜 외부 IP 꺼내기
        String userIp = "UNKNOWN_IP";
        if (sessionAttributes != null && sessionAttributes.containsKey("ip")) {
            userIp = (String) sessionAttributes.get("ip");
        }

        // 3. ✨ 터미널 관리자 전용 초간결 미니멀 로그 메트릭 출력
        String cleanMsg = message.content().replace("\n", " ").replace("\r", " "); // 줄바꿈 한 줄로 펴기
        System.out.printf("[%s] [CHAT] IP: %-15s | TOKEN: %-11s | USER: %-10s | MSG: %s\n",
                currentTime, userIp, message.token(), String.format("%-6s", message.sender()), cleanMsg);

        String clientTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));
        return new ChatMessage(message.sender(), message.content(), clientTime, message.token());
    }

    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatHistoryResponse> getChatHistory() {
        List<ChatMessageEntity> entities = chatRepository.findTop50ByOrderByIdDesc();
        Collections.reverse(entities);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h:mm");
        return entities.stream()
                .map(e -> new ChatHistoryResponse(
                        e.getSender(),
                        e.getContent(),
                        e.getCreatedAt().format(formatter),
                        e.getToken()
                ))
                .collect(Collectors.toList());
    }
}