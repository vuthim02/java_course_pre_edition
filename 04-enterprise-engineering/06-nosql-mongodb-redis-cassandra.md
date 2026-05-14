# Enterprise Engineering — Lesson 6: NoSQL (MongoDB, Redis, Cassandra)

## Why NoSQL?

Relational databases are great for structured data with relationships. NoSQL excels where SQL struggles:

| Problem | SQL Solution | NoSQL Solution |
|---------|-------------|----------------|
| Flexible schema | ALTER TABLE (blocking) | Schema-less, just save it |
| High-volume writes | Hard to scale writes | Built for horizontal scaling |
| Hierarchical data | Complex joins | Nested documents (MongoDB) |
| Real-time analytics | Complex aggregations | Key-value lookups (Redis) |
| Time-series data | Partition pruning issues | Designed for时序 (Cassandra) |
| Full-text search | LIKE queries (slow) | Native text search |

## MongoDB — Document Database

### Why MongoDB?

```
SQL Tables:                          MongoDB Documents:
┌──────────────────┐                 ┌──────────────────────┐
│ users             │                 │ {                     │
│ id | name | ...   │                 │   _id: ObjectId(...), │
├──────────────────┤                 │   name: "Alice",      │
│ orders            │                 │   email: "a@b.com",   │
│ id | user_id | ...│                 │   orders: [           │
└──────────────────┘                 │     { total: 100 },   │
  JOINs needed everywhere            │     { total: 200 }    │
                                      │   ]                   │
                                      │ }                     │
                                      │  All in ONE document  │
```

### Spring Data MongoDB

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

### Document Mapping

```java
@Document(collection = "users")  // MongoDB collection
public class User {
    @Id
    private String id;

    @Field("full_name")
    private String name;

    private String email;

    private Address address;  // Embedded document

    private List<Order> orders;  // Embedded array of documents
}

// Embedded document — no @Id needed
public class Address {
    private String street;
    private String city;
    private String zipCode;
}
```

### Repository

```java
public interface UserRepository extends MongoRepository<User, String> {
    List<User> findByEmail(String email);
    List<User> findByAddressCity(String city);
    List<User> findByOrdersTotalGreaterThan(BigDecimal amount);
}
```

### Aggregation Pipeline

```java
@Autowired
private MongoTemplate mongoTemplate;

public List<OrderSummary> getOrderSummaries() {
    Aggregation agg = Aggregation.newAggregation(
        Aggregation.match(Criteria.where("status").is("COMPLETED")),
        Aggregation.group("customerId")
            .sum("total").as("totalSpent")
            .count().as("orderCount"),
        Aggregation.sort(Sort.by(Sort.Direction.DESC, "totalSpent")),
        Aggregation.limit(10)
    );

    return mongoTemplate.aggregate(agg, "orders", OrderSummary.class)
        .getMappedResults();
}
```

## Redis — In-Memory Cache & Store

### Why Redis?

```
┌──────────────────────────────────────────┐
│  REDIS — THE SWISS ARMY KNIFE OF DATA     │
│                                            │
│  ┌────────┐ ┌────────┐ ┌────────┐         │
│  │ Cache  │ │ Session│ │  Queue │         │
│  │        │ │ Store  │ │        │         │
│  └────────┘ └────────┘ └────────┘         │
│  ┌────────┐ ┌────────┐ ┌────────┐         │
│  │ Pub/Sub│ │ Leader-│ │  Rate  │         │
│  │        │ │ board  │ │ Limiter│         │
│  └────────┘ └────────┘ └────────┘         │
│  ALL IN-MEMORY — SUB-MILLISECOND RESPONSE  │
└──────────────────────────────────────────┘
```

### Spring Data Redis

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### RedisTemplate Usage

```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

public void cacheUser(User user) {
    redisTemplate.opsForValue().set("user:" + user.getId(), user, 1, TimeUnit.HOURS);
}

public Optional<User> getCachedUser(Long id) {
    User user = (User) redisTemplate.opsForValue().get("user:" + id);
    return Optional.ofNullable(user);
}
```

### Redis Data Types

```java
// String — simple key-value
redisTemplate.opsForValue().set("key", "value");

// List — queue / stack
redisTemplate.opsForList().leftPush("queue", "task1");
redisTemplate.opsForList().rightPop("queue");  // FIFO

// Set — unique items
redisTemplate.opsForSet().add("tags", "java", "spring", "redis");

// Sorted Set — leaderboards
redisTemplate.opsForZSet().add("leaderboard", "player1", 1500);

// Hash — object fields
redisTemplate.opsForHash().put("session:123", "userId", "456");
```

