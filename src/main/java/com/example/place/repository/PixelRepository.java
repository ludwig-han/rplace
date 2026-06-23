package com.example.place.repository;

import com.example.place.entity.PixelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PixelRepository extends JpaRepository<PixelEntity, Long> {
    Optional<PixelEntity> findByXAndY(int x, int y);
}