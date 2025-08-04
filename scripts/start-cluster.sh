#!/bin/bash

# Start the complete Spring Boot Cluster
# This script starts both nodes and provides instructions for Apache setup

echo "=========================================="
echo "Starting Spring Boot Cluster"
echo "=========================================="

# Check if JAR file exists
JAR_FILE="target/springboot-cluster-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo "Error: JAR file not found at $JAR_FILE"
    echo "Building the application first..."
    mvn clean package -DskipTests
    
    if [ $? -ne 0 ]; then
        echo "Build failed. Please check the Maven build output."
        exit 1
    fi
fi

# Create logs directory
mkdir -p logs

echo "Starting Node1..."
./scripts/start-node1.sh

echo ""
echo "Waiting 10 seconds for Node1 to initialize..."
sleep 10

echo "Starting Node2..."
./scripts/start-node2.sh

echo ""
echo "Waiting 10 seconds for Node2 to initialize..."
sleep 10

echo ""
echo "=========================================="
echo "Cluster Status Check"
echo "=========================================="

# Check if both nodes are running
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
echo "1. Configure and start Apache HTTP Server"
echo "2. Copy apache-config/000-default.conf to /etc/apache2/sites-available/"
echo "3. Enable required Apache modules:"
echo "   sudo a2enmod proxy proxy_http proxy_balancer lbmethod_byrequests headers rewrite"
echo "4. Restart Apache: sudo systemctl restart apache2"
echo "5. Access the load-balanced application at: http://localhost"
echo "6. Monitor the cluster at: http://localhost/balancer-manager"
echo ""
echo "To stop the cluster, run: ./scripts/stop-cluster.sh"

