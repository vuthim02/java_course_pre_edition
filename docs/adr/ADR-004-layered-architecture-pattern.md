# ADR-004: Layered Architecture Pattern

## Status

Accepted

## Context

Course projects require a consistent architectural pattern that is teachable, scalable, and reflects real-world enterprise applications. Multiple architectural styles exist (ports-and-adapters/hexagonal, onion, clean architecture, CQRS).

## Decision

Adopt the three-layer architecture (Controller → Service → Repository) as the standard pattern across all course projects. Structure:

```
com.{project}.controller   → HTTP request handling, validation, response formatting
com.{project}.service      → Business logic, orchestration, transaction boundaries
com.{project}.repository   → Data access, query construction, persistence
com.{project}.dto          → Data transfer objects, request/response models
com.{project}.exception    → Domain and application exceptions
com.{project}.config       → Configuration, security, framework setup
```

Rationale:
- Most widely recognized and understood architecture in enterprise Java
- Clear separation of concerns enables testing at each layer independently
- Direct mapping to Spring's natural component stereotypes (@Controller, @Service, @Repository)
- Simplest architecture that effectively demonstrates DI, AOP, and transaction management
- Can evolve into hexagonal architecture or CQRS in advanced modules

## Consequences

**Pros:**
- Low cognitive load for students learning enterprise patterns
- Industry-typical interview topic (controller/service/repository questions)
- Easy to establish coding conventions and review standards
- Layers map naturally to test types (unit → service, integration → repository, e2e → controller)

**Cons:**
- Can lead to anemic domain models if business logic leaks into services
- Not ideal for complex domain logic (DDD requires richer patterns)
- Layer coupling can cause issues in very large projects
