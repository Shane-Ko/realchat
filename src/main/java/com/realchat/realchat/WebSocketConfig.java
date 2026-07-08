package com.realchat.realchat;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/*
 * WebSocket 설정 클래스
 * 
 * 브라우저와 서버 간 실시간 통신을 위한 WebSocket(STOMP) 설정.
 * 
 * - @Configuration: 이 클래스가 설정 파일임을 Spring에 알림
 * - @EnableWebSocketMessageBroker: WebSocket 메시지 기능을 켬
 * 
 * 동작 흐름:
 *   1. 브라우저가 /ws-chat 으로 WebSocket 연결
 *   2. /app/chat.send 로 메시지 전송
 *   3. 서버가 받아서 처리 후 /topic/messages 로 브로드캐스트
 *   4. 해당 경로를 구독 중인 모든 브라우저에 메시지 전달
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer{
	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		// 브라우저가 WebSocket 연결할 주소
		// withSockJS(): WebSocket을 지원하지 않는 브라우저에서도 동작하게 하기 위함 (일종의 보험)
		
		// 1. WebSocket 연결 (문 열기)
		registry.addEndpoint("/ws-chat").withSockJS();
	}
	
	// 2. 연결된 후의 규칙 (STOMP 설정)
	@Override
	public void configureMessageBroker(MessageBrokerRegistry registry) {
		// /app 으로 시작하는 메시지는 서버(Controller)가 처리
	    registry.setApplicationDestinationPrefixes("/app");
	    
	    // /topic 으로 시작하는 경로는 구독자에게 메시지를 뿌려주는 역할
	    registry.enableSimpleBroker("/topic", "/queue");
	}

}
