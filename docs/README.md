# Spring Boot Cluster with Hazelcast Session Sharing

This project demonstrates a complete Spring Boot 3.4.5 cluster setup with the following features:

- **Spring Security** with CSP headers and disabled authentication
- **Hazelcast** for distributed session sharing
- **Apache HTTP Server** as a load balancer proxy
- **Two-node cluster** configuration
- **Sticky sessions** for session affinity

## Architecture Overview

```
Internet/Users
      ↓
Apache HTTP Server (Port 80)
      ↓ (Load Balancer)
   ┌─────────────────┐
   ↓                 ↓
Node1 (Port 8081)   Node2 (Port 8082)
   ↓                 ↓
Hazelcast (5701)   Hazelcast (5702)
   └─────────────────┘
   (Session Sharing)
```

## Technology Stack

- **Spring Boot**: 3.4.5
- **Java**: 17+
- **Spring Security**: CSP headers, disabled authentication
- **Hazelcast**: 5.3.6 for session clustering
- **Apache HTTP Server**: 2.4+ for load balancing
- **Maven**: Build tool

## Project Structure

```
springboot-cluster/
├── src/main/java/com/example/cluster/
│   ├── ClusterApplication.java          # Main application class
│   ├── config/
│   │   ├── SecurityConfig.java          # Spring Security configuration
│   │   └── HazelcastConfig.java         # Hazelcast configuration
│   └── controller/
│       └── ApiController.java           # REST API endpoints
├── src/main/resources/
│   └── application.properties           # Base configuration
├── node1/
│   └── application-node1.properties     # Node1 specific config
├── node2/
│   └── application-node2.properties     # Node2 specific config
├── apache-config/
│   ├── httpd.conf                       # Apache main config
│   ├── springboot-cluster.conf          # Virtual host config
│   └── 000-default.conf                 # Simplified site config
├── scripts/
│   ├── start-node1.sh                   # Start Node1
│   ├── start-node2.sh                   # Start Node2
│   ├── stop-node1.sh                    # Stop Node1
│   ├── stop-node2.sh                    # Stop Node2
│   ├── start-cluster.sh                 # Start entire cluster
│   └── stop-cluster.sh                  # Stop entire cluster
├── docs/
│   └── README.md                        # This documentation
└── pom.xml                              # Maven configuration
```

## Features

### Spring Security Configuration
- **Disabled Authentication**: All API endpoints are accessible without authentication
- **CSP Headers**: Comprehensive Content Security Policy headers
- **Security Headers**: X-Frame-Options, X-Content-Type-Options, etc.
- **CORS Support**: Cross-origin requests enabled

### Hazelcast Session Sharing
- **Distributed Sessions**: Sessions shared across all cluster nodes
- **Automatic Discovery**: TCP/IP based cluster discovery
- **Session Persistence**: 30-minute session timeout
- **High Availability**: Backup replicas for session data

### Load Balancing
- **Apache HTTP Server**: Acts as reverse proxy and load balancer
- **Sticky Sessions**: Session affinity using CLUSTERSESSIONID cookie
- **Health Checks**: Automatic health monitoring of backend nodes
- **Balancer Manager**: Web interface for monitoring and management

## Prerequisites

1. **Java 17+** installed
2. **Maven 3.6+** installed
3. **Apache HTTP Server 2.4+** installed
4. **curl** (for testing)
5. **jq** (optional, for JSON formatting)

## Quick Start

### 1. Build the Application

```bash
cd springboot-cluster
mvn clean package
```

### 2. Start the Cluster

```bash
./scripts/start-cluster.sh
```

This will:
- Start Node1 on port 8081
- Start Node2 on port 8082
- Display health check results
- Show next steps for Apache configuration

### 3. Configure Apache HTTP Server

#### Enable Required Modules
```bash
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests
sudo a2enmod headers
sudo a2enmod rewrite
```

#### Copy Configuration
```bash
sudo cp apache-config/000-default.conf /etc/apache2/sites-available/
sudo a2ensite 000-default
```

#### Restart Apache
```bash
sudo systemctl restart apache2
```

### 4. Test the Setup

