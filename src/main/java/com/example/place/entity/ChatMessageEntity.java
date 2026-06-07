package com.example.place.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto Increment
    private Long id;

    private String sender;

    @Column(columnDefinition = "TEXT") // 긴 텍스트 대비
    private String content;

    private String token;
    private LocalDateTime createdAt;

    public ChatMessageEntity() {}

    public ChatMessageEntity(String sender, String content, String token) {
        this.sender = sender;
        this.content = content;
        this.token = token;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public Long getId() { return id; }
    public String getSender() { return sender; }
    public String getContent() { return content; }
    public String getToken() { return token; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}