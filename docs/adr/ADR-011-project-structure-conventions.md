# ADR-011: Project Structure Conventions

## Status

Accepted

## Context

With multiple projects across beginner, intermediate, advanced, and elite levels, consistent project structure and package naming is essential for navigability and maintainability.

## Decision

Adopt the following organizational conventions:

**Package Naming:**
```
com.{instructor}.{project}.{layer}
com.brocode.ecommerce.controller
com.brocode.ecommerce.service
com.brocode.ecommerce.repository
com.brocode.ecommerce.dto
com.brocode.ecommerce.exception
com.brocode.ecommerce.config
com.brocode.ecommerce.entity
```

**Module Organization:**
```
projects/
├── beginner/        → Single-file exercises, simple classes
│   └── NN-topic/
│       ├── pom.xml
│       └── src/
├── intermediate/    → Spring Boot applications
│   └── NN-project/
│       ├── pom.xml
│       └── src/main/java/com/brocode/{project}/
└── advanced/        → Multi-module Maven projects
    └── NN-project/
        ├── pom.xml (parent)
        ├── common/
        ├── service/
        └── api/
```

**File Organization:**
- One class per file (except small records or utility classes)
- Resource files in `src/main/resources/`:
  - `application.yml` (not `.properties`)
  - `db/migration/` for Flyway migrations
  - `templates/` for Thymeleaf (if used)
- Static resources in `src/main/resources/static/`

**Naming Conventions:**
- Classes: PascalCase nouns (UserService, ProductRepository)
- Methods: camelCase verbs (findById, createOrder)
- Constants: UPPER_SNAKE_CASE (MAX_RETRY_COUNT)
- Packages: lowercase, no underscores

## Consequences

**Pros:**
- Predictable navigation across all course projects
- Students internalize Java package conventions
- Simplifies automated tooling (checkstyle, archunit enforcement)

**Cons:**
- Deep package hierarchies for complex projects
- Some projects may not fit the pattern cleanly
