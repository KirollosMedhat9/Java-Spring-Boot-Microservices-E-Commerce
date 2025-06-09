
FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} service-registry.jar

EXPOSE 8761

ENTRYPOINT ["java","-jar","/app/service-registry.jar"]
