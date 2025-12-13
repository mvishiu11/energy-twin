# Build stage
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copy project files
COPY energy-twin/pom.xml ./
COPY energy-twin/lib ./lib

# Download dependencies (cached layer)
RUN mvn dependency:go-offline -B || true

# Copy source code
COPY energy-twin/src ./src

# Build the application (skip tests and spotless for dev)
RUN mvn clean package -DskipTests -Dspotless.check.skip=true

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built JAR (now includes JADE)
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8081

# Run the application
CMD ["java", "-jar", "app.jar"]
