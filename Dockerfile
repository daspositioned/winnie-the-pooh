FROM gradle:jdk25-alpine AS build

COPY . .
RUN gradle clean installDist --no-daemon

FROM eclipse-temurin:25-alpine AS runtime

WORKDIR /app

COPY --from=build /home/gradle/build/install/honeypot/ .

ENTRYPOINT ["bin/honeypot"]
