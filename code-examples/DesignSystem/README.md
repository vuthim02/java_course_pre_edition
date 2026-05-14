# Design System — Architecture Decision Records & Patterns

## Table of Contents
1. [Architecture Decision Records](#1-architecture-decision-records)
2. [System Design Templates](#2-system-design-templates)
3. [API Design Standards](#3-api-design-standards)
4. [Error Handling Patterns](#4-error-handling-patterns)
5. [Logging & Observability Standards](#5-logging--observability-standards)
6. [Security Design Patterns](#6-security-design-patterns)
7. [Database Design Patterns](#7-database-design-patterns)
8. [Caching Strategy](#8-caching-strategy)

---

## 1. Architecture Decision Records

### ADR Template

```markdown
# ADR-NNN: Title of Decision

**Status:** [Proposed | Accepted | Deprecated | Superseded]
**Date:** YYYY-MM-DD
**Deciders:** [Names]

## Context
What is the issue motivating this decision?

## Decision
What is the change being proposed?

## Consequences
What becomes easier/harder?

## Alternatives Considered
- Alternative A: Pros/Cons
- Alternative B: Pros/Cons

## References
- Link to relevant docs
```

### ADR-001: Use Spring Boot 3.2 with Java 21

```markdown
**Status:** Accepted
**Date:** 2024-01-15

## Context
Choosing the base framework and Java version for all microservices.

## Decision
- Java 21 LTS with Virtual Threads (Project Loom)
- Spring Boot 3.2 with Spring Framework 6.1
- Spring Cloud 2023.0.0

## Consequences
- Virtual threads simplify concurrent programming
- Records, pattern matching, sealed classes improve code quality
- Spring Boot 3.2+ deprecates older Spring APIs

## Alternatives
- Java 17 LTS: misses virtual threads and latest language features
- Quarkus: faster startup but smaller ecosystem
- Micronaut: AOT compilation but fewer integrations
```

### ADR-002: Database per Service (Database Isolation)

```markdown
**Status:** Accepted
**Date:** 2024-02-01

## Context
Microservices need data isolation to avoid coupling.

## Decision
- Each service owns its database schema
- No direct cross-service database access
- Inter-service communication via APIs/events only
- Shared data via eventual consistency (Kafka)

## Consequences
- Full service autonomy and independent scaling
- Data duplication across services is acceptable
- Requires saga pattern for distributed transactions

## Alternatives
- Shared database: simpler but couples services
- Schema-per-service: compromise but still shared DB
```

### ADR-003: Async Communication via Kafka

```markdown
**Status:** Accepted
**Date:** 2024-02-15

## Context
Services need to communicate asynchronously for event-driven workflows.

## Decision
- Apache Kafka as event backbone
- Avro schemas with Schema Registry
- Event sourcing for critical workflows (orders, payments)
- Dead letter queues for failed events

## Consequences
- Strong durability with log-based storage
- Schema evolution via compatibility checks
- At-least-once delivery semantics

## Alternatives
- RabbitMQ: simpler but weaker durability guarantees
- Pulsar: newer but smaller ecosystem
```

---

## 2. System Design Templates

### REST API Design Template

```
┌──────────────────────────────────────────┐
│          API Design Checklist             │
├──────────────────────────────────────────┤
│ ☐ Resource-oriented URLs (nouns, not verbs)  │
│ ☐ Consistent pluralization (/users not /user) │
│ ☐ Versioning: /api/v1/                    │
│ ☐ Pagination: ?page=0&size=20             │
│ ☐ Sorting: ?sort=name,asc                 │
│ ☐ Filtering: ?status=active&createdAfter=  │
│ ☐ Field selection: ?fields=id,name,email  │
│ ☐ Proper HTTP methods (GET/POST/PUT/DELETE)│
│ ☐ Consistent error responses              │
│ ☐ HATEOAS links for discoverability       │
│ ☐ Idempotency keys for mutations          │
│ ☐ Rate limiting headers                   │
│ ☐ CORS configuration                      │
│ ☐ Content negotiation (Accept header)     │
│ ☐ OpenAPI/Swagger documentation           │
└──────────────────────────────────────────┘
```

### Error Response Format

```json
{
    "error": {
        "code": "VALIDATION_ERROR",
        "message": "Invalid request parameters",
        "details": [
            {
                "field": "email",
                "message": "must be a valid email address",
                "rejectedValue": "not-an-email"
            }
        ],
        "traceId": "abc-123-def-456",
        "timestamp": "2024-01-15T10:30:00Z"
    }
}
```

### Paginated Response Format

```json
{
    "data": [...],
    "page": {
        "number": 0,
        "size": 20,
        "totalElements": 156,
        "totalPages": 8,
        "first": true,
        "last": false
    }
}
```

---

## 3. API Design Standards

### Naming Conventions

| Resource | GET | POST | PUT | DELETE |
|----------|-----|------|-----|--------|
| /users | List users | Create user | Replace user | Delete user |
| /users/{id} | Get user | 405 | Update user | Delete user |
| /users/{id}/orders | List orders | Create order | 405 | 405 |
| /users/{id}/orders/{oid} | Get order | 405 | Update order | Cancel order |

### HTTP Status Code Usage

| Code | Usage |
|------|-------|
| 200 | Successful GET, PUT, PATCH |
| 201 | Successful POST (resource created) |
| 204 | Successful DELETE |
| 400 | Bad request (validation error) |
| 401 | Unauthenticated (missing/invalid token) |
| 403 | Forbidden (authenticated but not authorized) |
| 404 | Resource not found |
| 409 | Conflict (duplicate, version conflict) |
| 422 | Unprocessable entity (business logic error) |
| 429 | Rate limit exceeded |
| 500 | Internal server error |
| 502 | Bad gateway (upstream failure) |
| 503 | Service unavailable (circuit breaker open) |

---

## 4. Error Handling Patterns

### Layered Exception Strategy

```
Controller Layer
    ├── @ExceptionHandler methods
    ├── GlobalExceptionHandler (@ControllerAdvice)
    └── Maps exceptions → ProblemDetail (RFC 9457)

Service Layer
    ├── Domain-specific exceptions (extends RuntimeException)
    ├── Business rule validation
    └── Transaction management

Repository Layer
    ├── DataAccessException → translated to domain exceptions
    └── OptimisticLockException → retry with backoff
```

### Exception Hierarchy

```java
// Base
abstract class AppException extends RuntimeException {
    abstract HttpStatus status();
    abstract String code();
}

// Specific
class ResourceNotFoundException extends AppException { ... }
class ValidationException extends AppException { ... }
class DuplicateResourceException extends AppException { ... }
class BusinessRuleViolation extends AppException { ... }
class UnauthorizedException extends AppException { ... }
class ForbiddenException extends AppException { ... }
```

---

## 5. Logging & Observability Standards

### Structured Logging Format (JSON)

```json
{
    "@timestamp": "2024-01-15T10:30:00.123Z",
    "level": "INFO",
    "logger": "com.example.service.OrderService",
    "message": "Order created successfully",
    "traceId": "abc123def456",
    "spanId": "789ghi",
    "service": "order-service",
    "environment": "production",
    "correlationId": "cust-123-order-456",
    "userId": "user-789",
    "duration": 245,
    "metadata": {
        "orderId": "ord-456",
        "total": 99.99,
        "items": 3
    }
}
```

### Log Levels by Environment

| Level | Development | Staging | Production |
|-------|-------------|---------|------------|
| TRACE | Enabled | Disabled | Disabled |
| DEBUG | Enabled | On request | Disabled |
| INFO | Enabled | Enabled | Enabled |
| WARN | Enabled | Enabled | Enabled |
| ERROR | Enabled | Enabled | Enabled |

### Health Check Endpoints

| Endpoint | Purpose | Dependencies Checked |
|----------|---------|---------------------|
| /actuator/health/liveness | Is the app alive? | None (JVM state) |
| /actuator/health/readiness | Can it serve traffic? | DB, Redis, Kafka |
| /actuator/info | Build info, git commit | None |
| /actuator/metrics | Performance metrics | None |
| /actuator/prometheus | Prometheus scrape | None |

---

## 6. Security Design Patterns

### Authentication Flow

```
┌────────┐     ┌──────────┐     ┌──────────┐
│ Client │────▶│ Gateway  │────▶│ Auth     │
│        │     │          │     │ Service  │
└────────┘     └──────────┘     └──────────┘
     │               │               │
     │ 1. POST /login│               │
     │    with creds │               │
     │               │ 2. Validate   │
     │               │    credentials│
     │               │ 3. Return JWT │
     │ 4. JWT token  │               │
     │               │               │
     │ 5. GET /res   │               │
     │    Bearer JWT │               │
     │               │ 6. Validate   │
     │               │    JWT        │
     │               │ 7. Extract    │
     │               │    user info  │
     │               │ 8. Forward to │
     │               │    service    │
     │ 9. Response   │               │
```

### Security Checklist

```
☐ All external endpoints require authentication
☐ JWT tokens signed with RS256 (asymmetric)
☐ Short-lived access tokens (15 min)
☐ Long-lived refresh tokens (7 days) with rotation
☐ Rate limiting per user/IP/tenant
☐ CORS configured per environment
☐ SQL injection prevention (parameterized queries)
☐ XSS prevention (output encoding)
☐ CSRF protection (state-changing endpoints)
☐ HTTPS enforced (HSTS headers)
☐ Secrets managed via Vault/env, not in code
☐ Audit logging for all data mutations
☐ Principle of least privilege (RBAC)
☐ Input validation on all endpoints
☐ Sensitive data masked in logs (PII/PCI)
```

---

## 7. Database Design Patterns

### Naming Conventions

```
Tables:     plural snake_case  → users, order_items
Columns:    singular snake_case → first_name, created_at
Indexes:    idx_{table}_{column} → idx_users_email
Unique:     uq_{table}_{column} → uq_users_email
Foreign:    fk_{child}_{parent} → fk_order_items_orders
Primary:    pk_{table}          → pk_users
Sequences:  seq_{table}_id     → seq_users_id
```

### Index Strategy

| Index Type | When to Use |
|------------|-------------|
| B-tree | Default, equality + range queries |
| Hash | Exact match lookups (UUID, email) |
| GiST | Full-text search, geo queries |
| GIN | Array columns, JSONB, full-text |
| BRIN | Large tables with natural ordering (logs, time series) |
| Partial | WHERE clause subset (`WHERE status = 'active'`) |
| Covering | INCLUDE for index-only scans |
| Composite | Multi-column queries (order of columns matters) |

### Connection Pool Sizing

```
Pool Size = T * (C - 1) + 1

Where:
  T = number of threads handling requests
  C = number of database calls per request

Example:
  10 threads × 3 queries = 30 connections (with 5 spare)

Rule of thumb: Start with CoreCount × 2 and adjust
```

---

## 8. Caching Strategy

### Multi-Layer Cache

```
┌──────────────────────────────────┐
│         Client Browser            │
│   Cache: Browser Cache (Cache-    │
│          Control headers)         │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│         CDN (CloudFront/Cloudflare)│
│   Cache: Static assets, API       │
│          responses (short TTL)    │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│         API Gateway               │
│   Cache: GET responses, JWT       │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│         Application Layer         │
│   Cache: Caffeine (local)         │
│          Redis (distributed)      │
└──────────────┬───────────────────┘
               │
┌──────────────▼───────────────────┐
│         Database Layer            │
│   Cache: Shared buffers,          │
│          Query cache              │
└──────────────────────────────────┘
```

### Cache Invalidation Patterns

| Pattern | Description | Use Case |
|---------|-------------|----------|
| TTL | Expire after time | Read-heavy, stale data acceptable |
| Write-Through | Update cache on write | Strong consistency needed |
| Write-Behind | Async cache update | High write throughput |
| Cache-Aside | App loads on miss | General purpose |
| Read-Through | DB loads on miss | Lazy loading |
| Event-Driven | Invalidate via events | Microservices |
| MESI | Cache coherence protocol | Multi-CPU shared data |
| In-Memory + Redis | Local + distributed | Hybrid approach |

### Cache Key Design

```
Key Format: {prefix}:{entity}:{id}:{field}

Examples:
  user:profile:123
  product:456:details
  session:token:abc123
  rate_limit:user:789:endpoint

Prefix convention:
  - cache: → general cache
  - rate:  → rate limiting
  - sess:  → sessions
  - lock:  → distributed locks
```
