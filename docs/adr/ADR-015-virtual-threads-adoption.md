# ADR-015: Virtual Threads Adoption

## Status

Accepted

## Context

Java 21 introduces Virtual Threads (Project Loom) as a first-class concurrency mechanism. The course must teach concurrent programming but traditional thread-per-request models and complex thread pools create educational overhead.

## Decision

Adopt **Virtual Threads** as the default concurrency model for I/O-bound workloads. Specific guidelines:

**Use Virtual Threads for:**
- REST API request handling (Tomcat/Jetty with `spring.threads.virtual.enabled=true`)
- Database calls via JDBC/JPA
- HTTP client calls (RestClient, WebClient)
- File I/O operations
- Message consumption (Kafka, RabbitMQ listeners)
- Any task that blocks on I/O

**Do NOT use Virtual Threads for:**
- CPU-bound computations (parallel streams, ForkJoinPool preferred)
- Pinned thread scenarios:
  - `synchronized` blocks/methods (use `ReentrantLock` instead)
  - `native` methods or JNI calls
  - Blocking in `synchronized` blocks inside libraries
- Thread-per-core architectures (use platform threads)

**Configuration:**
```yaml
spring:
  threads:
    virtual:
      enabled: true
```

**Teaching Approach:**
- Foundation modules: traditional `Thread`, `Runnable`, `Callable`, `ExecutorService`
- Intermediate: virtual threads as drop-in replacement, structured concurrency
- Advanced: when to choose platform vs. virtual threads, pinning analysis
- Elite: Loom internals, carrier threads, fork-join pool management

## Consequences

**Pros:**
- Dramatically simplifies concurrent programming education
- Eliminates thread pool sizing complexity
- Millions of virtual threads with minimal memory overhead
- Drop-in replacement for most platform thread usage
- Structured concurrency (`StructuredTaskScope`) enables safer patterns

**Cons:**
- Pinning issues with `synchronized` require teaching alternative locking
- Third-party libraries not yet updated for virtual threads
- Pooled resources (database connections) still limited; thread-per-request creates connection pressure
- Debugging and profiling tools still maturing for virtual thread support
