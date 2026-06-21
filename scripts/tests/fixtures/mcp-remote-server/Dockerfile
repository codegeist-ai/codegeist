FROM eclipse-temurin:25-jre

WORKDIR /app
COPY target/codegeist-mcp-remote-smoke-server.jar /app/server.jar
EXPOSE 3000

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
