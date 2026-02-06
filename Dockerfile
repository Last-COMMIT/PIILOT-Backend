# ===========================================
# PIILOT Backend Dockerfile (Multi-stage Build)
# ===========================================
# Stage 1: Gradle로 JAR 빌드
# Stage 2: JRE만 있는 가벼운 이미지에 JAR 복사
# ===========================================

# Stage 1: Build
FROM gradle:8.14-jdk21 AS builder

WORKDIR /app

# Gradle 캐시 활용을 위해 의존성 파일 먼저 복사
# build.gradle, settings.gradle이 바뀌지 않으면 의존성 다운로드를 건너뜁니다
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 다운로드 (캐시 활용)
RUN gradle dependencies --no-daemon || true

# 소스 코드 복사
COPY src ./src

# JAR 파일 빌드 (테스트는 건너뜀 - CI에서 별도로 실행)
RUN gradle bootJar --no-daemon -x test

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# curl 설치 (헬스체크에 사용)
RUN apk add --no-cache curl

# Stage 1에서 만든 JAR 파일만 복사 (빌드 도구는 가져오지 않음)
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트 사용
EXPOSE 8080

# 헬스체크: 30초마다 /actuator/health 호출, 60초 동안 시작 대기
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션과 함께 실행
# -XX:+UseContainerSupport: Docker 컨테이너 메모리 제한을 인식
# -XX:MaxRAMPercentage=75.0: 컨테이너 메모리의 75%까지 사용
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
