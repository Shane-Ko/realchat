package com.realchat.realchat;

/*
 * ChatMessage
 *
 * 브라우저에서 보내는 채팅 메시지를 담는 클래스.
 * roomId 추가로 어떤 방에 보내는 메시지인지 구분.
 */
public class ChatMessage {

    private Long roomId;
    private String sender;
    private String content;

    public ChatMessage() {
    }

    public Long getRoomId() { return roomId; }
    public String getSender() { return sender; }
    public String getContent() { return content; }

    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public void setSender(String sender) { this.sender = sender; }
    public void setContent(String content) { this.content = content; }
}