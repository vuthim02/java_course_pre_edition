# ADR-003: Spring Boot 3.x for Enterprise Projects

## Status

Accepted

## Context

The course's intermediate, advanced, and elite modules require a web framework that supports modern Java features, reactive programming, cloud-native deployment, and enterprise patterns. Spring Boot is the dominant framework in the Java ecosystem.

## Decision

Use Spring Boot 3.2+ for all enterprise-oriented modules. Specific commitments:

- **Spring Boot 3.2+** as the base version (compatible with Java 21)
- **Jakarta EE 10** namespace (javax → jakarta migration complete)
- **Spring Web MVC** for traditional REST APIs
- **Spring WebFlux** for reactive streams in real-time and event-driven modules
- **Spring Security 6.x** for authentication and authorization
- **Spring Data JPA / R2DBC** for database access
- **Spring Cloud** for microservices patterns (discovery, config, gateway)
- **GraalVM native-image** support for deployment modules

## Consequences

**Pros:**
- Industry-standard framework with vast ecosystem and community support
- Auto-configuration reduces boilerplate in educational examples
- Reactive stack (WebFlux, R2DBC) available for advanced concurrent patterns
- Native-image support enables teaching cloud-native Java

**Cons:**
- Framework abstraction can obscure underlying Java concepts for beginners
- Rapid release cycle means course content requires periodic updates
- Auto-configuration magic can confuse students learning DI from first principles
