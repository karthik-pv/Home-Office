# Use a base image with OpenJDK  
FROM openjdk:21  

# Set the working directory  
WORKDIR /app  

# Copy the built JAR file from the target directory  
COPY target/*.jar app.jar  

# Expose the application port  
EXPOSE 8080  

# Command to run the application  
ENTRYPOINT ["java", "-jar", "app.jar"]