#!/bin/bash

# Script to stop the complete Spring Boot Cluster.
# This script orchestrates the shutdown of both Node1 and Node2
# and performs cleanup of any remaining processes.

echo "=========================================="
echo "Stopping Spring Boot Cluster"
echo "=========================================="

echo "Stopping Node1..."
# Execute the stop-node1.sh script to shut down the first node.
./scripts/stop-node1.sh

echo ""
echo "Stopping Node2..."
# Execute the stop-node2.sh script to shut down the second node.
./scripts/stop-node2.sh

echo ""
echo "=========================================="
echo "Cluster Stopped Successfully!"
echo "=========================================="

# Clean up any remaining processes.
# This loop iterates through common ports used by the cluster and kills any processes still listening on them.
echo "Cleaning up any remaining processes..."

# Kill any remaining Java processes on the cluster ports.
for port in 8081 8082 9081 9082 5701 5702; do
    # Find the PID of the process listening on the current port.
    PID=$(lsof -ti:$port 2>/dev/null)
    if [ ! -z "$PID" ]; then
        echo "Killing process on port $port (PID: $PID)"
        kill -9 $PID 2>/dev/null # Force kill the process.
    fi
done

# Remove PID files that were created during startup.
rm -f node1.pid node2.pid

echo ""
echo "All cluster processes stopped."
echo "Note: Apache HTTP Server is still running if it was started separately."
echo "To stop Apache, run: sudo systemctl stop apache2"

