FROM maven:3.8.5-open-jdk17 as build
COPY . .
RUN mvn clean package
RUN mvn clean package -DskipTests

FROM openjdk:17.0.1-jdk-slim
COPY --form=build /target/Backend-3.3.1-SNAPSHOT.jar Backend.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","Backend.jar"]