package com.realchat.realchat;

import org.springframework.data.jpa.repository.JpaRepository;

/*
 * ChatRoomRepository
 * 
 * ChatRoom 엔티티의 DB 작업 담당.
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

}