# 1단계: 빌드 스테이지
FROM gradle:8.5-jdk21 AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 캐시를 활용하기 위해 먼저 의존성 관련 파일만 복사
COPY build.gradle settings.gradle gradle.properties* /app/
COPY gradle /app/gradle

# 의존성 미리 다운로드
RUN gradle build -x test --no-daemon || true

# 전체 프로젝트 복사
COPY . /app

# 실제 빌드
RUN gradle build -x test --no-daemon

# 2단계: 실행 스테이지
FROM eclipse-temurin:21-jre

# 작업 디렉토리
WORKDIR /app

# 빌드된 jar 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 포트 오픈 (Spring Boot 기본 포트)
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
