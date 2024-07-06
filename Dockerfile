# Use maven image with temurin JDK 17 for building
FROM maven:3-eclipse-temurin-17 AS build

# Copy project files to the build stage
COPY . .

# Build the SpringBoot application (replace with your build command if different)
RUN mvn clean package -DskipTests

# Use a slim Alpine image with temurin JDK 17 for runtime
FROM eclipse-temurin:17-alpine

# Copy the JAR file from the build stage
COPY --from=build /target/*.jar Backend.jar

# Expose the port where your application listens (typically 8080)
EXPOSE 8080

# Define the startup command to run the application jar
ENTRYPOINT ["java", "-Dspring.profiles.active=render", "-jar", "Backend.jar"]
