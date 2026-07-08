package com.realchat.realchat;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/*
 * User 엔티티
 *
 * online: 접속 중이면 true
 * lastActiveAt: 마지막 활동 시간
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    private boolean online;

    private LocalDateTime lastActiveAt;

    public User() {
    }

    public User(String username, String nickname, String password) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.online = false;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public String getPassword() { return password; }
    public boolean isOnline() { return online; }
    public LocalDateTime getLastActiveAt() { return lastActiveAt; }

    public void setOnline(boolean online) { this.online = online; }
    public void setLastActiveAt(LocalDateTime lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}