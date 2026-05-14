# Enterprise Engineering — Lesson 15: Caching (Redis, Hazelcast, Caffeine)

## Why Cache?

```
Without Cache:                         With Cache:
┌──────────┐     ┌──────────┐          ┌──────────┐     ┌──────────┐
│Client    │────▶│Database  │          │Client    │─┐    │Database  │
│          │     │(SLOW)    │          │          │ │    │(SLOW)    │
│ Request  │     │ 50ms     │          │ Request  │ │    │          │
│  100/sec  │     │          │          │  100/sec  │ │    │          │
└──────────┘     └──────────┘          └──────────┘ │    └──────────┘
                                                     │
  CPU at 100%, DB struggling           ┌─────────────┘
                                       │ Cache HIT?    ┌────┐
                                       ├── YES ────────▶│Cache│
                                       │                │(FAST│
                                       │ NO              │1ms) │
                                       │                └────┘
                                       ▼ Database hit
                                       50ms
```

| Metric | No Cache | With Cache |
|--------|----------|------------|
| Response time (p50) | 50ms | 1ms |
| Response time (p99) | 200ms | 5ms |
| Database load | 100% | 10% |
| CPU usage | 90% | 20% |

## Caching Strategies

### Cache-Aside

```
Application checks cache first, then database:
1. READ: Check cache → miss → query DB → store in cache → return
2. WRITE: Write to DB → invalidate cache entry
```

```java
public User getUserById(Long id) {
    // Check cache first
    User cached = cache.get("user:" + id);
    if (cached != null) return cached;

    // Cache miss — query database
    User user = userRepository.findById(id).orElse(null);

    // Store in cache
    if (user != null) cache.put("user:" + id, user, 1, TimeUnit.HOURS);

    return user;
}
```

### Read-Through

```
Cache is responsible for loading from DB:
Application only talks to cache.
```

### Write-Through

```
Every write goes through cache to DB:
Write to cache → cache writes to DB → both updated atomically.
```

### Write-Behind

```
Write to cache first, asynchronously write to DB:
Fast writes, but risk of data loss if cache fails.
```

## Redis — Distributed Cache

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

### Configuration

```java
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.builder(factory)
            .cacheDefaults(config)
            .withCacheConfiguration("products",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofMinutes(30)))
            .withCacheConfiguration("users",
                RedisCacheConfiguration.defaultCacheConfig()
                    .entryTtl(Duration.ofHours(2)))
            .build();
    }
}
```

### Spring @Cacheable

```java
@Service
public class ProductService {

    @Cacheable(value = "products", key = "#id",
               unless = "#result == null")
    public Product getProduct(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));
    }

    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @CachePut(value = "products", key = "#product.id")
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Cacheable(value = "products", key = "#category + '_' + #minPrice")
    public List<Product> searchByCategory(String category, BigDecimal minPrice) {
        return productRepository.findByCategoryAndPriceGreaterThan(category, minPrice);
    }
}
```

### Redis Template (Manual)

```java
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value, long ttlSeconds) {
        redisTemplate.opsForValue()
            .set(key, value, Duration.ofSeconds(ttlSeconds));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable((T) value);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void evictByPattern(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
```

## Hazelcast — In-Memory Data Grid

Hazelcast is a distributed cache that runs as a **peer-to-peer grid** (not client-server like Redis):

```
Redis (Client-Server):              Hazelcast (Peer-to-Peer):
┌──────┐  ┌──────┐                  ┌──────────────────────┐
│ App  │  │ App  │                  │  Node 1 ─── Node 2   │
└──┬───┘  └──┬───┘                  │    │     ───     │    │
   │         │                       │  Node 3 ─── Node 4   │
   ▼         ▼                       └──────────────────────┘
┌──────────────────┐                        │    │    │
│    Redis Server   │                  ┌─────┘    │    └─────┐
└──────────────────┘                  ▼          ▼          ▼
                                 App 1      App 2      App 3
```

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
</dependency>
```

```java
@Configuration
public class HazelcastConfig {

    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("hazelcast-instance");

        MapConfig usersCache = new MapConfig("users")
            .setTimeToLiveSeconds(3600)
            .setEvictionConfig(new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE)
                .setSize(500));

        MapConfig sessionsCache = new MapConfig("sessions")
            .setTimeToLiveSeconds(1800)
            .setEvictionConfig(new EvictionConfig()
                .setEvictionPolicy(EvictionPolicy.LFU)
                .setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE)
                .setSize(10000));

        config.addMapConfig(usersCache);
        config.addMapConfig(sessionsCache);
        return config;
    }
}
```

### Hazelcast with Spring

```java
@Service
public class HazelcastCacheService {

