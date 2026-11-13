# Stage 1: Build the application using Maven
FROM maven:3.9.6-eclipse-temurin-11-alpine AS build
WORKDIR /app

# Cache Maven dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Build project without tests
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Production lightweight runtime
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app

# Copy built JAR from builder
COPY --from=build /app/target/auth-app-0.0.1-SNAPSHOT.jar app.jar

# Directory for file-backed database storage if using H2
RUN mkdir -p /app/data

EXPOSE 8080
ENV PORT=8080

# Keep memory within free tier limits (512MB)
ENTRYPOINT ["java", "-Xmx384m", "-jar", "app.jar"]
