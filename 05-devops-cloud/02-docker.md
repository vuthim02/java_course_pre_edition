# DevOps & Cloud — Lesson 2: Docker & Docker Compose

## What is Docker?

Docker packages your application + all dependencies into a **container** — a lightweight, portable unit.

```
Traditional:                         Docker:
┌───────────────────┐               ┌──────────────────┐
│ Java App          │               │ Docker Container │
├───────────────────┤               │ ┌──────────────┐ │
│ Dependencies      │               │ │ Java App     │ │
│ (JRE, libs)       │               │ │ Dependencies  ││
├───────────────────┤               │ │ JRE 21       │ │
│ OS Libs           │               │ │ OS Libs      │ │
├───────────────────┤               │ └──────────────┘ │
│ Operating System  │               └──────────────────┘
└───────────────────┘               Runs anywhere Docker runs
```

## Dockerfile for Java

```dockerfile
# Multi-stage build — smaller final image

# Stage 1: Build with Maven
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline  # Cache dependencies
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Run with JRE (smaller)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

## Essential Docker Commands

```bash
# Build
docker build -t myapp:latest .
docker build -t myapp:1.0.0 .

# Run
docker run -d -p 8080:8080 --name myapp myapp:latest
docker run -d -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod myapp:latest
docker run -d -p 8080:8080 -v /data:/app/data myapp:latest

# Manage
docker ps                # Running containers
docker ps -a             # All containers
docker logs myapp        # See logs
docker logs -f myapp     # Follow logs
docker stop myapp        # Stop
docker start myapp       # Start stopped
docker rm myapp          # Remove container
docker exec -it myapp sh # Shell into container

# Images
docker images            # List images
docker rmi myapp:latest  # Remove image
docker system prune      # Clean unused

# Docker Hub
docker login
docker tag myapp username/myapp:latest
docker push username/myapp:latest
docker pull username/myapp:latest
```

## Docker Compose — Multi-Container Apps

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
      - SPRING_REDIS_HOST=redis
    depends_on:
      - db
      - redis
    networks:
      - app-network

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: secret
    volumes:
      - postgres-data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - app-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - app-network

volumes:
  postgres-data:

networks:
  app-network:
```

### Docker Compose Commands

```bash
docker compose up            # Start all services
docker compose up -d         # Start in background
docker compose down          # Stop and remove
docker compose down -v       # Stop, remove, delete volumes
docker compose logs -f       # Follow all logs
docker compose logs app      # Logs for specific service
docker compose ps            # List services
docker compose build         # Rebuild images
docker compose restart app   # Restart specific service
```

## Dockerfile Best Practices

### Layer Caching

