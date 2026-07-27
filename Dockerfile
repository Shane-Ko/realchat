# ============================================
# realchat — Spring Boot 멀티스테이지 빌드
# Stage 1: Gradle 빌드 (JDK 26)
# Stage 2: 런타임 (JRE 26, non-root)
# ============================================

# ---------- Stage 1: 빌드 ----------
# eclipse-temurin:26-jdk 는 docker.io 에서 검증됨(2026-07). Kyverno 통과.
FROM eclipse-temurin:26-jdk AS builder

WORKDIR /workspace

# 의존성 레이어 캐시: 빌드 스크립트만 먼저 복사
COPY gradle ./gradle
COPY gradlew build.gradle settings.gradle ./

# ★ gradlew 는 실행 비트 없이 커밋됨(-rw-rw-r--, 검증). chmod 필수.
RUN chmod +x ./gradlew

# 의존성 다운로드만 먼저 수행(Docker 레이어 캐시 활용)
RUN ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# 소스 복사 후 bootJar 실행
COPY src ./src
RUN ./gradlew --no-daemon bootJar -x test

# 산출물 위치 확인(실패 시 여기서 멈춤)
RUN ls -la build/libs/

# ---------- Stage 2: 런타임 ----------
# 26-jre 도 docker.io 검증됨.
FROM eclipse-temurin:26-jre

# non-root 실행(보안). 8080은 권한 포트(<1024)가 아니라 USER 전환 후 정상 동작.
RUN groupadd -r app && useradd -r -g app -u 65532 app

WORKDIR /app

# 빌드 스테이지에서 fat JAR 복사 (버전 고정: build.gradle 의 0.0.1-SNAPSHOT)
COPY --from=builder /workspace/build/libs/realchat-0.0.1-SNAPSHOT.jar /app/app.jar

RUN chown -R app:app /app
USER app

EXPOSE 8080

# exec 형식: SIGTERM 전파로 graceful shutdown
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
