# Use the official OpenJDK 20 image as the base
FROM openjdk:17-jdk-slim

# # Set the working directory inside the container
WORKDIR /app


# Copy the Spring Boot JAR file into the container
COPY ./target/data-pipeline-0.0.1-SNAPSHOT.jar /app/data-pipe.jar
COPY ./data-models /app/data-models

RUN chmod 777 -R *


# Set the entry point command to run the Spring Boot application
CMD ["sh", "-c", "java -jar data-pipe.jar stream"]