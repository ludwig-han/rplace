package com.example.place.controller;

import com.example.place.entity.ChatMessageEntity;
import com.example.place.repository.ChatRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ChatController {

    private final ChatRepository chatRepository;

    // 생성자 주입 (Spring 프레임워크가 자장 권장하는 DI 방식)
    public ChatController(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public record ChatMessage(String sender, String content, String timestamp, String token) {}
    public record ChatHistoryResponse(String sender, String content, String timestamp, String token) {}

    @MessageMapping("/chat")
    @SendTo("/topic/room")
    public ChatMessage broadcastMessage(ChatMessage message) {

        // 1. 실시간 메시지 패킷이 들어오는 순간 DB에 영구 저장
        ChatMessageEntity entity = new ChatMessageEntity(message.sender(), message.content(), message.token());
        chatRepository.save(entity);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("a h:mm"));
        String ip = "127.0.0.1";

        System.out.println("🚨 [접속기록] [" + currentTime + "] IP: " + ip + " | 토큰: [" + message.token() + "] | 보낸이: " + message.sender() + " | 내용: " + message.content());

        return new ChatMessage(message.sender(), message.content(), currentTime, message.token());
    }

    // ✨ 2. 최초 입장한 유저를 위한 과거 대화 백업 파일 쏘아주기 엔드포인트
    @GetMapping("/api/chat/history")
    @ResponseBody
    public List<ChatHistoryResponse> getChatHistory() {
        List<ChatMessageEntity> entities = chatRepository.findTop50ByOrderByIdDesc();

        // 💡 알고리즘 트랩 해결: 최신 50개를 역순(Desc)으로 긁어왔으므로,
        // 타임라인대로 위에 배치하기 위해 리스트를 정상 순서(과거 -> 현재)로 뒤집어줍니다.
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