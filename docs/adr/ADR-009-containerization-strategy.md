# ADR-009: Containerization Strategy

## Status

Accepted

## Context

Course modules must be deployable in reproducible environments. Students need exposure to modern deployment practices without requiring cloud infrastructure. Multiple containerization options exist (Docker, Podman, Docker Compose, Kubernetes, Nomad).

## Decision

Use **Docker** for local development containerization and **Docker Compose** for multi-service orchestration in advanced modules. Introduce **Kubernetes (Minikube/K3s)** in the Elite and DevOps modules.

**Docker Strategy:**
- Multi-stage builds for optimized images (builder pattern for Maven)
- Distroless base images for production (Google distroless)
- Docker Compose for local development with PostgreSQL, Kafka, Redis
- `.dockerignore` files in each project

**Kubernetes Strategy:**
- Minikube for local K8s development
- K3s for lightweight production-like clusters
- Helm charts for package management
- Kubernetes manifests for stateless services, StatefulSets for databases
- Horizontal Pod Autoscaler (HPA) for scaling exercises

**Container Registry:** Docker Hub for public images, GitHub Container Registry (ghcr.io) for course-specific images.

## Consequences

**Pros:**
- Industry standard tooling directly applicable to real jobs
- Reproducible development environments across student machines
- Docker Compose enables complex multi-service setups without cloud costs
- K8s exposure prepares students for cloud-native roles

**Cons:**
- Resource-intensive for students with limited hardware
- Docker Desktop licensing changes require open-source alternatives (Rancher Desktop, Colima)
- K8s complexity can overwhelm students in earlier modules
