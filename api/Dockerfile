FROM eclipse-temurin:21-jre-alpine

# Set the working directory inside the container
WORKDIR /app

EXPOSE 8080

# Copy the built JAR file into the container
COPY build/libs/api-0.0.1-SNAPSHOT.jar app.jar

# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]

