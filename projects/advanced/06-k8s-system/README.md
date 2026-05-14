# K8s System Demo

A Spring Boot 3.2+ application demonstrating Kubernetes deployment patterns including health probes, leader election, graceful shutdown, ConfigMap/Secret integration, and more.

## Architecture

```
                    ┌──────────────┐
                    │   Ingress    │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │   Service    │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────▼─────┐ ┌───▼────┐ ┌────▼─────┐
        │  Pod v1   │ │  Pod   │ │  Pod v2  │
        └───────────┘ └────────┘ └──────────┘
              │            │            │
        ┌─────▼────────────▼────────────▼─────┐
        │    ConfigMap / Secret / Database    │
        └─────────────────────────────────────┘
```

## K8s Patterns Demonstrated

| Pattern | Implementation |
|---|---|
| **Health Probes** | `/health/liveness`, `/health/readiness`, `/health/startup` + Actuator probes |
| **Leader Election** | Database-backed with `@Lock(PESSIMISTIC_WRITE)`, lease renewal via `@Scheduled` |
| **Graceful Shutdown** | Tomcat connector drain, readiness marked "not ready", configurable timeout |
| **ConfigMap Integration** | Reads config from env vars (`HOSTNAME`, `SPRING_APPLICATION_NAME`, etc.) |
| **Secret Integration** | Undefined env vars return "not found" — ready for K8s Secret injection |
| **Resource Management** | JVM container support (`-XX:+UseContainerSupport`) |

## Project Structure

```
k8s-system/
├── pom.xml
├── README.md
├── k8s/                          # Kubernetes manifests
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── hpa.yaml
│   ├── ingress.yaml
│   ├── secret.yaml
│   └── service.yaml
├── src/
│   ├── main/
│   │   ├── java/com/k8sdemo/
│   │   │   ├── K8sSystemApplication.java
│   │   │   ├── config/
│   │   │   │   └── ActuatorConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── HealthController.java
│   │   │   │   └── DemoController.java
│   │   │   ├── model/
│   │   │   │   └── LeaderLease.java
│   │   │   ├── repository/
│   │   │   │   └── LeaderLeaseRepository.java
│   │   │   └── service/
│   │   │       ├── GracefulShutdownService.java
│   │   │       └── LeaderElectionService.java
│   │   └── resources/
│   │       └── application.yml
```

## Endpoints

### Health Probes
| Endpoint | Purpose | Response |
|---|---|---|
| `GET /health/liveness` | Is the app alive? | `{"status":"alive"}` (200) |
| `GET /health/readiness` | Is the app ready for traffic? | `{"status":"ready"}` (200) or `{"status":"not ready","reason":"..."}` (503) |
| `GET /health/startup` | Has the app started? | `{"status":"started"}` (200) |

### Actuator Probes (used by K8s deployment.yaml)
| Endpoint | K8s Probe |
|---|---|
| `/actuator/health/liveness` | `livenessProbe` + `startupProbe` |
| `/actuator/health/readiness` | `readinessProbe` |

### Demo API
| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/info` | App name, version, pod name (from `HOSTNAME` env), IP |
| GET | `/api/config/{key}` | Read config by key (env var → system property → "not found") |
| GET | `/api/leader` | Current leader instance |
| POST | `/api/shutdown` | Initiate graceful shutdown simulation |

## Running Locally

```bash
# Build
mvn clean package -DskipTests

# Run
java -jar target/app.jar

# Test
curl http://localhost:8080/health/liveness
curl http://localhost:8080/health/readiness
curl http://localhost:8080/api/info
curl http://localhost:8080/api/leader
```

## Deploying to Kubernetes

```bash
# Apply manifests
kubectl apply -f k8s/

# Check status
kubectl get pods -n cloud-native
kubectl get svc -n cloud-native

# Port-forward to access
kubectl port-forward -n cloud-native service/cloud-native-app 8080:80
```

### Configuration with ConfigMap

The `k8s/configmap.yaml` exposes environment variables (`APP_MESSAGE`, `FEATURE_ENABLED`, `MAX_RETRIES`) that the app reads. Access them via:

```bash
curl http://localhost:8080/api/config/APP_MESSAGE
curl http://localhost:8080/api/config/HOSTNAME
```

### Secrets

Secrets are injected as environment variables from `k8s/secret.yaml`. The app reads them the same way:

```bash
curl http://localhost:8080/api/config/DB_USERNAME
curl http://localhost:8080/api/config/API_KEY
```

## How Leader Election Works

1. Each instance runs a `@Scheduled` task every 10 seconds
2. It acquires a `PESSIMISTIC_WRITE` lock on the `leader_leases` table row
3. If no lease exists, it creates one and becomes leader
4. If the lease is expired, it takes over
5. The current leader renews its lease (sets `expiresAt = now + 30s`)
6. `GET /api/leader` returns the current leader from the DB

## How Graceful Shutdown Works

1. `POST /api/shutdown` sets the `shuttingDown` flag
2. The readiness probe immediately returns 503 ("not ready")
3. The K8s Service stops routing traffic to this pod
4. After 1s, the JVM `System.exit(0)` triggers Spring's `ContextClosedEvent`
5. The Tomcat connector pauses and the thread pool drains (max 30s)
6. The JVM exits cleanly
