# ==========================================
# SkillForge Spring Boot Backend Dockerfile
# Multi-stage build for minimal image size
# ==========================================

# Stage 1: Build JAR using Maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build application package
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-privileged system user for security
RUN useradd -m -u 1001 appuser
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /app/target/skillforge-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
