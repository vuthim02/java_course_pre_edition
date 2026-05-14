# ADR-005: PostgreSQL as Primary Database

## Status

Accepted

## Context

Course projects need a production-grade database that supports both relational and modern workloads (JSON, vector search). H2 is needed for lightweight testing. The choice must balance realism with educational usability.

## Decision

Use **PostgreSQL** as the primary database for all projects with **H2** in-memory database for unit/integration testing. Specific PostgreSQL features leveraged:

- **JSONB** columns for flexible schema in social media and SaaS projects
- **pgvector** extension for AI/embeddings in RAG-based projects
- **Full-text search** for e-commerce and social media search features
- **Window functions** for trading platform analytics
- **Partial indexes** for multi-tenant query performance
- **LISTEN/NOTIFY** for real-time chat notification alternative

Database configuration via Docker Compose (`docker-compose.yml`) for local development and Testcontainers for integration tests.

## Consequences

**Pros:**
- Production-grade ACID compliance for transaction-heavy modules (trading, e-commerce)
- pgvector enables AI course module without additional infrastructure
- JSONB support teaches hybrid relational-document patterns
- H2 compatibility mode with PostgreSQL reduces test complexity

**Cons:**
- Students must install Docker or configure PostgreSQL locally
- Some PostgreSQL-specific syntax requires adapter configurations
- pgvector extension adds deployment complexity for AI module