### Caching with @Cacheable

```java
@Cacheable(value = "users", key = "#id", unless = "#result == null")
public User getUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new UserNotFoundException(id));
}

@CacheEvict(value = "users", key = "#id")
public void deleteUser(Long id) {
    userRepository.deleteById(id);
}

@CachePut(value = "users", key = "#user.id")
public User updateUser(User user) {
    return userRepository.save(user);
}
```

## Cassandra — Wide-Column Store

### Why Cassandra?

Cassandra excels at **high-volume writes** across multiple data centers with **no single point of failure**.

```
┌──────────────────────────────────────────────┐
│         CASSANDRA RING (No Master)            │
│                                              │
│  ┌──────┐    ┌──────┐    ┌──────┐           │
│  │Node A├────┤Node B├────┤Node C│           │
│  └──┬───┘    └──┬───┘    └──┬───┘           │
│     │           │           │                │
│  ┌──▼───┐    ┌──▼───┐    ┌──▼───┐           │
│  │Node D├────┤Node E├────┤Node F│           │
│  └──────┘    └──────┘    └──────┘           │
│                                              │
│  Write to ANY node → replicated to N nodes    │
│  Read from ANY node → consistent eventually   │
│  Zero downtime when nodes fail                │
└──────────────────────────────────────────────┘
```

### Data Model (Different from SQL!)

```sql
-- Cassandra CQL (looks like SQL, behaves differently)
CREATE TABLE users_by_email (
    email TEXT PRIMARY KEY,
    user_id UUID,
    name TEXT,
    created_at TIMESTAMP
);

CREATE TABLE orders_by_user (
    user_id UUID,
    order_id UUID,
    total DECIMAL,
    status TEXT,
    created_at TIMESTAMP,
    PRIMARY KEY (user_id, created_at, order_id)
) WITH CLUSTERING ORDER BY (created_at DESC);
```

### Key Cassandra Rules

| Rule | Why |
|------|-----|
| **Query-first design** | Model tables for your query patterns, not relationships |
| **One table per query** | Denormalize aggressively, joins are not supported |
| **Partition key is king** | All data for one key lives on one node |
| **No joins, no foreign keys** | Duplicate data across tables |
| **Eventually consistent** | Reads may not see latest writes immediately |

### Spring Data Cassandra

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

```java
@Table("users_by_email")
public class UserByEmail {
    @PrimaryKey
    private String email;
    private UUID userId;
    private String name;
    private Instant createdAt;
}

public interface UserByEmailRepository extends CassandraRepository<UserByEmail, String> {
    List<UserByEmail> findByName(String name);
}
```

## When to Use Which

| Database | Best For | Avoid When |
|----------|----------|------------|
| PostgreSQL | Structured data, complex relationships, ACID | Massive scale, flexible schema |
| MongoDB | Flexible schema, nested data, rapid prototyping | Complex transactions, joins |
| Redis | Caching, sessions, real-time data, queues | Data > RAM, complex queries |
| Cassandra | High-volume writes, multi-DC, time-series | Complex queries, strong consistency |

## Polyglot Persistence

Modern apps use MULTIPLE databases:

```
┌──────────────────────────────────────────┐
│          YOUR APPLICATION                  │
├──────────────────────────────────────────┤
│  ┌─────────┐  ┌────────┐  ┌──────────┐  │
│  │PostgreSQL│  │ Redis  │  │MongoDB   │  │
│  │Orders,    │  │Session,│  │Catalog,  │  │
│  │Users,     │  │Cache,  │  │Content,  │  │
│  │Relations  │  │Queue   │  │Analytics │  │
│  └─────────┘  └────────┘  └──────────┘  │
└──────────────────────────────────────────┘
```

## Exercises

1. Set up MongoDB with a document containing embedded sub-documents. Query by nested field.
2. Implement Redis caching for a slow database query. Measure the speed difference.
3. Create a Redis sorted set leaderboard in a Spring Boot app.
4. Model a Cassandra table for time-series sensor data (device_id, timestamp, value).
5. Read about a company that uses polyglot persistence and diagram their database architecture.
