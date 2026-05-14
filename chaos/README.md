# Chaos Engineering Experiments for Java by BroCode

## What is Chaos Engineering?

Chaos Engineering is the discipline of experimenting on a system to build confidence in its capability to withstand turbulent conditions in production. By intentionally injecting failures, we uncover weaknesses before they manifest as user-facing outages.

## Why Chaos Engineering for Java Microservices?

Java microservices built with Spring Boot typically rely on:
- Service discovery (Eureka)
- Distributed configuration (Spring Cloud Config)
- API gateways (Spring Cloud Gateway)
- Circuit breakers (Resilience4j)
- Database connections (HikariCP)
- Message brokers (Kafka)

Each of these is a potential failure point. Chaos experiments validate that:

1. **Circuit breakers trip correctly** when downstream services fail
2. **Connection pools recover** after database outages
3. **HPA scales** under CPU/memory stress
4. **Service mesh reroutes** around failed instances
5. **Retry mechanisms** handle transient network failures

## How to Use with LitmusChaos

### Prerequisites

```bash
# Install LitmusChaos
kubectl apply -f https://litmuschaos.github.io/litmus/litmus-operator-v1.13.8.yaml

# Install chaos service account
kubectl apply -f https://litmuschaos.github.io/litmus/litmus-admin-rbac.yaml
```

### Running Experiments

```bash
# Pod delete - tests resilience to instance failure
kubectl apply -f chaos/pod-delete-experiment.yaml

# CPU stress - tests HPA scaling
kubectl apply -f chaos/cpu-stress-experiment.yaml

# Network delay - tests timeouts and retries
kubectl apply -f chaos/network-delay-experiment.yaml

# DB failure - tests circuit breakers
kubectl apply -f chaos/db-failure-experiment.yaml
```

### Monitoring

Watch experiment status:
```bash
kubectl get chaosengine -w
kubectl describe chaosengine pod-delete-microservices
```

## How to Use with Chaos Mesh

```bash
# Install Chaos Mesh
curl -sSL https://mirrors.chaos-mesh.org/latest/install.sh | bash

# Apply Chaos Mesh experiments
kubectl apply -f chaos/chaos-mesh/pod-chaos.yaml
kubectl apply -f chaos/chaos-mesh/network-chaos.yaml
kubectl apply -f chaos/chaos-mesh/stress-chaos.yaml
```

## Experiment Categories

| Experiment | Tool | What It Tests |
|---|---|---|
| Pod Delete | LitmusChaos / Chaos Mesh | Stateless resilience, HPA recovery |
| CPU Stress | LitmusChaos / Chaos Mesh | HPA autoscaling, resource limits |
| Network Delay | LitmusChaos / Chaos Mesh | Timeouts, retries, circuit breakers |
| DB Failure | LitmusChaos | Connection pooling, fallbacks |
