# Multi-stage build để tối ưu kích thước image
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml và mvnw để cache dependencies
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Download dependencies (sẽ được cache nếu pom.xml không thay đổi)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application (skip tests để build nhanh hơn)
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Install wget for health check
RUN apk add --no-cache wget

# Tạo user non-root để chạy ứng dụng (bảo mật hơn)
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR file từ build stage
COPY --from=build /app/target/dashboard-0.0.1-SNAPSHOT.jar app.jar

# Tạo entrypoint script để xử lý Firebase credentials từ env
RUN echo '#!/bin/sh' > /entrypoint.sh && \
    echo 'set -e' >> /entrypoint.sh && \
    echo 'if [ -n "$FIREBASE_SERVICE_ACCOUNT_KEY" ]; then' >> /entrypoint.sh && \
    echo '  echo "$FIREBASE_SERVICE_ACCOUNT_KEY" > /app/serviceAccountKey.json' >> /entrypoint.sh && \
    echo '  export GOOGLE_APPLICATION_CREDENTIALS=/app/serviceAccountKey.json' >> /entrypoint.sh && \
    echo '  echo "✅ Firebase credentials loaded from environment variable"' >> /entrypoint.sh && \
    echo 'fi' >> /entrypoint.sh && \
    echo 'exec java -jar app.jar "$@"' >> /entrypoint.sh && \
    chmod +x /entrypoint.sh

# Change ownership to spring user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring:spring

# Expose port (Render sẽ tự động map PORT env variable)
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:${PORT:-8080}/ || exit 1

# Run application với entrypoint script
ENTRYPOINT ["/entrypoint.sh"]

