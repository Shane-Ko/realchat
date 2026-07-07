package com.realchat.realchat;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/*
 * JwtFilter
 *
 * 모든 HTTP 요청이 Controller에 도착하기 전에 거치는 필터.
 * 요청에 JWT 토큰이 있으면 검증하고, 유효하면 "이 사람은 인증된 사용자"로 등록.
 *
 * 비유: 건물 입구 경비원
 *   - 신분증(토큰) 있으면 → 확인 후 통과
 *   - 신분증 없으면 → 그냥 통과 (SecurityConfig에서 permitAll인 경로도 있으니까)
 *   - 가짜 신분증이면 → 무시 (인증 안 된 상태로 진행 → Security가 막음)
 *
 * OncePerRequestFilter: 요청 1번당 이 필터를 1번만 실행
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 꺼내기
        //    브라우저가 "Authorization: Bearer eyJhbG..." 형태로 보냄
        String header = request.getHeader("Authorization");

        // 2. 토큰이 있고 "Bearer "로 시작하면
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);  // "Bearer " 제거 → 순수 토큰만

            // 3. 토큰이 유효한지 확인
            if (jwtUtil.isValid(token)) {
                // 4. 토큰에서 username 꺼내기
                String username = jwtUtil.getUsername(token);

                // 5. Spring Security에 "이 사람은 인증된 사용자야" 등록
                UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(username, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // 6. 다음 필터 또는 Controller로 넘기기
        filterChain.doFilter(request, response);
    }
}