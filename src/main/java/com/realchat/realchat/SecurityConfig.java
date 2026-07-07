package com.realchat.realchat;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/*
 * SecurityConfig
 *
 * Spring Security 설정 클래스.
 * "어떤 URL은 누구나 접근 가능, 어떤 URL은 로그인 필요"를 정한다.
 *
 * @Configuration: 설정 파일임을 Spring에 알림
 * @EnableWebSecurity: Spring Security 기능을 켬
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 비활성화 (REST API에서는 보통 끔)
            .csrf(csrf -> csrf.disable())

            // 세션 사용 안 함 (JWT를 쓰니까)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // URL별 접근 권한 설정
            .authorizeHttpRequests(auth -> auth
                // 이 경로들은 누구나 접근 가능 (로그인 필요 없음)
                .requestMatchers("/api/auth/**").permitAll()   // 회원가입, 로그인
                .requestMatchers("/ws-chat/**").permitAll()    // WebSocket 연결
                .requestMatchers("/**").permitAll()            // HTML, CSS, JS 파일
            );

        return http.build();
    }
}