#### Direct Node Access
```bash
# Node1
curl http://localhost:8081/api/health

# Node2
curl http://localhost:8082/api/health
```

#### Load Balanced Access
```bash
# Through Apache (port 80)
curl http://localhost/api/health
curl http://localhost/api/info
```

#### Session Testing
```bash
# Create a session
curl -c cookies.txt http://localhost/api/session

# Set session attribute
curl -b cookies.txt -X POST "http://localhost/api/session/attribute?key=test&value=hello"

# Get session attribute (should work on any node)
curl -b cookies.txt http://localhost/api/session/attribute/test
```

### 5. Monitor the Cluster

- **Balancer Manager**: http://localhost/balancer-manager
- **Node1 Actuator**: http://localhost:9081
- **Node2 Actuator**: http://localhost:9082

## Configuration Details

### Node Configuration

#### Node1 (Port 8081)
- Application Port: 8081
- Management Port: 9081
- Hazelcast Port: 5701
- Log File: logs/node1.log

#### Node2 (Port 8082)
- Application Port: 8082
- Management Port: 9082
- Hazelcast Port: 5702
- Log File: logs/node2.log

### Hazelcast Configuration
- Cluster Name: spring-boot-cluster
- Discovery: TCP/IP with members 127.0.0.1:5701,127.0.0.1:5702
- Session Map: spring:session:sessions
- Session Timeout: 30 minutes
- Backup Count: 1 replica

### Apache Configuration
- Listen Port: 80
- Backend Nodes: 127.0.0.1:8081, 127.0.0.1:8082
- Load Balancing: Round-robin with sticky sessions
- Session Cookie: CLUSTERSESSIONID

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/api/health` | GET | Health check |
| `/api/info` | GET | Application information |
| `/api/test` | GET | Simple test endpoint |
| `/api/session` | GET | Session information |
| `/api/session/attribute` | POST | Set session attribute |
| `/api/session/attribute/{key}` | GET | Get session attribute |

## Management and Monitoring

### Actuator Endpoints
- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`
- Hazelcast: `/actuator/hazelcast`

### Apache Monitoring
- Balancer Manager: `/balancer-manager`
- Server Status: `/server-status`
- Server Info: `/server-info`

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   ```bash
   # Check what's using the port
   lsof -i :8081
   lsof -i :8082
   
   # Kill the process
   kill -9 <PID>
   ```

2. **Hazelcast Connection Issues**
   - Check firewall settings
   - Verify network configuration
   - Check logs for connection errors

3. **Apache Module Issues**
   ```bash
   # Check enabled modules
   apache2ctl -M | grep proxy
   
   # Enable missing modules
   sudo a2enmod proxy_balancer
   ```

4. **Session Not Shared**
   - Verify Hazelcast cluster formation
   - Check session cookie configuration
   - Verify sticky session setup

### Log Files
- Node1: `logs/node1.log`
- Node2: `logs/node2.log`
- Apache Error: `/var/log/apache2/error.log`
- Apache Access: `/var/log/apache2/access.log`

### Useful Commands

```bash
# Check cluster status
./scripts/start-cluster.sh

# Stop cluster
./scripts/stop-cluster.sh

# Check running processes
ps aux | grep java

# Check port usage
netstat -tlnp | grep -E ':(8081|8082|5701|5702|80)'

# Test load balancing
for i in {1..10}; do curl -s http://localhost/api/info | jq .serverPort; done
```

## Security Considerations

1. **CSP Headers**: Configured to prevent XSS attacks
2. **Security Headers**: X-Frame-Options, X-Content-Type-Options, etc.
3. **Authentication Disabled**: All endpoints are public (as requested)
4. **HTTPS**: Consider enabling SSL/TLS for production
5. **Firewall**: Configure appropriate firewall rules

## Production Deployment

For production deployment, consider:

1. **SSL/TLS Configuration**
2. **Firewall Rules**
3. **Monitoring and Alerting**
4. **Backup and Recovery**
5. **Resource Limits**
6. **Security Hardening**

## Support

For issues and questions:
1. Check the logs in the `logs/` directory
2. Verify configuration files
3. Test individual components
4. Check network connectivity
5. Review Apache error logs

