FROM maven:3.8.1-openjdk-17-slim as build
COPY . .
RUN mvn clean package
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/Backend-3.3.1-SNAPSHOT.jar Backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Backend.jar"]