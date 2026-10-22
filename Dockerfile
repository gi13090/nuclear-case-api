FROM maven:3.9.11-eclipse-temurin-21@sha256:6fdc855a6ed81d288ca7ca37ac6ff5e9308b612485c0801d70b25a858c83d237 AS build
WORKDIR /build
ENV MAVEN_OPTS="-Xmx256m"
COPY pom.xml .
COPY src ./src
RUN java -version && javac -version && mvn -version && mvn -B -ntp clean verify -DargLine=-Xmx384m

FROM eclipse-temurin:21-jre-jammy@sha256:affcab5356e1270ac35d2361ee83135af2f03e8d580eca78a6d5d6fdbf048db0 AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /build/target/nuclear-case-api-0.0.1-SNAPSHOT.jar ./app.jar
ENV PORT=8080 \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0"
USER 10001:10001
EXPOSE 8080
STOPSIGNAL SIGTERM
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
