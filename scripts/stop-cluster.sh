#!/bin/bash

# Stop the complete Spring Boot Cluster

echo "=========================================="
echo "Stopping Spring Boot Cluster"
echo "=========================================="

echo "Stopping Node1..."
./scripts/stop-node1.sh

echo ""
echo "Stopping Node2..."
./scripts/stop-node2.sh

echo ""
echo "=========================================="
echo "Cluster Stopped Successfully!"
echo "=========================================="

# Clean up any remaining processes
echo "Cleaning up any remaining processes..."

# Kill any remaining Java processes on the cluster ports
for port in 8081 8082 9081 9082 5701 5702; do
    PID=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$PID" ]; then
        echo "Killing process on port $port (PID: $PID)"
        kill -9 $PID 2>/dev/null
    fi
done

# Remove PID files
rm -f node1.pid node2.pid

echo ""
echo "All cluster processes stopped."
echo "Note: Apache HTTP Server is still running if it was started separately."
echo "To stop Apache, run: sudo systemctl stop apache2"

