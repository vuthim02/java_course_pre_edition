# ADR-012: Observability Stack

## Status

Accepted

## Context

Enterprise modules require monitoring, alerting, and debugging capabilities. Observability must be taught as a first-class concern, not an afterthought. The three pillars — metrics, tracing, logging — need consistent tooling.

## Decision

Adopt the following observability stack across all enterprise modules:

**Metrics:**
- **Micrometer** as the metrics facade (vendor-neutral)
- **Prometheus** for metrics collection and storage
- **Grafana** for dashboards and visualization
- Pre-built Grafana dashboards for JVM, Spring Boot, and PostgreSQL
- Custom business metrics (order rate, trade volume, API latency)

**Distributed Tracing:**
- **OpenTelemetry** SDK for trace generation and context propagation
- **Jaeger** or **Grafana Tempo** for trace storage and visualization
- Trace sampling: head-based (probability sampling) for development, tail-based for production
- Baggage propagation for tenant context across service boundaries

**Logging:**
- **Structured JSON logging** via Logback with `logstash-logback-encoder`
- Correlation IDs propagated across service calls via MDC
- Log levels: ERROR/WARN for production concerns, DEBUG for development
- Centralized logging via Loki (Grafana Cloud or self-hosted)
- No sensitive data in logs (PII, credentials, tokens)

**Health Checks:**
- Spring Boot Actuator health endpoints
- Custom health indicators for downstream dependencies (database, Kafka, Redis)
- Readiness and liveness probes for Kubernetes

## Consequences

**Pros:**
- Industry-standard stack (Prometheus + Grafana is dominant in cloud-native)
- OpenTelemetry provides vendor-neutral tracing
- Structured logging enables log analysis and alerting
- Students gain marketable SRE/DevOps skills

**Cons:**
- Significant infrastructure overhead (Prometheus, Grafana, Loki, Tempo)
- Learning curve for dashboard creation and PromQL
- Resource consumption in development environments
