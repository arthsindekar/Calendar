# Use official OpenJDK 17 image
FROM openjdk:17-jdk-slim

# Create app directory
WORKDIR /app

# Copy the JAR built Maven workflow
COPY out/artifacts/Assignment6_jar/Assignment6.jar app.jar

# Run the JAR
CMD ["java", "-jar", "app.jar"]
