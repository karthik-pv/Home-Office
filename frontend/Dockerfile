# Use the official Node.js image  
FROM node:22.2.0 AS build  

# Set the working directory  
WORKDIR /app  

# Copy package.json and package-lock.json (if available)  
COPY package*.json ./  

RUN npm install  

# Copy the rest of your application code  
COPY . .  

# Build your application  
RUN npm run build  

# Use a lightweight server to serve the static files  
FROM node:22.2.0  

# Install serve globally  
RUN npm install -g serve  

# Set the working directory  
WORKDIR /app  

# Copy the build files from the previous stage  
COPY --from=build /app/build .  

# Expose the port the app runs on  
EXPOSE 3000  

# Command to run the application  
CMD ["serve", "-s", ".", "-l", "3000"]