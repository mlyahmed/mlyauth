FROM openjdk:8-jdk-alpine
MAINTAINER Moulay Ahmed EL IDRISSI ATTACH<ahmed.elidrissi.attach@gmail.com>
VOLUME /tmp
VOLUME /opt

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-XX:+PrintFlagsFinal", "-Xmx1024m", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker-mysql", "-jar", "/app.jar"]
EXPOSE 16666