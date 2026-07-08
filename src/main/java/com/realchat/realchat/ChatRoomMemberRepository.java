package com.realchat.realchat;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * ChatRoomMemberRepository
 *
 * 채팅방 멤버 관계의 DB 작업 담당.
 *
 * findByUser: 이 유저가 속한 모든 방의 멤버 정보 조회
 *   → "코파가 들어있는 방 목록"
 *
 * findByChatRoom: 이 방에 속한 모든 멤버 정보 조회
 *   → "방1에 누가 있는지"
 *
 * findByChatRoomAndUser: 특정 방에 특정 유저가 있는지 확인
 *   → "코파가 방1에 이미 있어?" (중복 초대 방지)
 */
public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    List<ChatRoomMember> findByUser(User user);

    List<ChatRoomMember> findByChatRoom(ChatRoom chatRoom);

    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    // 특정 방의 특정 유저 멤버 정보 조회 (읽음 처리용)
    Optional<ChatRoomMember> findByChatRoomAndUser(ChatRoom chatRoom, User user);
}