FROM maven:3.8.4-openjdk-17 as build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests
RUN echo "=== Listing target directory ===" && \
    ls -la target/

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/shopping-service-1.0-SNAPSHOT.jar app.jar
RUN echo "=== Listing app directory ===" && \
    ls -la && \
    echo "=== Jar file details ===" && \
    ls -la app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/app.jar"] 