#!/bin/bash

# Script to start Node2 of the Spring Boot Cluster.
# This script configures the environment and launches the Spring Boot application for Node2.

echo "Starting Spring Boot Cluster - Node2..."

# Set JAVA_HOME environment variable.
# This ensures the correct Java Development Kit (JDK) is used.
# It defaults to a common OpenJDK path if not already set.
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}

# Set Spring profile for Node2.
# This activates the configuration specific to Node2 (e.g., application-node2.properties).
export SPRING_PROFILES_ACTIVE=node2

# Create a logs directory if it doesn't already exist.
# This is where application logs will be stored.
mkdir -p logs

# Set JVM (Java Virtual Machine) options for Node2.
# -Xms512m: Sets the initial heap size to 512 MB.
# -Xmx1024m: Sets the maximum heap size to 1024 MB.
# -XX:+UseG1GC: Enables the Garbage-First (G1) garbage collector for better performance.
# -XX:MaxGCPauseMillis=200: Tries to keep garbage collection pauses below 200 milliseconds.
# -Dspring.profiles.active=node2: Activates the Spring profile for Node2 (redundant with export but good for clarity).
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dspring.profiles.active=node2"

# Define the path to the application properties file specific to Node2.
CONFIG_FILE="node2/application-node2.properties"

# Define the path to the executable JAR file.
JAR_FILE="target/springboot-cluster-1.0.0.jar"

# Check if the JAR file exists before attempting to run the application.
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please run \'mvn clean package\' first to build the application."
    exit 1
fi

# Check if the Node2 specific configuration file exists.
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Configuration file not found at $CONFIG_FILE"
    exit 1
fi

echo "Starting Node2 on port 8082 with Hazelcast port 5702..."
echo "Configuration file: $CONFIG_FILE"
echo "JAR file: $JAR_FILE"
echo "JVM Options: $JAVA_OPTS"

# Start the Spring Boot application in the background.
# java $JAVA_OPTS: Applies the defined JVM options.
# -jar $JAR_FILE: Specifies the executable JAR to run.
# --spring.config.location: Tells Spring Boot where to find configuration files.
#   classpath:/application.properties: Loads the base configuration.
#   $CONFIG_FILE: Loads the Node2 specific configuration, overriding base settings.
java $JAVA_OPTS -jar $JAR_FILE --spring.config.location=classpath:/application.properties,$CONFIG_FILE &

# Store the Process ID (PID) of the background process.
NODE2_PID=$! # $! holds the PID of the last background command.
echo $NODE2_PID > node2.pid # Saves the PID to a file for easy stopping later.

echo "Node2 started with PID: $NODE2_PID"
echo "Application will be available at: http://localhost:8082"
echo "Actuator endpoints available at: http://localhost:9082"
echo "Health check: http://localhost:8082/api/health"
echo ""
echo "To stop Node2, run: kill $NODE2_PID or use stop-node2.sh"
echo "To view logs, run: tail -f logs/node2.log"

