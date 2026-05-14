# ADR-001: Java 21 as Target Version

## Status

Accepted

## Context

The course must target a single Java version that balances modern language features with ecosystem stability and student career relevance. Multiple Java versions exist (17 LTS, 21 LTS, 22+), each offering different feature sets. The course covers both foundational and advanced enterprise topics, requiring a version that supports modern paradigms without sacrificing stability.

## Decision

Adopt Java 21 (LTS) as the exclusive target version across all course modules, projects, and examples. Key features leveraged:

- **Virtual Threads (Project Loom)**: For I/O-bound concurrent workloads in chat, trading, and API gateway projects
- **Records**: For DTOs, API responses, and value objects throughout all layers
- **Pattern Matching (switch expressions, instanceof)**: For cleaner control flow in domain logic
- **Sealed Classes**: For restricted type hierarchies in domain modeling
- **Text Blocks**: For SQL, JSON, and YAML strings in test fixtures and examples
- **Sequenced Collections**: For ordered collection operations
- **String Templates (preview)**: For safer string interpolation in logging and query building

## Consequences

**Pros:**
- Students learn the latest LTS standard relevant for 2024–2030 enterprise jobs
- Virtual threads simplify concurrent programming education vs. complex thread pools
- Records eliminate boilerplate, keeping examples concise
- Pattern matching promotes functional-style Java that aligns with modern paradigms

**Cons:**
- Some legacy enterprise environments still use Java 11/17; students may need adaptation
- String Templates remain in preview; course materials must note this
- Build tool and framework versions must be carefully selected for Java 21 compatibility
