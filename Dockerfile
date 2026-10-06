FROM node:22-slim AS frontend
WORKDIR /web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
COPY --from=frontend /web/dist ./src/main/resources/static
RUN mvn -B package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends unzip && rm -rf /var/lib/apt/lists/*
ADD https://gitlab.orekit.org/orekit/orekit-data/-/archive/main/orekit-data-main.zip /tmp/od.zip
RUN unzip -q /tmp/od.zip -d /app && mv /app/orekit-data-main /app/orekit-data
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-Djava.net.preferIPv4Stack=true", "-jar", "/app/app.jar"]