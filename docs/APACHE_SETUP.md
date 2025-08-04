# Apache HTTP Server Setup Guide

This guide provides detailed instructions for configuring Apache HTTP Server as a load balancer for the Spring Boot cluster.

## Prerequisites

- Apache HTTP Server 2.4+ installed
- Root or sudo access
- Spring Boot cluster nodes running

## Installation

### Ubuntu/Debian
```bash
sudo apt update
sudo apt install apache2
```

### CentOS/RHEL
```bash
sudo yum install httpd
# or for newer versions
sudo dnf install httpd
```

## Module Configuration

### Enable Required Modules

```bash
# Enable proxy modules
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod proxy_balancer
sudo a2enmod lbmethod_byrequests

# Enable additional modules
sudo a2enmod headers
sudo a2enmod rewrite
sudo a2enmod status
sudo a2enmod info
```

### Verify Modules
```bash
apache2ctl -M | grep -E "(proxy|headers|rewrite)"
```

Expected output:
```
proxy_module (shared)
proxy_http_module (shared)
proxy_balancer_module (shared)
lbmethod_byrequests_module (shared)
headers_module (shared)
rewrite_module (shared)
```

## Configuration Files

### Option 1: Simple Configuration (Recommended)

Copy the provided configuration:
```bash
sudo cp apache-config/000-default.conf /etc/apache2/sites-available/
sudo a2ensite 000-default
sudo systemctl reload apache2
```

### Option 2: Custom Virtual Host

Create a new virtual host:
```bash
sudo cp apache-config/springboot-cluster.conf /etc/apache2/sites-available/
sudo a2ensite springboot-cluster
sudo a2dissite 000-default  # Disable default site
sudo systemctl reload apache2
```

### Option 3: Manual Configuration

Edit the default site configuration:
```bash
sudo nano /etc/apache2/sites-available/000-default.conf
```

Add the following content:
```apache
<VirtualHost *:80>
    ServerAdmin webmaster@localhost
    DocumentRoot /var/www/html

    # Enable proxy
    ProxyRequests Off
    ProxyPreserveHost On

    # Define backend cluster
    <Proxy balancer://springboot-cluster>
        BalancerMember http://127.0.0.1:8081 route=node1
        BalancerMember http://127.0.0.1:8082 route=node2
        ProxySet lbmethod=byrequests
    </Proxy>

    # Sticky sessions
    RewriteEngine On
    RewriteCond %{HTTP_COOKIE} CLUSTERSESSIONID=([^;]+)\.([^;]+)
    RewriteRule ^(.*)$ $1 [E=ROUTEID:%2]

    # Proxy configuration
    ProxyPass /balancer-manager !
    ProxyPass / balancer://springboot-cluster/ stickysession=CLUSTERSESSIONID
    ProxyPassReverse / balancer://springboot-cluster/

    # Balancer manager
    <Location "/balancer-manager">
        SetHandler balancer-manager
        Require local
    </Location>

    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined
</VirtualHost>
```

## Service Management

### Start Apache
```bash
sudo systemctl start apache2
sudo systemctl enable apache2  # Enable auto-start
```

### Check Status
```bash
sudo systemctl status apache2
```

### Restart/Reload
```bash
sudo systemctl restart apache2  # Full restart
sudo systemctl reload apache2   # Reload configuration
```

## Testing Configuration

### Test Configuration Syntax
```bash
sudo apache2ctl configtest
```

### Test Load Balancing
```bash
# Test multiple requests
for i in {1..10}; do
    curl -s http://localhost/api/info | grep serverPort
done
```

### Test Sticky Sessions
```bash
# Create session and test persistence
curl -c cookies.txt http://localhost/api/session
curl -b cookies.txt http://localhost/api/session
curl -b cookies.txt http://localhost/api/session
```

## Monitoring and Management

### Balancer Manager
Access the balancer manager at: http://localhost/balancer-manager

Features:
- View backend server status
- Enable/disable backend servers
- Adjust load balancing weights
- View connection statistics

### Server Status
Access server status at: http://localhost/server-status

### Log Files
- Error Log: `/var/log/apache2/error.log`
- Access Log: `/var/log/apache2/access.log`
- Custom Logs: `/var/log/apache2/springboot-cluster-*.log`

## Advanced Configuration

### SSL/TLS Configuration

