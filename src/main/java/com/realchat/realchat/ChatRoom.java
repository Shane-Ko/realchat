package com.realchat.realchat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * ChatRoom 엔티티
 *
 * 채팅방 하나를 나타내는 클래스.
 * - DM: 1:1 채팅방 (두 명만)
 * - GROUP: 단체 채팅방 (여러 명)
 */
@Entity
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;  // "DM" 또는 "GROUP"

    private String title;  // 방 이름 (DM은 null, GROUP은 방 제목)

    private LocalDateTime createdAt;

    public ChatRoom() {
    }

    public ChatRoom(String type, String title) {
        this.type = type;
        this.title = title;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}