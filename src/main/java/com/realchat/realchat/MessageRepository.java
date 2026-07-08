package com.realchat.realchat;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
/*
 * MessageRepository -> 그 테이블에 저장/조회/삭제 (실제 작업)
 * 
 * Message 엔티티의 DB 작업 (저장, 조회, 삭제)을 담당하는 인터페이스 이다.
 * JpaRepository를 상속 받으면 직접 SQL을 작성하지 않아도
 * save(), findAll(), findById(), delete() 등의 메서드를 바로 사용할 수 있다.
 * 
 * <Message, Long> : 
 * 			- Message: 어떤 엔티티를 다루는지
 * 			- Long: 그 엔티티의 PK 타입 (id 필드가 Long 이다)
 * 
 * findByRoomIdOrderByCreatedAtAsc: 특정 방의 메시지를 시간순으로 조회
 *   → "방1의 메시지를 오래된 순서대로 가져와"
 * 
 */
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    long countByRoomIdAndIdGreaterThan(Long roomId, Long messageId);

    Message findTopByRoomIdOrderByIdDesc(Long roomId);

    // 방 삭제 시 해당 방 메시지 전체 삭제
    void deleteByRoomId(Long roomId);
}