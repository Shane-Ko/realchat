##### 1단계 : 빌드 #####
FROM gradle:jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

##### 2단계 : 실행 #####
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [ "java","-jar","app.jar" ]