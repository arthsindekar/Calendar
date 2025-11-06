# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk


# Create app directory
WORKDIR /app

# Copy the JAR built Maven workflow
COPY out/artifacts/Assignment6_jar/Assignment6.jar app.jar

COPY res/ValidCommands.txt commands.txt

# Run the JAR
CMD ["java", "-jar", "app.jar", "--mode", "headless", "commands.txt"]
