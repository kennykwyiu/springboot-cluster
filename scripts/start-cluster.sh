#!/bin/bash

# Script to start the complete Spring Boot Cluster.
# This script orchestrates the startup of both Node1 and Node2
# and provides instructions for setting up the Apache HTTP Server.

echo "=========================================="
echo "Starting Spring Boot Cluster"
echo "=========================================="

# Define the path to the executable JAR file.
JAR_FILE="target/springboot-cluster-1.0.0.jar"

# Check if the JAR file exists.
# If not, it attempts to build the application using Maven.
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Building the application first..."
    # Run Maven clean package to build the JAR. -DskipTests skips running tests.
    mvn clean package -DskipTests
    
    # Check the exit status of the Maven command.
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the Maven build output."
        exit 1 # Exit if the build fails.
    fi
fi

# Create a logs directory if it doesn't already exist.
# This directory will store the log files for each node.
mkdir -p logs

echo "Starting Node1..."
# Execute the start-node1.sh script to launch the first node.
./scripts/start-node1.sh

echo ""
echo "Waiting 10 seconds for Node1 to initialize..."
sleep 10 # Pause to allow Node1 to fully start up and join the Hazelcast cluster.

echo "Starting Node2..."
# Execute the start-node2.sh script to launch the second node.
./scripts/start-node2.sh

echo ""
echo "Waiting 10 seconds for Node2 to initialize..."
sleep 10 # Pause to allow Node2 to fully start up and join the Hazelcast cluster.

echo ""
echo "=========================================="
echo "Cluster Status Check"
echo "=========================================="

# Check the health of both nodes using curl.
# The `jq . 2>/dev/null || curl -s ...` part attempts to pretty-print JSON if `jq` is available,
# otherwise, it just outputs the raw response.
echo "Checking Node1 health..."
curl -s http://localhost:8081/api/health | jq . 2>/dev/null || curl -s http://localhost:8081/api/health

echo ""
echo "Checking Node2 health..."
curl -s http://localhost:8082/api/health | jq . 2>/dev/null || curl -s http://localhost:8082/api/health

echo ""
echo "=========================================="
echo "Cluster Started Successfully!"
echo "=========================================="
echo ""
echo "Node1: http://localhost:8081"
echo "Node2: http://localhost:8082"
echo ""
echo "Actuator Endpoints:"
echo "Node1 Management: http://localhost:9081"
echo "Node2 Management: http://localhost:9082"
echo ""
echo "API Endpoints:"
echo "Health Check: /api/health"
echo "Info: /api/info"
echo "Session Test: /api/session"
echo "Test Endpoint: /api/test"
echo ""
echo "Next Steps:"
echo "1. Configure and start Apache HTTP Server" # Instructions for Apache setup.
echo "2. Copy apache-config/000-default.conf to /etc/apache2/sites-available/" # Copy the provided Apache config.
echo "3. Enable required Apache modules:" # List of Apache modules to enable.
echo "   sudo a2enmod proxy proxy_http proxy_balancer lbmethod_byrequests headers rewrite"
echo "4. Restart Apache: sudo systemctl restart apache2" # Command to restart Apache.
echo "5. Access the load-balanced application at: http://localhost" # The final access URL.
echo "6. Monitor the cluster at: http://localhost/balancer-manager" # URL for Apache's balancer manager.
echo ""
echo "To stop the cluster, run: ./scripts/stop-cluster.sh" # Instruction to stop the cluster.

