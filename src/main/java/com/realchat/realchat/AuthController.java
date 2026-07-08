package com.realchat.realchat;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;

/*
 * AuthController
 *
 * 회원가입과 로그인 요청을 처리하는 컨트롤러.
 *
 * - POST /api/auth/signup  → 회원가입 (username, password 받아서 DB 저장)
 * - POST /api/auth/login   → 로그인 (확인 후 JWT 토큰 발급)
 *
 * @RestController: @Controller + 응답을 JSON으로 보냄
 * @RequestMapping: 이 컨트롤러의 모든 API는 /api/auth 로 시작
 */

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final UserRepository userRepository;
	private final JwtUtil jwtUtil;
	private final BCryptPasswordEncoder passwordEncoder;
	private final SimpMessagingTemplate messagingTemplate;

	public AuthController(UserRepository userRepository, JwtUtil jwtUtil,
	                      SimpMessagingTemplate messagingTemplate) {
	    this.userRepository = userRepository;
	    this.jwtUtil = jwtUtil;
	    this.passwordEncoder = new BCryptPasswordEncoder();
	    this.messagingTemplate = messagingTemplate;
	}
    
    
    // 회원가입 API
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String nickname = request.get("nickname");
        String password = request.get("password");

        // 아이디 중복 확인
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 사용중인 아이디입니다"));
        }

        // 닉네임 중복 확인
        if (userRepository.findByNickname(nickname).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "이미 사용중인 닉네임입니다"));
        }

        // 비밀번호 암호화 후 저장
        User user = new User(username, nickname, passwordEncoder.encode(password));
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "가입 완료"));
    }
    
    // 로그인 API
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
    	String username = request.get("username");
    	String password = request.get("password");
    	
    	// username 으로 회원 찾기
    	Optional<User> userOptional = userRepository.findByUsername(username);
    	
    	// 회원이 없으면
    	if (userOptional.isEmpty()) {
    		return ResponseEntity.badRequest().body(Map.of("error","아이디가 존재하지 않습니다."));
    	}
    	
    	User user = userOptional.get();
    	
    	// 비밀번호 확인
    	if (!passwordEncoder.matches(password, user.getPassword())) {
    		// 틀릴경우
    		return ResponseEntity.badRequest().body(Map.of("error","비밀번호가 틀렸습니다."));
    	}
    	
    	// 로그인 성공 → 접속 상태 변경
    	user.setOnline(true);
    	user.setLastActiveAt(LocalDateTime.now());
    	userRepository.save(user);
    	
    	// 접속 상태 변경 알림
    	messagingTemplate.convertAndSend("/topic/notify",
    	    Map.of("type", "USER_STATUS", "nickname", user.getNickname(), "online", true));
    	
    	// 로그인 성공 - JWT 토큰 발급
    	String token = jwtUtil.createToken(username);
    	
    	return ResponseEntity.ok(Map.of(
    		    "message", "로그인 성공",
    		    "token", token,
    		    "username", username,
    		    "nickname", user.getNickname()
    		));
    }
    
    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = jwtUtil.getUsername(token);
        Optional<User> userOptional = userRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setOnline(false);
            userRepository.save(user);

            // 접속 상태 변경 알림
            messagingTemplate.convertAndSend("/topic/notify",
                Map.of("type", "USER_STATUS", "nickname", user.getNickname(), "online", false));
        }

        return ResponseEntity.ok(Map.of("message", "로그아웃 완료"));
    }
}
