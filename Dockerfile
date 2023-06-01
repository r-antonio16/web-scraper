FROM alpine:latest

RUN apk update &&  \
    apk add tor && \
    apk add openjdk20-jdk --repository=http://dl-cdn.alpinelinux.org/alpine/edge/testing/ && \
    apk add openrc --no-cache && \
    apk add curl && \
    apk cache clean

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