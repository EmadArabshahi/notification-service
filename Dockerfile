FROM gradle:7.5.1-jdk11-alpine AS build

WORKDIR /build

COPY ./build.gradle .

COPY ./src ./src

RUN gradle build --no-daemon

FROM openjdk:11-jre-slim
WORKDIR /

COPY --from=build /build/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]