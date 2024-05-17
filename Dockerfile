FROM maven:3.9.6-eclipse-temurin-21 AS build

COPY pom.xml /app/pom.xml
COPY src /app/src

WORKDIR /app
RUN mvn clean package


FROM eclipse-temurin:21-jre AS runtime

WORKDIR /app
COPY --from=build /app/target/classification-service*.jar /app/classification-service.jar

# Create the classification-service user
RUN addgroup classification-service && \
    adduser --disabled-password --gecos '' --ingroup classification-service classification-service

# Change permissions
RUN mkdir /app/store \
    && chown -R classification-service:classification-service /app/store

# Run as the classification-service user
USER classification-service
EXPOSE 8089
CMD ["java", "-Xmx3g", "-jar", "classification-service.jar"]

