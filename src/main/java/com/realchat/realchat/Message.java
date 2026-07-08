package com.realchat.realchat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

/*  
 * Message 엔티티 -> 테이블이 무엇인지 정의 (설계도)
 * 
 * 채팅 메시지 한 건을 나타내는 클래스 이다.
 * 이 클래스가 MySQL의 message 테이블과 1:1로 매핑된다.
 * 
 * @Entity: 이 클래스를 DB 테이블로 등록
 * @Id + @GeneratedValue : id 필드를 PrimaryKey + AUTO_INCREMENT 로 설정
 * 필드(sender, content, createdAt) 이 message 테이블의 컬럼이 된다
 * 매개변수 생성자: 새 메세지를 만들 때 편하게 사용위함
 * Getter: Spring 이 JSON 변환할 때 값을 읽어가기 위해 필요
 * 
 * Spring Boot 실행 시 application.properies 의 ddl-auto=update 설정에 의해
 * 이 클래스를 보고 MySQL 에 message 테이블이 자동 생성 된다.
 * 
 */


@Entity
public class Message {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	private Long roomId;
	
	private String sender;
	
	private String content;
	
	private LocalDateTime createdAt;
	
	// 기본 생성자 (JPA 가 필요로 함)
	public Message() {
		
	}
	
	public Message(Long roomId, String sender, String content) {
	    this.roomId = roomId;
	    this.sender = sender;
	    this.content = content;
	    this.createdAt = LocalDateTime.now();
	}
	
	// Getter
	public Long getId() {
		return id;
	}
	
	public Long getRoomId() { return roomId; }
	
	public String getSender() {
		return sender;
	}
	
	public String getContent() {
		return content;
	}
	
	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
