# ADR-013: Security Architecture

## Status

Accepted

## Context

Course projects handle authentication, authorization, and data protection. A consistent security architecture is needed across all modules, from basic password handling in intermediate projects to OAuth2 and JWT in enterprise systems.

## Decision

Adopt the following security architecture:

**Authentication:**
- **JWT (JSON Web Tokens)** as the primary auth mechanism
- Access tokens (short-lived, 15 min) + Refresh tokens (long-lived, 7 days)
- Token stored in HTTP-only cookies for web clients, Authorization header for API clients
- Password hashing via **bcrypt** (Spring Security's `BCryptPasswordEncoder`)
- **OAuth2** with Spring Security for social login demonstrations

**Authorization:**
- **Role-Based Access Control (RBAC)** as the base model
- Roles: `USER`, `MODERATOR`, `ADMIN`, `SUPER_ADMIN`
- Method-level security via `@PreAuthorize`, `@PostAuthorize`, `@Secured`
- Permission-based authorization for fine-grained control in advanced modules

**OWASP Compliance:**
- CSRF protection (enabled for cookie-based auth, disabled for API-only)
- XSS prevention via Content-Security-Policy headers
- SQL injection prevention via parameterized queries (JPA/JDBC)
- Rate limiting via Bucket4j or Spring Cloud Gateway filters
- HTTPS enforcement in production configurations

**API Security:**
- API key validation for service-to-service communication
- Input validation at controller boundary (Bean Validation `@Valid`)
- Output sanitization to prevent data leakage

**Secret Management:**
- No secrets in code; environment variables for local dev
- Spring Cloud Config or Vault for centralized configuration
- `.env` files in `.gitignore`

## Consequences

**Pros:**
- Real-world security patterns applicable to any enterprise project
- JWT stateless authentication scales well for microservices
- Method-level security integrates naturally with layered architecture

**Cons:**
- JWT revocation complexity (requires blacklist or short expiry)
- RBAC can become unwieldy in complex domain models
- Security configuration is verbose and error-prone for beginners