    @Autowired
    private HazelcastInstance hazelcastInstance;

    public void putUser(User user) {
        IMap<String, User> map = hazelcastInstance.getMap("users");
        map.put("user:" + user.getId(), user, 1, TimeUnit.HOURS);
    }

    public User getUser(Long id) {
        IMap<String, User> map = hazelcastInstance.getMap("users");
        return map.get("user:" + id);
    }
}
```

## Caffeine — Local Cache

Caffeine is a **high-performance, in-process** cache. Unlike Redis/Hazelcast (distributed), Caffeine lives in the same JVM. It's the fastest option but data isn't shared across instances.

```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

```java
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("users", "products");
        manager.setCaffeine(Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(10_000)
            .recordStats());
        manager.setAsyncCacheMode(true);
        return manager;
    }
}
```

### Caffeine with Manual Control

```java
@Service
public class LocalCacheService {

    private final Cache<Long, User> userCache = Caffeine.newBuilder()
        .expireAfterWrite(30, TimeUnit.MINUTES)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .maximumSize(5_000)
        .removalListener((key, value, cause) ->
            log.debug("Removed {} from cache: {}", key, cause))
        .recordStats()
        .build();

    public User getUser(Long id) {
        return userCache.get(id, key -> {
            log.debug("Cache miss for user: {}", key);
            return userRepository.findById(key).orElse(null);
        });
    }

    public void evictUser(Long id) {
        userCache.invalidate(id);
    }

    public CacheStats getStats() {
        return userCache.stats();
    }
}
```

## Cache Comparison

| Feature | Redis | Hazelcast | Caffeine |
|---------|-------|-----------|----------|
| **Type** | Distributed (client-server) | Distributed (peer-to-peer) | Local (in-process) |
| **Speed** | ~1ms (network round-trip) | ~0.5ms (local if same node) | ~0.01ms (same heap) |
| **Persistence** | Yes (RDB/AOF) | Yes (map store) | No |
| **Data size** | Unlimited (RAM + disk) | Unlimited (RAM) | Limited to JVM heap |
| **Multi-DC** | Yes (cluster) | Yes (WAN replication) | No |
| **External dependency** | Yes (separate server) | No (embedded option) | No |
| **Best for** | Shared cache across instances | Medium-sized clusters | Single-instance, ultra-low-latency |

## Cache Invalidation

The hardest problem in caching: **keeping stale data out**.

| Strategy | How | Risk |
|----------|-----|------|
| TTL (Time-To-Live) | Auto-expire after N seconds | Stale data until TTL expires |
| Write-through | Update cache on every write | Consistent, but slower writes |
| Cache eviction on write | Delete cache entry on write | Next read hits DB |
| Versioning | Store version with cache, compare | Complex to implement |

```java
// Safe invalidation pattern
@Transactional
public User updateUser(User user) {
    // 1. Update database
    User saved = userRepository.save(user);

    // 2. Evict cache
    cacheService.evictByPattern("user:" + saved.getId());

    return saved;
}
```

## Cache-Aside Pattern (Production Example)

```java
@Service
public class ProductService {

    private static final String CACHE_PREFIX = "product:";

    public Product getProductWithCaching(Long id) {
        // 1. Try cache
        Product cached = localCache.getIfPresent(id);  // Caffeine (fast)
        if (cached != null) return cached;

        // 2. Try distributed cache
        Optional<Product> redis = redisCache.get(CACHE_PREFIX + id);
        if (redis.isPresent()) {
            localCache.put(id, redis.get());  // Populate local
            return redis.get();
        }

        // 3. Cache miss — load from DB
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ProductNotFoundException(id));

        // 4. Populate both caches
        localCache.put(id, product);
        redisCache.set(CACHE_PREFIX + id, product, 3600);

        return product;
    }
}
```

## Anti-Patterns

| Anti-Pattern | Why It's Bad | Fix |
|-------------|--------------|-----|
| Caching everything | Wastes memory, high invalidation cost | Cache only expensive queries |
| No TTL | Memory leak, stale data | Always set TTL |
| Caching mutable data without invalidation | Users see stale data | Evict on write |
| Very large cache values | Memory pressure, slow serialization | Cache summaries, not full objects |
| Ignoring cache hit ratio | Can't tell if caching helps | Track hit/miss ratio |

## Exercises

1. Implement cache-aside for a slow database query using Redis.
2. Add `@Cacheable` / `@CacheEvict` to a Spring Boot service.
3. Configure Caffeine as a local L1 cache with Redis as L2.
4. Test TTL invalidation — set 5-second TTL and verify eviction.
5. Measure response times with and without caching using a load test.
