package com.example.place.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pixels")
public class PixelEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int x;
    private int y;
    private String color;
    private String nickname;
    private LocalDateTime updatedAt;

    public PixelEntity() {}

    public PixelEntity(int x, int y, String color, String nickname) {
        this.x = x;
        this.y = y;
        this.color = color;
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public int getX() { return x; }
    public int getY() { return y; }
    public String getColor() { return color; }
    public String getNickname() { return nickname; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String color, String nickname) {
        this.color = color;
        this.nickname = nickname;
        this.updatedAt = LocalDateTime.now();
    }
}