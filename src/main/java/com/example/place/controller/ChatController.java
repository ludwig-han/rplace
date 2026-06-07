package com.example.place.controller;

import com.example.place.entity.ChatMessageEntity;
import com.example.place.repository.ChatRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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

        // 2. ✨ 웹소켓 세션에서 실제 접속한 사용자의 사설 IP 추출 (실패 시 로컬 표기)
        String userIp = "UNKNOWN_IP";
        if (sessionAttributes != null && sessionAttributes.containsKey("ip")) {
            userIp = (String) sessionAttributes.get("ip");
        }

        // 3. ✨ 터미널 관리자 전용 초간결 미니멀 로그 메트릭 출력
        // 📝 ChatController.java 내부의 printf 문을 이 코드로 통째로 교체!
        String cleanMsg = message.content().replace("\n", " ").replace("\r", " "); // 유저가 친 줄바꿈 한 줄로 펴기
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