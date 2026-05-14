# DevOps & Cloud — Lesson 9: Nginx, Reverse Proxy, SSL/TLS

## Why a Reverse Proxy?

```
Direct to Service:                   Via Reverse Proxy:
┌──────────┐                         ┌──────────┐  ┌──────────┐
│ Client   │────▶ :8080              │ Client   │──▶│ :443     │
└──────────┘                         └──────────┘  │  Nginx   │
                                                    └────┬─────┘
                                                         │
                                              ┌──────────┼──────────┐
                                              ▼          ▼          ▼
                                        ┌────────┐ ┌────────┐ ┌────────┐
                                        │:8080   │ │:8081   │ │:8082   │
                                        │User    │ │Order   │ │Payment │
                                        │Service │ │Service │ │Service │
                                        └────────┘ └────────┘ └────────┘
```

| Without Reverse Proxy | With Reverse Proxy |
|----------------------|-------------------|
| Expose ports directly | Single entry point |
| No SSL termination | SSL at proxy (simpler) |
| No load balancing | Load balance across instances |
| No caching | Cache static content |
| No rate limiting | Rate limit per client |
| Direct IP exposure | Hide backend topology |

## Installing Nginx

```bash
# Ubuntu/Debian
sudo apt update && sudo apt install nginx -y

# Start and enable
sudo systemctl start nginx
sudo systemctl enable nginx

# Check status
sudo systemctl status nginx
nginx -t  # Test configuration
```

## Basic Reverse Proxy

```nginx
# /etc/nginx/sites-available/java-app
server {
    listen 80;
    server_name api.myapp.com;

    # Forward all requests to Spring Boot
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Increase timeout for long requests
        proxy_read_timeout 120s;
        proxy_connect_timeout 30s;
    }
}
```

## Load Balancing Multiple Instances

```nginx
upstream java_backend {
    # Load balancing methods
    # default: round-robin
    # least_conn — send to least busy
    # ip_hash — sticky sessions

    server 127.0.0.1:8080 weight=3;   # 3x more traffic
    server 127.0.0.1:8081 weight=2;
    server 127.0.0.1:8082 weight=1;
    server 10.0.1.10:8080;             # Another server

    # Health checks
    keepalive 32;
}

server {
    listen 80;
    server_name api.myapp.com;

    location / {
        proxy_pass http://java_backend;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## Routing by Path

```nginx
upstream user_service {
    server 127.0.0.1:8081;
}

upstream order_service {
    server 127.0.0.1:8082;
}

upstream payment_service {
    server 127.0.0.1:8083;
}

server {
    listen 80;
    server_name api.myapp.com;

    location /api/users {
        proxy_pass http://user_service;
    }

    location /api/orders {
        proxy_pass http://order_service;
    }

    location /api/payments {
        proxy_pass http://payment_service;
    }

    # Static files — no proxy needed
    location /static/ {
        root /var/www/myapp;
        expires 30d;
    }

    # Deny access to sensitive paths
    location ~ /\.(git|env|config) {
        deny all;
    }
}
```

## SSL/TLS with Let's Encrypt

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx -y

# Get certificate
sudo certbot --nginx -d api.myapp.com

# Auto-renewal (certbot adds systemd timer)
sudo certbot renew --dry-run
```

### Manual SSL Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name api.myapp.com;

    ssl_certificate /etc/letsencrypt/live/api.myapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.myapp.com/privkey.pem;

    # Modern SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    ssl_stapling on;
    ssl_stapling_verify on;
    ssl_dhparam /etc/ssl/certs/dhparam.pem;

    # HSTS (HTTP Strict Transport Security)
    add_header Strict-Transport-Security "max-age=63072000" always;

    # Security headers
    add_header X-Frame-Options SAMEORIGIN;
    add_header X-Content-Type-Options nosniff;
    add_header X-XSS-Protection "1; mode=block";

    location / {
        proxy_pass http://java_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name api.myapp.com;
    return 301 https://$server_name$request_uri;
}
```

## Rate Limiting

```nginx
# Define rate limit zone (10MB = ~160,000 sessions)
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=10r/s;

