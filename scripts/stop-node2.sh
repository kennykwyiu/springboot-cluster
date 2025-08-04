#!/bin/bash

# Stop Node2 of Spring Boot Cluster

echo "Stopping Spring Boot Cluster - Node2..."

# Check if PID file exists
if [ -f "node2.pid" ]; then
    NODE2_PID=$(cat node2.pid)
    
    # Check if process is running
    if ps -p $NODE2_PID > /dev/null 2>&1; then
        echo "Stopping Node2 with PID: $NODE2_PID"
        kill $NODE2_PID
        
        # Wait for graceful shutdown
        sleep 5
        
        # Force kill if still running
        if ps -p $NODE2_PID > /dev/null 2>&1; then
            echo "Force killing Node2..."
            kill -9 $NODE2_PID
        fi
        
        echo "Node2 stopped successfully"
    else
        echo "Node2 process not found (PID: $NODE2_PID)"
    fi
    
    # Remove PID file
    rm -f node2.pid
else
    echo "PID file not found. Attempting to find and stop Node2 process..."
    
    # Try to find the process by port
    NODE2_PID=$(lsof -ti:8082)
    if [ ! -z "$NODE2_PID" ]; then
        echo "Found Node2 process on port 8082 with PID: $NODE2_PID"
        kill $NODE2_PID
        sleep 3
        if ps -p $NODE2_PID > /dev/null 2>&1; then
            kill -9 $NODE2_PID
        fi
        echo "Node2 stopped"
    else
        echo "No process found running on port 8082"
    fi
fi

