# 빌더 단계
FROM gradle:8.10.2-jdk17 as builder
WORKDIR /build

# Gradle 설정 파일 복사 및 의존성 다운로드
COPY build.gradle settings.gradle /build/
RUN gradle build -x test --parallel --continue > /dev/null 2>&1 || true

# 애플리케이션 코드 복사 및 빌드
COPY . /build
RUN gradle build -x test --parallel && \
    rm -rf /root/.gradle/caches/ # Gradle 캐시 제거

# 최종 애플리케이션 이미지
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 JAR 파일만 복사
COPY --from=builder /build/build/libs/matdongsan-server-0.0.1-SNAPSHOT.jar .

EXPOSE 8080

CMD ["java", "-jar", "./matdongsan-server-0.0.1-SNAPSHOT.jar"]
