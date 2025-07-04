# Stage 1: Build the jar using Maven
FROM maven:3.9.3-eclipse-temurin-17 AS build

WORKDIR /app

# Copy only pom.xml and download dependencies first (for caching)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the source code
COPY src ./src

# Build the project and package the jar (skip tests for faster build)
RUN mvn clean package -DskipTests

# Stage 2: Create a lightweight runtime image
FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

# Copy the jar from the build stage
COPY --from=build /app/target/NewLife-0.0.1-SNAPSHOT.jar app.jar

# Expose the default Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
