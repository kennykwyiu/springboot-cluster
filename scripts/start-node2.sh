#!/bin/bash

# Start Node2 of Spring Boot Cluster
# This script starts the second node of the Spring Boot cluster

echo "Starting Spring Boot Cluster - Node2..."

# Set environment variables
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}
export SPRING_PROFILES_ACTIVE=node2

# Create logs directory if it doesn't exist
mkdir -p logs

# Set JVM options for Node2
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dspring.profiles.active=node2"

# Application properties file for Node2
CONFIG_FILE="node2/application-node2.properties"

# Check if the JAR file exists
JAR_FILE="target/springboot-cluster-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Please run 'mvn clean package' first to build the application"
    exit 1
fi

# Check if config file exists
if [ ! -f "$CONFIG_FILE" ]; then
    echo "Error: Configuration file not found at $CONFIG_FILE"
    exit 1
fi

echo "Starting Node2 on port 8082 with Hazelcast port 5702..."
echo "Configuration file: $CONFIG_FILE"
echo "JAR file: $JAR_FILE"
echo "JVM Options: $JAVA_OPTS"

# Start the application
java $JAVA_OPTS -jar $JAR_FILE --spring.config.location=classpath:/application.properties,$CONFIG_FILE &

# Store the PID
NODE2_PID=$!
echo $NODE2_PID > node2.pid

echo "Node2 started with PID: $NODE2_PID"
echo "Application will be available at: http://localhost:8082"
echo "Actuator endpoints available at: http://localhost:9082"
echo "Health check: http://localhost:8082/api/health"
echo ""
echo "To stop Node2, run: kill $NODE2_PID or use stop-node2.sh"
echo "To view logs, run: tail -f logs/node2.log"

