FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --chown=10001:10001 target/auth-0.0.1-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 9900
ENTRYPOINT ["java", "-jar", "app.jar"]
