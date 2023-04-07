FROM sbtscala/scala-sbt:openjdk-17.0.2_1.8.0_3.2.1 as build
RUN mkdir -p /root/build/project
COPY ./ /root/build
WORKDIR /root/build
RUN sbt d4-chat/stage

FROM openjdk:11-jre-slim-bullseye
WORKDIR .
COPY --from=build /root/build/target/universal/stage/ /app/
CMD app/bin/main
EXPOSE ${APP_PORT}

ENV APP_HOST=${APP_HOST} \
    APP_PORT=${APP_PORT} \
    DB_HOST=${DB_HOST} \
    DB_PORT=${DB_PORT} \
    DB_NAME=${DB_NAME} \
    DB_USER=${DB_USER} \
    DB_SCHEMA=${DB_SCHEMA} \
    DB_PASSWORD=${DB_PASSWORD}