server {
    listen 443 ssl;
    server_name api.myapp.com;

    location /api/ {
        limit_req zone=api_limit burst=20 nodelay;
        limit_req_status 429;

        # Burst: allow up to 20 excess requests
        # nodelay: process burst immediately, don't delay

        proxy_pass http://java_backend;
    }

    # Stricter limit for auth endpoints
    location /api/auth/ {
        limit_req zone=auth_limit:10m rate=3r/s burst=5 nodelay;
        proxy_pass http://java_backend;
    }
}
```

## Caching Static Content

```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=static_cache:10m max_size=1g inactive=60m;

server {
    location /static/ {
        proxy_cache static_cache;
        proxy_cache_valid 200 30d;
        proxy_cache_valid 404 1m;
        proxy_cache_use_stale error timeout updating;
        add_header X-Cache-Status $upstream_cache_status;
        proxy_pass http://java_backend;
    }

    location /api/ {
        proxy_cache_bypass $http_cache_control;
        proxy_no_cache $http_pragma;
        proxy_pass http://java_backend;
    }
}
```

## Full Production Configuration

```nginx
# /etc/nginx/nginx.conf
user www-data;
worker_processes auto;
pid /run/nginx.pid;

events {
    worker_connections 4096;
    multi_accept on;
    use epoll;
}

