FROM openjdk:8-jdk-alpine
MAINTAINER Moulay Ahmed EL IDRISSI ATTACH<ahmed.elidrissi.attach@gmail.com>

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Xmx512m", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=runtime,prod", "-Dstartup.passphrase=cM7g+:S*DY7m>c.D3{8jHtr6tH%^L~3t", "-jar", "/app.jar"]
EXPOSE 16666