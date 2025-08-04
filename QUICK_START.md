# Quick Start Guide - Spring Boot Cluster

## Overview
Complete Spring Boot 3.4.5 cluster with Hazelcast session sharing, Apache load balancer, and disabled authentication.

## What's Included
- ✅ Spring Boot 3.4.5 application
- ✅ Spring Security with CSP headers (authentication disabled)
- ✅ Hazelcast 5.3.6 for session sharing
- ✅ 2-node cluster configuration
- ✅ Apache HTTP Server load balancer configuration
- ✅ Startup/shutdown scripts
- ✅ Complete documentation

## Quick Setup (5 minutes)

### 1. Build & Start Cluster
```bash
cd springboot-cluster
mvn clean package
./scripts/start-cluster.sh
```

### 2. Configure Apache
```bash
# Enable modules
sudo a2enmod proxy proxy_http proxy_balancer lbmethod_byrequests headers rewrite

# Copy configuration
sudo cp apache-config/000-default.conf /etc/apache2/sites-available/

# Restart Apache
sudo systemctl restart apache2
```

### 3. Test Everything
```bash
# Test load balancer
curl http://localhost/api/health

# Test session sharing
curl -c cookies.txt http://localhost/api/session
curl -b cookies.txt -X POST "http://localhost/api/session/attribute?key=test&value=hello"
curl -b cookies.txt http://localhost/api/session/attribute/test
```

## Architecture
```
Users → Apache (Port 80) → Node1 (8081) + Node2 (8082)
                              ↓
                         Hazelcast Cluster
                        (Session Sharing)
```

## Key Features
- **No Authentication**: All APIs accessible without login
- **CSP Headers**: Content Security Policy enabled
- **Session Sharing**: Sessions work across both nodes
- **Sticky Sessions**: Users stick to same node when possible
- **Health Monitoring**: Built-in health checks and monitoring

## Ports Used
- **80**: Apache HTTP Server (load balancer)
- **8081**: Spring Boot Node1
- **8082**: Spring Boot Node2
- **9081**: Node1 Management (Actuator)
- **9082**: Node2 Management (Actuator)
- **5701**: Hazelcast Node1
- **5702**: Hazelcast Node2

## Important URLs
- **Load Balanced App**: http://localhost
- **Balancer Manager**: http://localhost/balancer-manager
- **Node1 Direct**: http://localhost:8081
- **Node2 Direct**: http://localhost:8082
- **Health Check**: http://localhost/api/health
- **Session Test**: http://localhost/api/session

## Stop Cluster
```bash
./scripts/stop-cluster.sh
```

## Need Help?
- Check `docs/README.md` for detailed documentation
- Check `docs/APACHE_SETUP.md` for Apache configuration help
- View logs in `logs/` directory
- Test individual nodes first, then load balancer

## File Structure
```
springboot-cluster/
├── src/                     # Java source code
├── node1/                   # Node1 configuration
├── node2/                   # Node2 configuration
├── apache-config/           # Apache configurations
├── scripts/                 # Start/stop scripts
├── docs/                    # Documentation
├── pom.xml                  # Maven configuration
└── QUICK_START.md          # This file
```

