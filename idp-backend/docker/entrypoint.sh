#!/bin/sh

exec java ${JAVA_OPTS} -DSTARTUP_PASSPHRASE=${STARTUP_PASSPHRASE} -Djava.security.egd=file:/dev/./urandom -jar "${HOME}/app.jar" "$@"