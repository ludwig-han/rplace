package com.example.place.repository;

import com.example.place.entity.PixelHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PixelHistoryRepository extends JpaRepository<PixelHistoryEntity, Long> {
    List<PixelHistoryEntity> findTop10ByXAndYOrderByCreatedAtDesc(int x, int y);
}