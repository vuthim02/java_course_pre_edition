# ADR-014: CQRS/Event Sourcing Decision

## Status

Accepted

## Context

Course modules span simple CRUD operations (auth, basic e-commerce) to complex domains requiring audit trails, temporal queries, and event-driven workflows (trading, multi-tenant SaaS). A single architecture cannot serve all use cases optimally.

## Decision

Apply **CQRS (Command Query Responsibility Segregation)** and **Event Sourcing** selectively, only where justified by domain requirements:

**CQRS Applied:**
- **Trading platform**: Commands (place order, cancel order) are write-optimized; Queries (portfolio value, trade history) use separate read models
- **Social media feed**: Writes (create post) are immediate; Reads (newsfeed generation) use materialized views
- **Multi-tenant billing**: Commands (record usage) are high-throughput; Queries (invoice generation) use aggregated read models

**Event Sourcing Applied:**
- **Trading platform**: Order state derived from event stream (OrderPlaced, OrderFilled, OrderCancelled)
- **Multi-tenant SaaS**: Tenant configuration changes as event stream for audit compliance
- **E-commerce (advanced)**: Order lifecycle as event stream

**CRUD Retained (no CQRS/ES):**
- Auth system (simple user CRUD)
- Basic e-commerce (product catalog, categories)
- Real-time chat (message history is append-only, not event-sourced)
- REST API foundations

**Implementation:**
- **Axon Framework** for CQRS/ES in dedicated modules
- **Debezium + Kafka** for CDC-based CQRS without event sourcing
- **Outbox pattern** (transactional outbox table) for reliable event publication
- **EventStoreDB** as alternative event store for elite module

## Consequences

**Pros:**
- Right-sizing architecture to problem complexity
- Students learn when CQRS/ES is appropriate vs. over-engineering
- Audit trail and temporal queries come naturally from event streams
- Clear separation of read/write concerns improves testability

**Cons:**
- Higher complexity and operational overhead where applied
- Event schema evolution requires migration strategies
- Eventual consistency between command and query models
- Steep learning curve for event sourcing newcomers
