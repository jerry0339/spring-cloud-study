FROM amazoncorretto:17.0.7-alpine
LABEL maintainer="jerry0339@naver.com"
LABEL version="1.0.0"
LABEL description="spring clude eureka discovery image for Dockerfile test"
LABEL createdDate="24-10-23"
CMD ["./mvnw", "clean", "package"]
ARG JAR_FILE_PATH=target/*.jar
COPY ${JAR_FILE_PATH} app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]