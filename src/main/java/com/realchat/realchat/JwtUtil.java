package com.realchat.realchat;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/*
 * JwtUtil
 *
 * JWT 토큰을 만들고 검증하는 도구 클래스.
 *
 * - createToken(): 로그인 성공 시 토큰 발급 (신분증 만들기)
 * - getUsername(): 토큰에서 username 꺼내기 (신분증에서 이름 읽기)
 * - isValid(): 토큰이 유효한지 확인 (신분증이 진짜인지 검사)
 *
 * @Component: Spring이 이 클래스를 자동으로 관리하게 등록
 *             (다른 클래스에서 가져다 쓸 수 있게)
 */

@Component
public class JwtUtil {
	
	// 토큰 서명에 사용하는 비밀키 (이거를 알아야 토큰을 만들 수 있음)
    // 실제 서비스에서는 이걸 코드에 적지 않고 Vault 같은 곳에 보관
	private final String SECRET = "RealChatSecretKeyRealChatSecretKey1234";
	
	// 토큰 유효 시간: 24시간
	private final long EXPIRATION = 1000 * 60 * 60 * 24;
	
	// 비밀키를 SecretKey 객체로 반환
	private SecretKey getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
	}
	
	// 토큰 만들기 (로그인 성공 시 호출)
    public String createToken(String username) {
        return Jwts.builder()
                .subject(username)                              // 토큰에 username 저장
                .issuedAt(new Date())                           // 발급 시간: 지금
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))  // 만료 시간: 24시간 후
                .signWith(getSigningKey())                      // 비밀키로 서명
                .compact();                                     // 문자열로 변환
    }
    
    public String getUsername(String token) {
    	return Jwts.parser()
    			.verifyWith(getSigningKey())	// 비밀키로 검증
    			.build()
    			.parseSignedClaims(token)		// 토큰 해석
    			.getPayload()
    			.getSubject();					// username 꺼냄
    }
	
 // 토큰이 유효한지 확인
    public boolean isValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;    // 검증 성공 → 유효
        } catch (Exception e) {
            return false;   // 검증 실패 → 무효 (만료, 위조 등)
        }
    }
}
