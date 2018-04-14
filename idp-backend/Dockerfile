FROM openjdk:8-jdk-alpine
MAINTAINER Moulay Ahmed EL IDRISSI ATTACH<ahmed.elidrissi.attach@gmail.com>
VOLUME /tmp
VOLUME /opt

ARG JAR_FILE
ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java", "-Xmx6G", "-XX:+UseG1GC", "-XX:+UnlockExperimentalVMOptions", "-XX:MaxGCPauseMillis=100", "-XX:+DisableExplicitGC", "-XX:TargetSurvivorRatio=90", "-XX:G1NewSizePercent=50", "-XX:G1MaxNewSizePercent=80", "-XX:InitiatingHeapOccupancyPercent=10", "-XX:G1MixedGCLiveThresholdPercent=50", "-XX:+AggressiveOpts",  "-XX:+AlwaysPreTouch", "-XX:+UseLargePagesInMetaspace", "-Djava.security.egd=file:/dev/./urandom", "-Dspring.profiles.active=docker", "-jar", "/app.jar"]
EXPOSE 16666