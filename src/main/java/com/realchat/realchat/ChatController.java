package com.realchat.realchat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;

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
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(MessageRepository messageRepository,
                          SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(ChatMessage chatMessage) {
        Message message = new Message(
            chatMessage.getRoomId(),
            chatMessage.getSender(),
            chatMessage.getContent()
        );
        messageRepository.save(message);

        // 해당 방 구독자에게 메시지 전달
        messagingTemplate.convertAndSend(
            "/topic/room/" + chatMessage.getRoomId(), message);

        // 글로벌 알림 (새 메시지 왔다고)
        messagingTemplate.convertAndSend("/topic/notify",
            Map.of("type", "NEW_MESSAGE", "roomId", chatMessage.getRoomId()));
    }

    @GetMapping("/api/chat/rooms/{roomId}/messages")
    @ResponseBody
    public ResponseEntity<?> getMessages(@PathVariable("roomId") Long roomId) {
        List<Message> messages = messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        return ResponseEntity.ok(messages);
    }
}
