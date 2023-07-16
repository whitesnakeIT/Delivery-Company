FROM openjdk:17-jdk-slim

LABEL authors="tomasz"

RUN mkdir /app

WORKDIR /app

COPY target/delivery.jar /app


EXPOSE 8081

CMD ["java", "-jar", "delivery.jar"]
