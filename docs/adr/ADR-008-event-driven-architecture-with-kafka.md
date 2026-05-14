# ADR-008: Event-Driven Architecture with Kafka

## Status

Accepted

## Context

Advanced modules require asynchronous communication, event-driven patterns, and stream processing. Multiple messaging systems exist (RabbitMQ, Kafka, Pulsar, AWS SQS/SNS).

## Decision

Use **Apache Kafka** as the primary event streaming platform for all event-driven course modules. Specific applications:

- **Order events** in e-commerce (order.created, payment.received, shipment.shipped)
- **Activity feed** in social media (post.created, like.added, friend.requested)
- **Trade execution** in trading platform (order.placed, trade.executed, portfolio.updated)
- **Notification delivery** across multiple modules
- **CDC (Change Data Capture)** via Debezium for database event streaming

Supporting patterns taught:
- **Outbox pattern** with transactional outbox table to guarantee at-least-once delivery
- **Idempotent consumers** for exactly-once semantics
- **Event sourcing** with Kafka as event store (with EventStoreDB as alternative in Elite module)
- **Kafka Streams** for stateful stream processing

## Consequences

**Pros:**
- Kafka's log-based architecture provides durability and replayability
- Strong ecosystem (Kafka Connect, Streams, Schema Registry)
- Excellent for teaching distributed systems concepts
- Real-world relevance (used by Netflix, Uber, LinkedIn, etc.)

**Cons:**
- Operational complexity — requires Zookeeper/KRaft, brokers, Schema Registry
- Limited to course infrastructure (Docker Compose); not cloud-managed
- Steeper learning curve than simpler message brokers
