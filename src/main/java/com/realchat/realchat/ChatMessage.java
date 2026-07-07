package com.realchat.realchat;

public class ChatMessage {
	
	private String sender;
	private String content;
	
	public ChatMessage() {
		
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setSender(String sender) {
		this.sender = sender;
	}
	
	public void setContent(String content) {
		this.content = content;
	}

}
