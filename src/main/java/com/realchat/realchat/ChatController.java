package com.realchat.realchat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/*
 * ChatController
 *
 * 채팅 메시지의 전체 흐름을 처리하는 클래스.
 *
 * 1. 브라우저가 /app/chat.send 로 메시지를 보냄
 * 2. 이 클래스가 받아서 Message 객체 생성
 * 3. Repository를 통해 MySQL에 저장
 * 4. /topic/messages 구독자 전원에게 브로드캐스트
 *
 * @Controller: 이 클래스가 요청을 받는 역할임을 Spring에 알림
 */

@Controller
public class ChatController {
	
	private final MessageRepository messageRepository;
	
	// 생성자
	public ChatController(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}
	
    // @MessageMapping: /app/chat.send 로 오는 메시지를 이 메서드가 처리
    // @SendTo: 처리 결과를 /topic/messages 구독자에게 전달
	@MessageMapping("/chat.send")
	@SendTo("/topic/messages")
    public Message sendMessage(ChatMessage chatMessage) {
        Message message = new Message(chatMessage.getSender(), chatMessage.getContent());
        messageRepository.save(message);
        return message;
    }
}
