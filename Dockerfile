FROM adoptopenjdk/openjdk11:jdk-11.0.10_9-alpine
VOLUME /tmp
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
