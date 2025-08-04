#!/bin/bash

# Start Node1 of Spring Boot Cluster
# This script starts the first node of the Spring Boot cluster

echo "Starting Spring Boot Cluster - Node1..."

# Set environment variables
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}
export SPRING_PROFILES_ACTIVE=node1

# Create logs directory if it doesn't exist
mkdir -p logs

# Set JVM options for Node1
export JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -Dspring.profiles.active=node1"

# Application properties file for Node1
CONFIG_FILE="node1/application-node1.properties"

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

echo "Starting Node1 on port 8081 with Hazelcast port 5701..."
echo "Configuration file: $CONFIG_FILE"
echo "JAR file: $JAR_FILE"
echo "JVM Options: $JAVA_OPTS"

# Start the application
java $JAVA_OPTS -jar $JAR_FILE --spring.config.location=classpath:/application.properties,$CONFIG_FILE &

# Store the PID
NODE1_PID=$!
echo $NODE1_PID > node1.pid

echo "Node1 started with PID: $NODE1_PID"
echo "Application will be available at: http://localhost:8081"
echo "Actuator endpoints available at: http://localhost:9081"
echo "Health check: http://localhost:8081/api/health"
echo ""
echo "To stop Node1, run: kill $NODE1_PID or use stop-node1.sh"
echo "To view logs, run: tail -f logs/node1.log"

