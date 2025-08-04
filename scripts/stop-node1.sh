#!/bin/bash

# Script to stop Node1 of the Spring Boot Cluster.
# This script attempts to gracefully shut down Node1, and if unsuccessful, force-kills the process.

echo "Stopping Spring Boot Cluster - Node1..."

# Check if the PID (Process ID) file for Node1 exists.
# The PID file is created by the start-node1.sh script to keep track of the running process.
if [ -f "node1.pid" ]; then
    NODE1_PID=$(cat node1.pid) # Read the PID from the file.
    
    # Check if a process with the retrieved PID is currently running.
    if ps -p $NODE1_PID > /dev/null 2>&1; then
        echo "Stopping Node1 with PID: $NODE1_PID"
        kill $NODE1_PID # Send a SIGTERM signal to the process for graceful shutdown.
        
        # Wait for a few seconds to allow the application to shut down gracefully.
        sleep 5
        
        # After waiting, check again if the process is still running.
        if ps -p $NODE1_PID > /dev/null 2>&1; then
            echo "Force killing Node1..." # If still running, force kill it.
            kill -9 $NODE1_PID # Send a SIGKILL signal to immediately terminate the process.
        fi
        
        echo "Node1 stopped successfully"
    else
        echo "Node1 process not found (PID: $NODE1_PID)" # Inform if the process is not found.
    fi
    
    # Remove the PID file after attempting to stop the process.
    rm -f node1.pid
else
    echo "PID file not found. Attempting to find and stop Node1 process..."
    
    # If the PID file is not found, try to find the process by its listening port (8081).
    NODE1_PID=$(lsof -ti:8081) # Use lsof to find the PID of the process listening on port 8081.
    if [ ! -z "$NODE1_PID" ]; then
        echo "Found Node1 process on port 8081 with PID: $NODE1_PID"
        kill $NODE1_PID # Send SIGTERM.
        sleep 3 # Wait for a short period.
        if ps -p $NODE1_PID > /dev/null 2>&1; then
            kill -9 $NODE1_PID # Force kill if still running.
        fi
        echo "Node1 stopped"
    else
        echo "No process found running on port 8081" # Inform if no process is found on the port.
    fi
fi

