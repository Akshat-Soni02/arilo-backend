# Stage 1: Build the application
FROM gradle:jdk21-jammy AS builder
WORKDIR /app

# Copy gradle settings and build files separately to leverage layer caching
COPY settings.gradle build.gradle ./

# Download dependencies to cache them
# We use --no-daemon and ignore errors just in case some configuration relies on src being present
RUN gradle dependencies --no-daemon || true

# Copy source code
COPY src ./src

# Build the application
# We skip tests to speed up the build in this example, but removing -x test is recommended for CI/CD
RUN gradle bootJar --no-daemon -x test

# Stage 2: Run the application
FROM eclipse-temurin:21-jre-alpine AS runner

# Create a non-root user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy the JAR file from the builder stage
# The path depends on the project name/version in build.gradle usually defaults to build/libs/
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership of the application file
RUN chown appuser:appgroup app.jar

# Switch to the non-root user
USER appuser

# Expose the application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
