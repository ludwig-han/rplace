package com.example.place.repository;

import com.example.place.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatRepository extends JpaRepository<ChatMessageEntity, Long> {
    // 💡 백엔드 기술 면접 단골 퀴즈 포인트
    // 가장 최근 메시지 50개를 ID 역순으로 가져오는 마법의 메서드 쿼리문입니다.
    List<ChatMessageEntity> findTop50ByOrderByIdDesc();
}