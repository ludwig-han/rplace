package com.example.place.controller;

import com.example.place.entity.PixelEntity;
import com.example.place.entity.PixelHistoryEntity;
import com.example.place.repository.PixelHistoryRepository;
import com.example.place.repository.PixelRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class PixelController {

    private final PixelRepository pixelRepository;
    private final PixelHistoryRepository pixelHistoryRepository;

    public PixelController(PixelRepository pixelRepository, PixelHistoryRepository pixelHistoryRepository) {
        this.pixelRepository = pixelRepository;
        this.pixelHistoryRepository = pixelHistoryRepository;
    }

    // 픽셀 찍기 요청/응답 구조
    public record PixelMessage(int x, int y, String color, String nickname) {}

    // 히스토리 응답 구조
    public record PixelHistoryResponse(String nickname, String color, String time) {}

    // 1. 픽셀 찍기 (WebSocket)
    @MessageMapping("/pixel")
    @SendTo("/topic/pixel")
    public PixelMessage drawPixel(PixelMessage message) {

        // 현재 상태 업데이트
        Optional<PixelEntity> existing = pixelRepository.findByXAndY(message.x(), message.y());
        if (existing.isPresent()) {
            existing.get().update(message.color(), message.nickname());
            pixelRepository.save(existing.get());
        } else {
            pixelRepository.save(new PixelEntity(message.x(), message.y(), message.color(), message.nickname()));
        }

        // 히스토리 기록
        pixelHistoryRepository.save(new PixelHistoryEntity(message.x(), message.y(), message.color(), message.nickname()));

        return message;
    }

    // 2. 전체 캔버스 불러오기 (HTTP)
    @GetMapping("/api/pixels")
    @ResponseBody
    public List<PixelMessage> getAllPixels() {
        return pixelRepository.findAll().stream()
                .map(p -> new PixelMessage(p.getX(), p.getY(), p.getColor(), p.getNickname()))
                .collect(Collectors.toList());
    }

    // 3. 특정 픽셀 히스토리 조회 (HTTP)
    @GetMapping("/api/pixel/history")
    @ResponseBody
    public List<PixelHistoryResponse> getPixelHistory(@RequestParam int x, @RequestParam int y) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm");
        return pixelHistoryRepository.findTop10ByXAndYOrderByCreatedAtDesc(x, y)
                .stream()
                .map(h -> new PixelHistoryResponse(h.getNickname(), h.getColor(), h.getCreatedAt().format(formatter)))
                .collect(Collectors.toList());
    }
}