Create SSL virtual host:
```apache
<VirtualHost *:443>
    ServerName your-domain.com
    
    SSLEngine on
    SSLCertificateFile /path/to/certificate.crt
    SSLCertificateKeyFile /path/to/private.key
    
    # Same proxy configuration as HTTP
    ProxyRequests Off
    ProxyPreserveHost On
    
    <Proxy balancer://springboot-cluster-ssl>
        BalancerMember http://127.0.0.1:8081 route=node1
        BalancerMember http://127.0.0.1:8082 route=node2
        ProxySet lbmethod=byrequests
    </Proxy>
    
    ProxyPass / balancer://springboot-cluster-ssl/ stickysession=CLUSTERSESSIONID
    ProxyPassReverse / balancer://springboot-cluster-ssl/
</VirtualHost>
```

### Health Check Configuration

Add health checks to backend members:
```apache
<Proxy balancer://springboot-cluster>
    BalancerMember http://127.0.0.1:8081 route=node1 status=+H
    BalancerMember http://127.0.0.1:8082 route=node2 status=+H
    ProxySet lbmethod=byrequests
    ProxySet hcmethod=GET
    ProxySet hcuri=/api/health
    ProxySet hcinterval=30
</Proxy>
```

### Security Headers

Add security headers:
```apache
Header always set X-Frame-Options "DENY"
Header always set X-Content-Type-Options "nosniff"
Header always set X-XSS-Protection "1; mode=block"
Header always set Strict-Transport-Security "max-age=31536000; includeSubDomains"
Header always set Referrer-Policy "strict-origin-when-cross-origin"
```

## Troubleshooting

### Common Issues

1. **Module Not Found**
   ```bash
   # Check available modules
   apache2ctl -M
   
   # Enable missing modules
   sudo a2enmod proxy_balancer
   sudo systemctl reload apache2
   ```

2. **Permission Denied**
   ```bash
   # Check SELinux (CentOS/RHEL)
   sudo setsebool -P httpd_can_network_connect 1
   
   # Check file permissions
   ls -la /etc/apache2/sites-available/
   ```

3. **Backend Connection Failed**
   ```bash
   # Check if backend servers are running
   curl http://localhost:8081/api/health
   curl http://localhost:8082/api/health
   
   # Check firewall
   sudo ufw status
   sudo iptables -L
   ```

4. **Configuration Errors**
   ```bash
   # Test configuration
   sudo apache2ctl configtest
   
   # Check error logs
   sudo tail -f /var/log/apache2/error.log
   ```

### Debugging Commands

```bash
# Check listening ports
sudo netstat -tlnp | grep apache2

# Check proxy status
curl -s http://localhost/balancer-manager

# Test backend connectivity
telnet localhost 8081
telnet localhost 8082

# Monitor access logs
sudo tail -f /var/log/apache2/access.log

# Check Apache processes
ps aux | grep apache2
```

## Performance Tuning

### Worker Configuration

Edit `/etc/apache2/mods-available/mpm_prefork.conf`:
```apache
<IfModule mpm_prefork_module>
    StartServers             8
    MinSpareServers          5
    MaxSpareServers         20
    ServerLimit            256
    MaxRequestWorkers      256
    MaxConnectionsPerChild   0
</IfModule>
```

### Proxy Configuration

Add to virtual host:
```apache
ProxyTimeout 300
ProxyPreserveHost On
ProxyAddHeaders On

# Connection pooling
ProxyPass / balancer://springboot-cluster/ connectiontimeout=5 ttl=60
```

## Security Considerations

1. **Restrict Balancer Manager Access**
   ```apache
   <Location "/balancer-manager">
       SetHandler balancer-manager
       Require ip 127.0.0.1
       Require ip 10.0.0.0/8
       Require ip 172.16.0.0/12
       Require ip 192.168.0.0/16
   </Location>
   ```

2. **Hide Server Information**
   ```apache
   ServerTokens Prod
   ServerSignature Off
   ```

3. **Enable Security Headers**
   ```apache
   Header always set X-Frame-Options "DENY"
   Header always set X-Content-Type-Options "nosniff"
   ```

## Maintenance

### Log Rotation
Configure logrotate for Apache logs:
```bash
sudo nano /etc/logrotate.d/apache2
```

### Backup Configuration
```bash
# Backup configuration files
sudo tar -czf apache-config-backup.tar.gz /etc/apache2/
```

### Update Procedures
1. Test configuration changes in staging
2. Backup current configuration
3. Apply changes
4. Test functionality
5. Monitor logs for errors

