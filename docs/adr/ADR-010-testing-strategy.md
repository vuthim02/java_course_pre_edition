# ADR-010: Testing Strategy

## Status

Accepted

## Context

The course requires a comprehensive testing approach that scales from unit tests in foundational modules to end-to-end tests in enterprise projects. A defined testing strategy ensures consistency and teaches industry best practices.

## Decision

Adopt the **testing pyramid** (unit → integration → e2e) with specific framework selections:

**Unit Tests:**
- **JUnit 5** (Jupiter) as the test framework
- **Mockito** for mocking dependencies
- **AssertJ** for fluent assertions
- Target: ~70% code coverage
- Test service layer in isolation, controller logic, domain models

**Integration Tests:**
- **Spring Boot `@WebMvcTest`** for controller-layer tests
- **Spring Boot `@DataJpaTest`** for repository tests
- **Testcontainers** for PostgreSQL/Kafka/Redis integration tests (not H2)
- **`@SpringBootTest`** for full context loading
- Flyway migrations run automatically in test setup

**End-to-End Tests:**
- **REST Assured** or **WebTestClient** for API-level E2E tests
- **Cypress** or **Playwright** for UI (if applicable)
- **Postman/Newman** collections for API contract verification
- Docker Compose-based test environment with Testcontainers

**Property-Based Testing:**
- **jqwik** for property-based testing in advanced modules

## Consequences

**Pros:**
- Industry-standard tooling students will encounter in the workplace
- Testcontainers provides realistic database testing without mocking
- Clear progression (simple → complex) matches module difficulty curve
- CI integration demonstrated with GitHub Actions

**Cons:**
- Testcontainers requires Docker, adding setup overhead
- Full Spring context tests are slow; must be carefully scoped
- E2E test maintenance increases with project complexity