http {
    # Basic settings
    sendfile on;
    tcp_nopush on;
    tcp_nodelay on;
    keepalive_timeout 65;
    types_hash_max_size 2048;

    # MIME types
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # Logging
    log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                    '$status $body_bytes_sent "$http_referer" '
                    '"$http_user_agent" "$http_x_forwarded_for" '
                    '$upstream_response_time $request_time';
    access_log /var/log/nginx/access.log main;
    error_log /var/log/nginx/error.log warn;

    # Gzip
    gzip on;
    gzip_vary on;
    gzip_proxied any;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml text/javascript;

    # Rate limit zones
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    # Include site configs
    include /etc/nginx/sites-enabled/*;
}
```

## Nginx Configuration Directives Deep Dive

```nginx
# Core directives explained

# Events block — connection processing
events {
    worker_connections 4096;     # Max connections per worker
    multi_accept on;             # Accept multiple connections at once
    use epoll;                   # Linux async I/O (auto-detected)
}

# HTTP block — global HTTP settings
http {
    # Basic tuning
    sendfile on;                 # Efficient file serving (kernel bypass)
    tcp_nopush on;               # Send headers in one packet
    tcp_nodelay on;              # Disable Nagle's algorithm
    keepalive_timeout 65;        # Keep idle connections alive
    keepalive_requests 1000;     # Max requests per keepalive connection
    client_max_body_size 10m;    # Max upload size
    client_body_timeout 30;      # Body read timeout

    # Location matching rules (order matters):
    # 1. = /exact     (exact match)
    # 2. ^~ /prefix   (prefix match, stop searching)
    # 3. ~ case       (regex, case-sensitive)
    # 4. ~* case      (regex, case-insensitive)
    # 5. /prefix      (prefix match, used if no regex matches)

    location = /health {
        # Exact match — highest priority
        return 200 "OK";
        add_header Content-Type text/plain;
    }

    location ^~ /static/ {
        # Prefix match — stop if matched, no regex
        root /var/www/myapp;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }

    location ~* \.(jpg|png|css|js)$ {
        # Regex match — static assets
        expires 7d;
        add_header Cache-Control "public";
    }

    location /api/ {
        # Prefix match — proxied to backend
        proxy_pass http://java_backend;
    }

    # Upstream server groups
    upstream java_backend {
        # Load balancing algorithms:
        # round-robin (default) — distributes evenly
        # least_conn — sends to least active connection
        # ip_hash — sticky sessions based on client IP
        # random — randomly selects
        # hash $request_uri — consistent hashing

        least_conn;

        server 10.0.1.10:8080 weight=5 max_fails=3 fail_timeout=30s;
        server 10.0.1.11:8080 weight=3;
        server 10.0.1.12:8080 backup;           # Backup server
        server 10.0.1.13:8080 down;              # Disabled (maintenance)

        # Health checks (requires nginx plus or third-party module)
        # zone backend 64k;
        # health_check interval=5s fails=3 passes=2;
    }
}
```

## Load Balancing Algorithms

```nginx
# Round Robin (default) — evenly distributes requests
upstream backend_round_robin {
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}

# Least Connections — sends to backend with fewest active connections
upstream backend_least_conn {
    least_conn;
    server backend1:8080;
    server backend2:8080;
}

# IP Hash — client IP ensures same backend every time (sticky sessions)
upstream backend_ip_hash {
    ip_hash;
    server backend1:8080;
    server backend2:8080;
}

# Generic Hash — consistent hashing (e.g., by URI for caching)
upstream backend_hash {
    hash $request_uri consistent;
    server backend1:8080;
    server backend2:8080;
}

# Random — randomly pick two, choose least loaded
upstream backend_random {
    random two least_conn;
    server backend1:8080;
    server backend2:8080;
    server backend3:8080;
}

# Weighted distribution
upstream backend_weighted {
    server backend1:8080 weight=5;   # 50% of traffic
    server backend2:8080 weight=3;   # 30%
    server backend3:8080 weight=2;   # 20%
}
```

## Rate Limiting — Expanded

```nginx
# Rate limiting zones — shared memory for tracking
# Syntax: limit_req_zone key zone=name:size rate=rate;

# Per IP address
limit_req_zone $binary_remote_addr zone=per_ip:10m rate=10r/s;

# Per server name (domain)
limit_req_zone $server_name zone=per_domain:10m rate=100r/s;

# Per URI (per-endpoint)
limit_req_zone $request_uri zone=per_uri:10m rate=5r/s;

# Combined key (IP + URI)
limit_req_zone $binary_remote_addr$request_uri zone=per_ip_uri:10m rate=10r/s;

# Connection limiting (concurrent connections, not rate)
limit_conn_zone $binary_remote_addr zone=conn_per_ip:10m;

server {
    listen 80;
    server_name api.myapp.com;

    # Apply rate limiting
    location /api/ {
        # 10 requests/sec per IP, burst 20, nodelay
        # Without nodelay: excess requests are delayed (throttled)
        # With nodelay: excess within burst processed immediately
        # After burst: 429 returned
        limit_req zone=per_ip burst=20 nodelay;
        limit_req_status 429;
        limit_req_log_level warn;

        # Also limit concurrent connections
        limit_conn conn_per_ip 10;
        limit_conn_status 503;

        proxy_pass http://java_backend;
    }

    # Stricter limit for auth endpoints
    location /api/auth/ {
        limit_req zone=per_ip burst=5 nodelay;
        limit_req_status 429;

        # Custom error page or redirect
        error_page 429 /rate-limited.html;

        proxy_pass http://java_backend;
    }

    # No rate limiting for static content
    location /static/ {
        root /var/www/myapp;
    }
}
```

### Rate Limiting Testing

```bash
# Test rate limiting — should see 200s then 429s
for i in {1..30}; do
  curl -s -o /dev/null -w "%{http_code}\n" http://api.myapp.com/api/
done

# With timing
for i in {1..30}; do
  curl -s -o /dev/null -w "Request $i: %{http_code} (%{time_total}s)\n" http://api.myapp.com/api/
done

# Concurrent connections
ab -n 100 -c 20 http://api.myapp.com/api/
```

## SSL/TLS Certificates — Let's Encrypt with Certbot

```bash
# Install certbot
sudo apt update && sudo apt install certbot python3-certbot-nginx -y

# Obtain certificate (auto-configures nginx)
sudo certbot --nginx -d api.myapp.com -d www.myapp.com \
  --non-interactive --agree-tos -m admin@myapp.com

# Obtain wildcard certificate (requires DNS challenge)
sudo certbot certonly --manual --preferred-challenges dns \
  -d *.myapp.com -d myapp.com

# List certificates
sudo certbot certificates

# Renew all certificates
sudo certbot renew

# Force renewal (even if not expiring)
sudo certbot renew --force-renewal

# Check renewal timer
sudo systemctl list-timers | grep certbot

# Revoke certificate
sudo certbot revoke --cert-name api.myapp.com
```

### mTLS (Mutual TLS)

```nginx
# mTLS — client also presents a certificate

server {
    listen 443 ssl;
    server_name api.myapp.com;

    # Server certificate
    ssl_certificate /etc/ssl/certs/server.crt;
    ssl_certificate_key /etc/ssl/private/server.key;

    # Client certificate verification
    ssl_client_certificate /etc/ssl/certs/ca.crt;
    ssl_verify_client on;           # Require client cert
    ssl_verify_depth 2;             # Verify CA chain depth

    # Pass verification result to backend
    proxy_set_header X-SSL-Client-Cert $ssl_client_cert;
    proxy_set_header X-SSL-Client-Verify $ssl_client_verify;
    proxy_set_header X-SSL-Client-Subject $ssl_client_s_subject;
    proxy_set_header X-SSL-Client-Issuer $ssl_client_i_dn;
    proxy_set_header X-SSL-Client-Serial $ssl_client_serial;

    # Reject requests without valid client certificate
    if ($ssl_client_verify != SUCCESS) {
        return 403;
    }

    location / {
        proxy_pass http://java_backend;
    }
}

# Example Java client with mTLS
# System.setProperty("javax.net.ssl.keyStore", "client-keystore.p12");
# System.setProperty("javax.net.ssl.keyStorePassword", "password");
# System.setProperty("javax.net.ssl.trustStore", "truststore.p12");
# System.setProperty("javax.net.ssl.trustStorePassword", "password");
```

## HTTP/2 and HTTP/3 Support

```nginx
server {
    # HTTP/2 — requires TLS
    listen 443 ssl http2;
    server_name api.myapp.com;

    # HTTP/3 (QUIC) — requires nginx 1.25+
    listen 443 quic reuseport;

    ssl_certificate /etc/letsencrypt/live/api.myapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.myapp.com/privkey.pem;

    # HTTP/2 optimization
    http2_max_concurrent_streams 128;
    http2_chunk_size 8k;
    http2_body_preread_size 64k;
    http2_recv_timeout 30;
    http2_idle_timeout 300;

    # HTTP/3 headers
    add_header Alt-Svc 'h3=":443"; ma=86400';

    # HTTP/2 push (be selective — over-pushing hurts)
    # http2_push /css/app.css;
    # http2_push_preload on;  # Honor Link: rel=preload headers

    location / {
        # HTTP/3 backend connections
        proxy_http_version 1.1;
        proxy_set_header Connection "";
        proxy_set_header Upgrade $http_upgrade;

        # Forward protocol info
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;

        proxy_pass http://java_backend;
    }
}
```

## Nginx as API Gateway

```nginx
# Advanced API Gateway configuration

# Cache zone for API responses
proxy_cache_path /var/cache/nginx/api levels=1:2
    keys_zone=api_cache:10m max_size=1g inactive=60m use_temp_path=off;

# Auth request endpoint
upstream auth_service {
    server auth-server:8081;
}

upstream user_service {
    server user-service:8082;
}

upstream order_service {
    server order-service:8083;
}

server {
    listen 443 ssl http2;
    server_name api.myapp.com;

    # Auth request — validates token before proxying
    location = /_auth {
        internal;  # Only accessible via auth_request directive
        proxy_pass http://auth_service/api/auth/validate;
        proxy_pass_request_body off;
        proxy_set_header Content-Length "";
        proxy_set_header X-Original-URI $request_uri;
        proxy_set_header X-Original-Method $request_method;

        # Cache auth result for 1 minute
        proxy_cache api_cache;
        proxy_cache_key "$cookie_sessionid";
        proxy_cache_valid 200 1m;
        proxy_cache_use_stale error timeout;
    }

    # API endpoints with auth
    location /api/users {
        auth_request /_auth;
        auth_request_set $auth_user $upstream_http_x_auth_user;
        auth_request_set $auth_roles $upstream_http_x_auth_roles;

        # Forward auth info to backend
        proxy_set_header X-Auth-User $auth_user;
        proxy_set_header X-Auth-Roles $auth_roles;

        # Rate limit
        limit_req zone=per_api burst=50 nodelay;

        proxy_pass http://user_service;
    }

    location /api/orders {
        auth_request /_auth;
        proxy_pass http://order_service;
    }

    # Response caching for GET requests
    location /api/cacheable {
        proxy_cache api_cache;
        proxy_cache_key "$scheme$request_method$host$request_uri";
        proxy_cache_valid 200 5m;
        proxy_cache_valid 404 1m;
        proxy_cache_use_stale error timeout updating;
        proxy_cache_background_update on;
        proxy_cache_lock on;
        add_header X-Cache-Status $upstream_cache_status;
        proxy_pass http://order_service;
    }

    # WebSocket proxying
    location /ws/ {
        proxy_pass http://websocket-server;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 86400;  # 24h for websockets
    }

    # Request/response body limits and inspection
    location /api/upload {
        client_max_body_size 100m;
        client_body_buffer_size 128k;
        proxy_request_buffering on;
        proxy_pass http://upload-service;
    }
}
```

## Advanced Security Headers and Configuration

```nginx
server {
    listen 443 ssl http2;
    server_name api.myapp.com;

    # Content Security Policy
    add_header Content-Security-Policy "
        default-src 'self';
        script-src 'self' 'unsafe-inline' 'strict-dynamic';
        style-src 'self' 'unsafe-inline';
        img-src 'self' data: https:;
        font-src 'self';
        connect-src 'self' https://api.myapp.com;
        frame-ancestors 'none';
        form-action 'self';
        base-uri 'self';
        upgrade-insecure-requests;
    " always;

    # Other security headers
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;
    add_header Permissions-Policy "camera=(), microphone=(), geolocation=()" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Disable server version disclosure
    server_tokens off;

    # Hide upstream response headers
    proxy_hide_header X-Powered-By;
    proxy_hide_header Server;

    location / {
        proxy_pass http://java_backend;
    }
}
```

## Troubleshooting

```bash
# Test configuration
nginx -t

# Reload without downtime
sudo nginx -s reload

# View logs
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log

# Check if port is in use
sudo netstat -tlnp | grep :80
sudo ss -tlnp | grep nginx

# Verify SSL certificate
openssl s_client -connect api.myapp.com:443 -servername api.myapp.com

# Test rate limiting
for i in {1..20}; do curl -s -o /dev/null -w "%{http_code}\n" https://api.myapp.com/api/; done

# Test upstream health
curl -I http://backend1:8080/actuator/health

# Analyze slow requests from logs
awk '{if ($NF > 1) print $0}' /var/log/nginx/access.log  # Requests > 1s

# Top slowest endpoints
awk '{print $7, $NF}' /var/log/nginx/access.log | sort -k2 -rn | head -10

# Connection stats
sudo ss -s
sudo ss -tn state time-wait | wc -l
```

## Exercises

1. Install Nginx and configure it as a reverse proxy for a Spring Boot app on port 8080.
2. Set up load balancing between two instances of the same app.
3. Obtain a Let's Encrypt SSL certificate and configure HTTPS.
4. Implement rate limiting on an API endpoint and test it.
5. Configure path-based routing to different backend services.
