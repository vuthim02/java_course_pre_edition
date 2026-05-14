# Enterprise Engineering — Lesson 5: JPA & Hibernate ORM Performance

## Why ORM Performance Matters

JPA/Hibernate is powerful — but it hides SQL. Hidden queries → N+1 problems, lazy loading explosions, and massive memory usage. Understanding ORM internals is critical for production systems.

```
Your Code:                              Actual SQL:
user.getOrders().forEach(o -> {         SELECT * FROM users WHERE id = 1;
    System.out.println(o.getTotal());    SELECT * FROM orders WHERE user_id = 1;
    o.getItems().forEach(i -> {          SELECT * FROM order_items WHERE order_id = 10;
        System.out.println(i.getName()); SELECT * FROM order_items WHERE order_id = 11;
    });                                  SELECT * FROM order_items WHERE order_id = 12;
});                                      ... 100+ queries!
```

## The Hibernate Lifecycle

```
                    ┌─────────────┐
                    │   NEW       │  Created with `new`, not tracked
                    └──────┬──────┘
                           │ persist() / save()
                           ▼
                    ┌─────────────┐
                    │ MANAGED     │  Tracked by persistence context
                    └──────┬──────┘
                     ┌─────┴─────┐
                     ▼           ▼
              ┌──────────┐ ┌──────────┐
              │REMOVED   │ │DETACHED  │  Session closed / evicted
              └──────────┘ └──────────┘
```

## The N+1 Query Problem

```java
// WRONG — N+1 queries!
@OneToMany
private List<Order> orders;

// Usage:
List<User> users = userRepository.findAll();
for (User user : users) {
    System.out.println(user.getOrders().size());  // Triggers SELECT per user!
}
```

### Solution 1: JOIN FETCH

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();
```

### Solution 2: Entity Graph

```java
@EntityGraph(attributePaths = {"orders"})
@Query("SELECT u FROM User u")
List<User> findAllWithOrders();
```

### Solution 3: Batch Fetching

```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=20
```

## N+1 Monitoring

```properties
# Log every SQL query
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql=trace
```

## Caching Levels

```
┌─────────────────────────────────────────────────────────────┐
│                     FIRST LEVEL CACHE                       │
│              (Persistence Context — per Session)             │
│  Auto-enabled. Same entity fetched twice = 1 SQL query.     │
├─────────────────────────────────────────────────────────────┤
│                     SECOND LEVEL CACHE                      │
│              (SessionFactory — across Sessions)              │
│  Must be explicitly enabled (Hazelcast, Redis, EHCache).    │
├─────────────────────────────────────────────────────────────┤
│                     QUERY CACHE                             │
│              Caches query results, not entities.             │
│  Useful for read-heavy, static data.                        │
└─────────────────────────────────────────────────────────────┘
```

### Enabling Second-Level Cache

```xml
<dependency>
    <groupId>org.hibernate.orm</groupId>
    <artifactId>hibernate-jcache</artifactId>
</dependency>
<dependency>
    <groupId>org.ehcache</groupId>
    <artifactId>ehcache</artifactId>
</dependency>
```

```properties
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=jcache
spring.jpa.properties.hibernate.cache.use_query_cache=true
```

```java
@Entity
@Cacheable  // Enable 2LC for this entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    @Id private Long id;
    private String name;
    private BigDecimal price;
}
```

## Lazy Loading Pitfalls

```java
// Opens session, loads user, closes session
User user = userService.findById(1L);

// THROWS LazyInitializationException!
// Session is closed, can't load orders
System.out.println(user.getOrders().size());
```

### Solutions

| Approach | How | Trade-off |
|----------|-----|-----------|
| JOIN FETCH | Load everything in one query | More data per query |
| Entity Graph | Declarative fetch plan | Static definition |
| Open Session in View | Keep session open for view | Performance issues in production |
| `@Transactional` on service | Keep session active | Long-running transactions |
| DTO Projection | Select only needed fields | Must write mappings |

## DTO Projections (Best for Read)

```java
// Instead of loading full entities:
public interface UserSummary {
    String getName();
    String getEmail();
    int getOrderCount();
}

@Query("SELECT u.name AS name, u.email AS email, " +
       "COUNT(o.id) AS orderCount " +
       "FROM User u LEFT JOIN u.orders o " +
       "GROUP BY u.id")
List<UserSummary> findAllSummaries();
```

## Batch Operations

```java
// WRONG — loads all entities into memory
List<Product> products = productRepository.findAll();
products.forEach(p -> p.setPrice(p.getPrice().multiply(BigDecimal.valueOf(1.1))));
productRepository.saveAll(products);

// RIGHT — single UPDATE statement
@Modifying
@Query("UPDATE Product p SET p.price = p.price * 1.1 WHERE p.category = :category")
int bulkPriceUpdate(@Param("category") String category);
```

## Flush Modes

| Mode | Behavior | When to Use |
|------|----------|-------------|
| AUTO | Flush before queries | Default, safe |
| COMMIT | Flush only at transaction commit | Read-heavy, batch writes |
| ALWAYS | Flush on every operation | Rarely needed |
| MANUAL | Never auto-flush | Native SQL, very large batches |

## Inheritance Strategies

```java
@Entity
@Inheritance(strategy = InheritanceType.JOINED)  // Normalized, slower reads
// @Inheritance(strategy = InheritanceType.SINGLE_TABLE)  // Fast reads, nullable columns
// @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)  // Flexible, complex queries
public abstract class Payment {
    @Id private Long id;
    private BigDecimal amount;
}

@Entity
public class CreditCardPayment extends Payment {
    private String cardNumber;
    private String cardHolder;
}
```

| Strategy | Tables | Queries | Normalization |
|----------|--------|---------|---------------|
| SINGLE_TABLE | 1 | Fast | Poor (nullable columns) |
| JOINED | N+1 | Slower (joins) | Perfect |
| TABLE_PER_CLASS | N | Union queries | Good |

## Exercises

1. Enable SQL logging on a Spring Boot app with JPA. Identify any N+1 queries.
2. Fix an N+1 problem using JOIN FETCH and verify the fix with SQL logging.
3. Add second-level cache with EHCache to an entity. Measure performance difference.
4. Create a DTO projection for a complex entity relationship.
5. Write a batch update using `@Modifying` with `@Query`.

## Performance Checklist

- [ ] SQL logging enabled during development
- [ ] N+1 queries eliminated (JOIN FETCH / Entity Graph / batch)
- [ ] Appropriate fetch strategy (LAZY for collections, EAGER for small relationships)
- [ ] DTO projections for read-heavy endpoints
- [ ] Batch operations instead of looped saves
- [ ] Pagination for large result sets (never `findAll()`)
- [ ] Second-level cache for read-heavy, static data
- [ ] Connection pool sized correctly (HikariCP)
- [ ] Indexes on foreign keys and query columns
- [ ] `@Transactional` boundaries properly defined
