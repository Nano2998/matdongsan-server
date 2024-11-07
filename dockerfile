# 빌더 단계
FROM gradle:8.10.2-jdk17 AS builder
WORKDIR /build

# 의존성만 먼저 다운로드
COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue || true

# 애플리케이션 코드 복사 및 빌드
COPY . /build
RUN gradle build -x test --parallel && \
    rm -rf /root/.gradle/caches/ # Gradle 캐시 제거

# 최종 애플리케이션 이미지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=builder /build/build/libs/matdongsan-server-0.0.1-SNAPSHOT.jar .

# 필요한 환경 변수 설정 (예: 메모리 최적화)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# 포트 노출 및 시작 명령어
EXPOSE 8080
CMD ["java", "-jar", "./matdongsan-server-0.0.1-SNAPSHOT.jar"]
