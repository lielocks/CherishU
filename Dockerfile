FROM openjdk:17-alpine
ARG JAR_FILE=*.jar
ARG PROFILES
ARG ENV
COPY ${JAR_FILE} app.jar
ENTRYPOINT {"java", "-Dspring.profiles.active=${PROFILES}", "-Dserver.env=${ENV}", "-jar", "/app.jar"}