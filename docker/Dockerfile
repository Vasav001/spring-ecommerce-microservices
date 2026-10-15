FROM maven:3.9.11-eclipse-temurin-21 AS build
ARG SERVICE
WORKDIR /src
COPY outbox-lib outbox-lib
COPY ${SERVICE} ${SERVICE}
RUN --mount=type=cache,target=/root/.m2,sharing=locked mvn -B -q -f outbox-lib/pom.xml install -DskipTests \
    && mvn -B -q -f ${SERVICE}/pom.xml package -DskipTests \
    && cp ${SERVICE}/target/${SERVICE}-*.jar /app.jar

FROM eclipse-temurin:21-jre
RUN useradd --system app
USER app
WORKDIR /app
COPY --from=build /app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
