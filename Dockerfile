# ======================================================
# BrekFood — Multi-stage Dockerfile
# Stage 1: Build with Maven
# Stage 2: Layered Spring Boot runtime image
# ======================================================

# ---- Stage 1: Build ----------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /workspace

# Copy Maven wrapper and POM first (layer-cache friendly)
COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached unless pom.xml changes)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B -q

# Copy source and build (skip tests — run separately in CI)
COPY src src
RUN ./mvnw package -DskipTests -B -q

# Extract Spring Boot layered JAR
RUN java -Djarmode=layertools -jar target/*.jar extract --destination target/extracted

# ---- Stage 2: Runtime --------------------------------------
FROM eclipse-temurin:17-jre-alpine AS runtime

# Security: run as non-root
RUN addgroup -S brekfood && adduser -S brekfood -G brekfood
WORKDIR /app

# Copy layers in cache-optimal order (least → most frequently changed)
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

# Fix ownership
RUN chown -R brekfood:brekfood /app
USER brekfood

EXPOSE 8080

# JVM tuning for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]

