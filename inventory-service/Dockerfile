
FROM openjdk:17-jdk-slim

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} inventory-service.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","/inventory-service.jar"]
