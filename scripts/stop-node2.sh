#!/bin/bash

# Script to stop Node2 of the Spring Boot Cluster.
# This script attempts to gracefully shut down Node2, and if unsuccessful, force-kills the process.

echo "Stopping Spring Boot Cluster - Node2..."

# Check if the PID (Process ID) file for Node2 exists.
# The PID file is created by the start-node2.sh script to keep track of the running process.
if [ -f "node2.pid" ]; then
    NODE2_PID=$(cat node2.pid) # Read the PID from the file.
    
    # Check if a process with the retrieved PID is currently running.
    if ps -p $NODE2_PID > /dev/null 2>&1; then
        echo "Stopping Node2 with PID: $NODE2_PID"
        kill $NODE2_PID # Send a SIGTERM signal to the process for graceful shutdown.
        
        # Wait for a few seconds to allow the application to shut down gracefully.
        sleep 5
        
        # After waiting, check again if the process is still running.
        if ps -p $NODE2_PID > /dev/null 2>&1; then
            echo "Force killing Node2..." # If still running, force kill it.
            kill -9 $NODE2_PID # Send a SIGKILL signal to immediately terminate the process.
        fi
        
        echo "Node2 stopped successfully"
    else
        echo "Node2 process not found (PID: $NODE2_PID)" # Inform if the process is not found.
    }
    
    # Remove the PID file after attempting to stop the process.
    rm -f node2.pid
else
    echo "PID file not found. Attempting to find and stop Node2 process..."
    
    # If the PID file is not found, try to find the process by its listening port (8082).
    NODE2_PID=$(lsof -ti:8082) # Use lsof to find the PID of the process listening on port 8082.
    if [ ! -z "$NODE2_PID" ]; then
        echo "Found Node2 process on port 8082 with PID: $NODE2_PID"
        kill $NODE2_PID # Send SIGTERM.
        sleep 3 # Wait for a short period.
        if ps -p $NODE2_PID > /dev/null 2>&1; then
            kill -9 $NODE2_PID # Force kill if still running.
        fi
        echo "Node2 stopped"
    else
        echo "No process found running on port 8082" # Inform if no process is found on the port.
    fi
fi

