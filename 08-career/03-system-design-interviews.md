# Career — Lesson 3: System Design Interviews

## What System Design Tests

System design interviews assess your ability to **build large-scale systems**. Unlike coding interviews (algorithms), system design is open-ended — there's no single right answer.

```
Interviewer: "Design a URL shortener like TinyURL."

They want to see:
✅ Do you ask clarifying questions?
✅ Can you break down a complex problem?
✅ Do you understand trade-offs?
✅ Can you estimate scale?
✅ Do you consider failure modes?
✅ Can you communicate clearly?
```

## The Framework

### 1. Requirements Clarification (5 min)

Always start by asking questions:

```markdown
**Functional Requirements:**
- What features? (create short URL, redirect, analytics?)
- Custom aliases? Expiration? User accounts?

**Non-Functional Requirements:**
- How many requests per day? (read/write ratio?)
- Latency requirements? (P99 < 100ms?)
- Durability? (can we lose data?)
- Consistency? (strong vs eventual?)
```

### 2. Capacity Estimation (5 min)

```markdown
Example: URL Shortener

Traffic: 100M new URLs/month = ~40/s writes
        100:1 read/write ratio = ~4000/s reads

Storage: 100M × 500 bytes = 50GB/month
        5 years = 3TB total

Bandwidth: 4000 reads × 500 bytes = 2MB/s outbound

Memory (cache): 80% cache hit rate → 4000 × 20% × 500 bytes
                = 400KB/s read from DB
```

### 3. API Design (5 min)

```java
// RESTful API
POST /api/v1/shorten
{ "url": "https://example.com/very-long-url", "custom_alias": "my-link" }
→ { "short_url": "https://short.ly/abc123", "expires_at": "..." }

GET /{shortCode}
→ 302 Redirect to original URL

GET /api/v1/analytics/{shortCode}
→ { "clicks": 1500, "top_countries": [...], "daily": [...] }
```

### 4. Data Model (5 min)

```sql
CREATE TABLE urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) UNIQUE NOT NULL,
    original_url TEXT NOT NULL,
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT NOW(),
    expires_at TIMESTAMP,
    INDEX idx_short_code (short_code)
);

CREATE TABLE clicks (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(10) NOT NULL,
    clicked_at TIMESTAMP DEFAULT NOW(),
    ip_address VARCHAR(45),
    user_agent TEXT,
    country VARCHAR(2),
    INDEX idx_short_code_time (short_code, clicked_at)
);
```

### 5. High-Level Design (10 min)

```
┌──────────┐     ┌──────────┐     ┌──────────┐
│ Client   │────▶│ Load     │────▶│ Web      │
│          │     │ Balancer │     │ Servers  │
└──────────┘     └──────────┘     └────┬─────┘
                                        │
                           ┌────────────┼────────────┐
                           ▼            ▼            ▼
                     ┌──────────┐ ┌──────────┐ ┌──────────┐
                     │ Cache    │ │ DB       │ │ Analytics│
                     │ (Redis)  │ │(Postgres)│ │ (Kafka + │
                     └──────────┘ └──────────┘ │  Spark)  │
                                                └──────────┘
```

### 6. Deep Dive (10 min)

Pick the most interesting component and go deep:

```markdown
**Short Code Generation:**

Option A: Hash + Base62 encode
  MD5("url") → 128 bits → Base62 → 6 chars
  Problem: collisions

Option B: Distributed ID + Base62
  Use Snowflake ID → Base62 → 6-7 chars
  No collisions, ordered, distributed

Option C: Pre-generate codes
  Batch of codes ready in DB
  Assign on request — fast, no computation

**Key Decision: Pre-generate codes (C) for lowest latency**
```

### 7. Scalability & Trade-offs (5 min)

```markdown
**Scaling Reads:**
- Add read replicas (PostgreSQL)
- Cache popular URLs in Redis (LRU eviction)
- CDN for geo-distribution (CloudFront)

**Scaling Writes:**
- Shard by short_code hash
- Each shard is independent
- Use consistent hashing for rebalancing

**Trade-offs:**
- Strong consistency vs availability (eventual for analytics)
- SQL vs NoSQL (SQL for relationships, NoSQL for click data)
- Monolith vs microservices (start monolith, split if needed)

**Failure Modes:**
- Cache failure → DB handles load (circuit breaker)
- DB failure → Read replicas promote; writes queued
- Whole region failure → DNS failover to second region
```

## Practice Problems

| Problem | Key Concepts | Difficulty |
|---------|-------------|------------|
| **URL Shortener** | Hashing, caching, distributed IDs | Medium |
| **Chat System** | WebSockets, presence, message ordering | Medium |
| **Design Uber** | Location tracking, matching, ETA | Hard |
| **Design Twitter** | Timeline, feed, fan-out | Hard |
| **Design YouTube** | Video upload, transcoding, streaming | Hard |
| **Design WhatsApp** | End-to-end encryption, group chat | Hard |
| **Design Rate Limiter** | Token bucket, sliding window | Medium |
| **Design Search Autocomplete** | Trie, top-K, precomputation | Medium |

## Java-Specific System Design Tips

```java
// 1. Know your concurrency primitives
ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
CompletableFuture.supplyAsync(() -> fetchFromDB(key))
    .orTimeout(500, MILLISECONDS);

// 2. Know JVM tuning
// "-Xms4g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=50"

// 3. Know your frameworks
// Spring Boot → REST APIs
// Spring Cloud → Service discovery, circuit breakers
// WebFlux → High concurrency, non-blocking
// Kafka → Event streaming, decoupling
```

## Exercises

1. Design a URL shortener. Walk through all 7 steps.
2. Design a real-time chat system. Focus on presence detection and message ordering.
3. Design a rate limiter. Compare token bucket vs sliding window.
4. Design a news feed system. Compare push vs pull for timeline generation.
5. Practice explaining trade-offs out loud — record yourself and review.
