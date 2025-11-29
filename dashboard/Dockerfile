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
    echo 'echo "🔍 Checking environment variables..."' >> /entrypoint.sh && \
    echo '# Support both FIREBASE_SERVICE_ACCOUNT_KEY and FIREBASE_SERVICE_ACCOUNT' >> /entrypoint.sh && \
    echo 'FIREBASE_KEY="${FIREBASE_SERVICE_ACCOUNT_KEY:-${FIREBASE_SERVICE_ACCOUNT}}"' >> /entrypoint.sh && \
    echo 'echo "FIREBASE_SERVICE_ACCOUNT_KEY is set: $([ -n "$FIREBASE_SERVICE_ACCOUNT_KEY" ] && echo "YES" || echo "NO")"' >> /entrypoint.sh && \
    echo 'echo "FIREBASE_SERVICE_ACCOUNT is set: $([ -n "$FIREBASE_SERVICE_ACCOUNT" ] && echo "YES" || echo "NO")"' >> /entrypoint.sh && \
    echo 'if [ -n "$FIREBASE_KEY" ]; then' >> /entrypoint.sh && \
    echo '  echo "📝 Writing Firebase credentials to file..."' >> /entrypoint.sh && \
    echo '  echo "$FIREBASE_KEY" > /app/serviceAccountKey.json' >> /entrypoint.sh && \
    echo '  chmod 600 /app/serviceAccountKey.json' >> /entrypoint.sh && \
    echo '  export GOOGLE_APPLICATION_CREDENTIALS=/app/serviceAccountKey.json' >> /entrypoint.sh && \
    echo '  echo "✅ Firebase credentials file created at: $GOOGLE_APPLICATION_CREDENTIALS"' >> /entrypoint.sh && \
    echo '  echo "📄 File exists: $([ -f "$GOOGLE_APPLICATION_CREDENTIALS" ] && echo "YES" || echo "NO")"' >> /entrypoint.sh && \
    echo '  echo "📏 File size: $(wc -c < "$GOOGLE_APPLICATION_CREDENTIALS") bytes"' >> /entrypoint.sh && \
    echo 'else' >> /entrypoint.sh && \
    echo '  echo "⚠️  FIREBASE_SERVICE_ACCOUNT_KEY/FIREBASE_SERVICE_ACCOUNT not set, skipping Firebase credentials setup"' >> /entrypoint.sh && \
    echo 'fi' >> /entrypoint.sh && \
    echo 'echo "🚀 Starting application..."' >> /entrypoint.sh && \
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

