FROM openjdk:11-jre-slim
MAINTAINER Moulay Ahmed EL IDRISSI ATTACH<ahmed.elidrissi.attach@gmail.com>

ENV STARTUP_PASSPHRASE ""
ENV JAVA_OPTS ""

ARG JAR_FILE

RUN useradd -ms /bin/bash idp

WORKDIR /home/idp

ADD docker/entrypoint.sh entrypoint.sh

RUN chmod 755 entrypoint.sh && chown idp:idp entrypoint.sh

USER idp

ADD ${JAR_FILE} app.jar

EXPOSE 16666
ENTRYPOINT ["./entrypoint.sh"]