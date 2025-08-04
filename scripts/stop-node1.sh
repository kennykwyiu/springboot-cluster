#!/bin/bash

# Stop Node1 of Spring Boot Cluster

echo "Stopping Spring Boot Cluster - Node1..."

# Check if PID file exists
if [ -f "node1.pid" ]; then
    NODE1_PID=$(cat node1.pid)
    
    # Check if process is running
    if ps -p $NODE1_PID > /dev/null 2>&1; then
        echo "Stopping Node1 with PID: $NODE1_PID"
        kill $NODE1_PID
        
        # Wait for graceful shutdown
        sleep 5
        
        # Force kill if still running
        if ps -p $NODE1_PID > /dev/null 2>&1; then
            echo "Force killing Node1..."
            kill -9 $NODE1_PID
        fi
        
        echo "Node1 stopped successfully"
    else
        echo "Node1 process not found (PID: $NODE1_PID)"
    fi
    
    # Remove PID file
    rm -f node1.pid
else
    echo "PID file not found. Attempting to find and stop Node1 process..."
    
    # Try to find the process by port
    NODE1_PID=$(lsof -ti:8081)
    if [ ! -z "$NODE1_PID" ]; then
        echo "Found Node1 process on port 8081 with PID: $NODE1_PID"
        kill $NODE1_PID
        sleep 3
        if ps -p $NODE1_PID > /dev/null 2>&1; then
            kill -9 $NODE1_PID
        fi
        echo "Node1 stopped"
    else
        echo "No process found running on port 8081"
    fi
fi

