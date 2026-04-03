# Stage 1: Build the Application
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the actual source code and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Serve the Application
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app

# We must ensure the uploads directory physically exists on the container so Docker volume mapping functions properly
RUN mkdir -p /app/uploads

# Copy the built jar from Stage 1
COPY --from=build /app/target/scan2dine-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Note: Ensure you map the volumes securely when running: 
# docker run -v /host/path/uploads:/app/uploads -p 8080:8080 my-spring-backend
ENTRYPOINT ["java", "-jar", "app.jar"]
