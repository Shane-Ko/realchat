package com.realchat.realchat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * ChatRoomMember 엔티티
 *
 * 채팅방과 유저의 관계.
 * lastReadMessageId: 이 유저가 이 방에서 마지막으로 읽은 메시지 ID
 *   → 안 읽은 수 = 방의 최신 메시지 id - lastReadMessageId
 */
@Entity
@Table(name = "chat_room_member")
public class ChatRoomMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Long lastReadMessageId;

    private LocalDateTime joinedAt;

    public ChatRoomMember() {
    }

    public ChatRoomMember(ChatRoom chatRoom, User user) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.lastReadMessageId = 0L;
        this.joinedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ChatRoom getChatRoom() { return chatRoom; }
    public User getUser() { return user; }
    public Long getLastReadMessageId() { return lastReadMessageId; }
    public LocalDateTime getJoinedAt() { return joinedAt; }

    // 읽음 처리 시 업데이트
    public void setLastReadMessageId(Long lastReadMessageId) {
        this.lastReadMessageId = lastReadMessageId;
    }
}