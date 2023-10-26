FROM alpine:latest

ENV OPENJDK_VERSION 21

RUN apk update &&  \
    apk add tor && \
    apk add openjdk${OPENJDK_VERSION}-jdk --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing/ && \
    apk add curl && \
    apk add firefox && \
    apk add libexif && \
    apk add xvfb && \
    apk add dbus && \
    apk add py-pip && \
    apk add ttf-dejavu && \
    apk add openrc --no-cache && \
    apk cache clean && \
    pip install --upgrade pip && \
    pip install selenium

RUN addgroup -S scraper &&  \
    adduser -S scraper -G scraper && \
    openrc && \
    touch /run/openrc/softlevel

COPY --chown=scraper docker/restart-tor.sh /app/scripts/restart-tor.sh
COPY --chown=scraper docker/entrypoint.sh /entrypoint.sh

USER scraper

ARG JAR_FILE
ADD ${JAR_FILE} /app/app.jar

ENTRYPOINT ["/bin/sh", "/entrypoint.sh"]