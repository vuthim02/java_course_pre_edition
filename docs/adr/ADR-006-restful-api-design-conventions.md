# ADR-006: RESTful API Design Conventions

## Status

Accepted

## Context

Multiple course projects expose REST APIs. Without consistent conventions, students encounter conflicting patterns across modules, causing confusion and making the codebase harder to maintain.

## Decision

Adopt the following REST API conventions across all projects:

**URL Structure:**
- Use plural nouns: `/api/users`, `/api/products`, `/api/orders`
- Nest related resources: `/api/users/{id}/orders`
- Version via prefix: `/api/v1/...`
- Kebab-case for multi-word resources: `/api/order-items`

**HTTP Methods:**
- `GET` for retrieval (list and single)
- `POST` for creation
- `PUT` for full replacement
- `PATCH` for partial update
- `DELETE` for removal

**Pagination:**
- Page-based: `?page=0&size=20`
- Response includes: `content[]`, `totalElements`, `totalPages`, `page`, `size`
- Sort: `?sort=createdAt,desc&sort=name,asc`

**Error Response Envelope:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2025-01-01T00:00:00Z",
  "path": "/api/users",
  "details": ["email: must be a valid email address"]
}
```

**Hypermedia (advanced modules only):** HATEOAS links for discoverability.

## Consequences

**Pros:**
- Consistent API patterns reduce cognitive load when switching between projects
- Students internalize industry-standard REST conventions
- Simplifies client-side code and API documentation generation

**Cons:**
- Some resources may not fit cleanly into CRUD (e.g., search, analytics)
- Strict conventions can require verbose endpoints for complex operations
- HATEOAS adds complexity only warranted in advanced modules