```dockerfile
# BAD — re-downloads dependencies on every source change
FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY . .                          # Changes = invalidates everything
RUN mvn package

# GOOD — leverage Docker layer caching
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .                    # Only pom.xml -> cached unless pom changes
RUN mvn dependency:go-offline     # Download deps (cached layer)
COPY src ./src                    # Source changes only invalidate here
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Sort `COPY` instructions from least to most frequently changing. Use `--link` for independent layer caching:

```dockerfile
COPY --link target/*.jar app.jar   # New layer doesn't invalidate ancestors
```

### .dockerignore

```dockerignore
# .dockerignore — exclude everything not needed for the build
.git
.gitignore
target/
*.log
.env
.idea/
*.iml
node_modules/
.DS_Store
docker-compose*.yml
README.md
```

### Multi-Stage Build Patterns

```dockerfile
# Stage 1: Compile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Package optimization (Spring Boot layered JAR)
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# Stage 3: Final runtime image
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./
USER appuser
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

## Docker Networking

```bash
# List all networks
docker network ls

# Bridge network (default) — isolated on one host
docker run -d --network bridge --name app1 myapp:latest
docker run -d --network bridge --name app2 myapp:latest
# Containers on default bridge communicate via IP (not name)

# Custom bridge network — DNS resolution by name
docker network create my-network
docker run -d --network my-network --name app myapp:latest
docker run -d --network my-network --name db postgres:16-alpine
# app can reach db by hostname "db"

# Host network — shares host network stack (no isolation)
docker run -d --network host --name myapp myapp:latest
# Directly on host IP, no port mapping needed (but no isolation)

# Overlay network — for multi-host (Swarm)
docker network create -d overlay --attachable my-overlay
docker service create --network my-overlay --name myapp myapp:latest
```

### Network Configuration

```bash
# Expose ports
docker run -p 8080:8080 myapp              # Map host:container
docker run -p 127.0.0.1:8080:8080 myapp    # Bind to specific interface
docker run -p 8080:8080/udp myapp           # UDP port
docker run -p 8080-8090:8080-8090 myapp    # Port range

# Network aliases
docker run --network my-network \
  --network-alias api-service myapp:latest

# Disconnect/connect containers from networks
docker network disconnect my-network myapp
docker network connect my-network myapp
docker network inspect my-network          # See all connected containers
```

## Docker Volumes and Bind Mounts

```bash
# Named volumes — managed by Docker
docker volume create postgres-data          # Create named volume
docker run -v postgres-data:/var/lib/postgresql/data postgres:16
docker volume ls                            # List volumes
docker volume inspect postgres-data         # See mount point on host
docker volume rm postgres-data              # Remove volume
docker volume prune                         # Remove unused volumes

# Bind mounts — host directory mapped into container
docker run -v /home/user/config:/app/config myapp:latest
docker run -v $(pwd):/app -w /app myapp:latest  # Dev hot-reload

# tmpfs mounts — in-memory, for sensitive data
docker run --tmpfs /tmp:noexec,nosuid,size=64m myapp:latest
docker run --mount type=tmpfs,destination=/app/cache,tmpfs-size=1g myapp:latest

# Back up a volume
docker run --rm -v postgres-data:/data -v $(pwd):/backup alpine \
  tar czf /backup/postgres-data.tar.gz -C /data .
```

## Docker Compose Extended Example

```yaml
# docker-compose.yml — app + db + cache + queue + monitoring
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
      args:
        - JAR_VERSION=1.0.0
    image: myapp:1.0.0
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/mydb
      - SPRING_REDIS_HOST=redis
      - SPRING_RABBITMQ_HOST=rabbitmq
    depends_on:
      db:
        condition: service_healthy
      redis:
        condition: service_started
      rabbitmq:
        condition: service_healthy
    volumes:
      - app-logs:/app/logs
    networks:
      - backend
      - monitoring
    deploy:
      replicas: 2
      resources:
        limits:
          cpus: '1.0'
          memory: 1G
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3

  db:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: mydb
      POSTGRES_USER: myuser
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres-data:/var/lib/postgresql/data
      - ./init-db:/docker-entrypoint-initdb.d
    ports:
      - "5432:5432"
    networks:
      - backend
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U myuser"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    volumes:
      - redis-data:/data
    ports:
      - "6379:6379"
    networks:
      - backend
    command: redis-server --appendonly yes --requirepass ${REDIS_PASSWORD}
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s

  rabbitmq:
    image: rabbitmq:3-management-alpine
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: ${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    ports:
      - "5672:5672"   # AMQP
      - "15672:15672" # Management UI
    networks:
      - backend
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_port_connectivity"]
      interval: 30s
      timeout: 10s

  prometheus:
    image: prom/prometheus
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml
    ports:
      - "9090:9090"
    networks:
      - monitoring

  grafana:
    image: grafana/grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    ports:
      - "3000:3000"
    networks:
      - monitoring

volumes:
  postgres-data:
  redis-data:
  rabbitmq-data:
  app-logs:
  grafana-data:

networks:
  backend:
  monitoring:
```

## Docker Maintenance and Debugging

```bash
# Cleanup
docker system df                            # Show disk usage
docker system prune                         # Remove all unused: containers, networks, images (not volumes)
docker system prune -a --volumes            # Remove EVERYTHING unused
docker container prune                      # Remove stopped containers
docker image prune -a                       # Remove unused images
docker volume prune                         # Remove unused volumes
docker network prune                        # Remove unused networks

# Debugging with docker exec and inspect
docker exec -it myapp sh                    # Interactive shell
docker exec myapp cat /app/config/properties # Run single command
docker exec myapp env                        # Environment variables
docker exec myapp ps aux                     # Processes inside container
docker inspect myapp                         # Full container metadata
docker inspect myapp | jq '.[].NetworkSettings.IPAddress'  # Get IP
docker inspect myapp | jq '.[].Mounts'       # Mounted volumes
docker inspect myapp | jq '.[].Config.Env'   # Environment variables
docker logs myapp --tail 100                 # Last 100 lines
docker logs myapp --since 2026-05-12T10:00:00Z   # Logs since timestamp
docker logs myapp -t                         # Add timestamps
docker events --filter 'container=myapp'     # Stream container events

# Resource usage
docker stats                                 # Live resource stats (all containers)
docker stats myapp                           # Single container
docker stats --no-stream                     # One-shot snapshot
docker top myapp                             # Processes inside container
docker port myapp                            # Port mappings

# Dive — image layer inspection (install first)
dive myapp:latest                            # Analyze image layers

# BuildKit improvements
DOCKER_BUILDKIT=1 docker build -t myapp .    # Enable BuildKit
docker build --cache-from myapp:cache .      # Use external cache

```

## Container Security

```dockerfile
# Secure Dockerfile for production

FROM eclipse-temurin:21-jre-alpine

# Create non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Set secure umask
RUN umask 027

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Drop all capabilities, run as non-root
USER appuser:appgroup

# Make filesystem read-only except for writable directories
# (at runtime with --read-only --tmpfs /tmp --tmpfs /app/logs)

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
```

```bash
# Run container with security hardening
docker run -d \
  --name myapp \
  --read-only \                              # Read-only root filesystem
  --tmpfs /tmp:noexec,nosuid,size=64m \      # Writable temp (no exec)
  --tmpfs /app/logs:noexec,nosuid,size=256m \ # Writable logs
  --security-opt=no-new-privileges:true \     # Prevent privilege escalation
  --security-opt=seccomp=seccomp-profile.json \  # Restrict syscalls
  --cap-drop=ALL \                           # Drop all capabilities
  --cap-add=NET_BIND_SERVICE \               # Only allow binding to port
  --memory="512m" \                          # Memory limit
  --cpus="0.5" \                             # CPU limit
  --pids-limit=100 \                         # Prevent fork bomb
  --restart=on-failure:5 \
  myapp:latest
```

```json
{
  "defaultAction": "SCMP_ACT_ERRNO",
  "architectures": ["SCMP_ARCH_X86_64"],
  "syscalls": [
    {"names": ["accept", "bind", "close", "connect", "dup", "exit",
               "exit_group", "fstat", "getsockname", "listen", "lseek",
               "mmap", "mprotect", "munmap", "open", "openat", "read",
               "recvfrom", "sendto", "setsockopt", "shutdown", "socket",
               "stat", "write", "writev", "futex", "nanosleep",
               "clock_gettime", "getrandom", "brk"], "action": "SCMP_ACT_ALLOW"}
  ]
}
```

Run with Docker Scout scanning:
```bash
docker scout quickview myapp:latest          # Vulnerability overview
docker scout cves myapp:latest               # Full CVE report
docker scout recommendations myapp:latest    # Remediation suggestions
```

---

1. Write a Dockerfile for a Spring Boot app. Build and run it.
2. Create a docker-compose.yml with app + PostgreSQL + Redis.
3. Use `docker compose logs` to debug a connection issue.
4. Push your image to Docker Hub.
5. Add a health check to your Dockerfile.
