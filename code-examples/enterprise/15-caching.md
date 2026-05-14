# Caching: Redis, Hazelcast, Caffeine

Caching improves application performance by storing frequently accessed data in memory. Spring's Cache abstraction provides annotation-driven caching, with pluggable CacheManager implementations for Redis, Caffeine, Hazelcast, and more.

## @EnableCaching and Cache Annotations

```java
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableCaching
public class CacheConfig {
    // CacheManager beans defined below
}

@Service
public class CachedUserService {

    private final UserRepository userRepository;

    public CachedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Cache result — cache name is "users", key is the userId
    @Cacheable(value = "users", key = "#id")
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    // Conditional caching — only cache if result is present
    @Cacheable(value = "users", key = "#email", unless = "#result == null")
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // Multiple cache names — data written to both caches
    @Cacheable({"users", "profiles"})
    public UserProfile getProfile(Long userId) {
        return loadProfile(userId);
    }

    // Cache with complex key (SpEL)
    @Cacheable(value = "users", key = "T(java.lang.String).format('%s-%d', #name, #age)")
    public List<User> searchByNameAndAge(String name, int age) {
        return userRepository.findByNameAndAge(name, age);
    }

    // @CachePut — always executes method and updates cache
    @CachePut(value = "users", key = "#result.id()")
    public User updateUser(UpdateUserRequest request) {
        return userRepository.update(request);
    }

    // @CacheEvict — remove entry from cache
    @CacheEvict(value = "users", key = "#id")
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    // Evict all entries in a cache
    @CacheEvict(value = "users", allEntries = true)
    public void clearUserCache() {
        System.out.println("User cache cleared");
    }

    // Evict before method execution
    @CacheEvict(value = "users", key = "#id", beforeInvocation = true)
    public void updateBeforeEvict(Long id, String name) {
        // cache cleared before method runs
    }

    // Multiple annotations
    @Caching(
        cacheable = @Cacheable("products"),
        put = @CachePut(value = "products-by-sku", key = "#result.sku()")
    )
    public Product createProduct(CreateProductRequest request) {
        return productRepository.save(request);
    }

    // Combined evict and put
    @Caching(evict = {
        @CacheEvict(value = "users", key = "#id"),
        @CacheEvict(value = "user-profiles", key = "#id")
    })
    public void deleteUserData(Long id) {
        userRepository.delete(id);
    }
}
```

## CacheManager Configuration

### Caffeine

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

```java
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineCacheConfig {

    @Bean
    @Primary
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setDefaultCacheSpec("initialCapacity=100,maximumSize=500,expireAfterWrite=10m");
        manager.setAllowNullValues(false);

        // Cache definitions per region
        manager.registerCustomCache("users",
            Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .recordStats()
                .build());

        manager.registerCustomCache("products",
            Caffeine.newBuilder()
                .initialCapacity(200)
                .maximumSize(5000)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .expireAfterAccess(30, TimeUnit.MINUTES)
                .build());

        manager.registerCustomCache("reference-data",
            Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterWrite(24, TimeUnit.HOURS)
                .build());

        return manager;
    }

    @Bean
    public CaffeineCacheManager smallCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCacheSpecification(
            "initialCapacity=10,maximumSize=50,expireAfterWrite=5m");
        return manager;
    }
}
```

```yaml
spring:
  cache:
    type: caffeine
    caffeine:
      spec: initialCapacity=100,maximumSize=500,expireAfterWrite=10m
```

### Redis

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```java
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.*;
import java.time.Duration;
import java.util.Map;

@Configuration
public class RedisCacheConfig {

    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .disableCachingNullValues()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new GenericJackson2JsonRedisSerializer()));

        Map<String, RedisCacheConfiguration> cacheConfigs = Map.of(
            "users", defaultConfig.entryTtl(Duration.ofMinutes(30)),
            "products", defaultConfig.entryTtl(Duration.ofHours(1)),
            "sessions", defaultConfig.entryTtl(Duration.ofMinutes(5)),
            "reference-data", defaultConfig.entryTtl(Duration.ofDays(1))
        );

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigs)
            .transactionAware()
            .build();
    }
}
```

```yaml
spring:
  cache:
    type: redis
  redis:
    host: localhost
    port: 6379
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 4
```

### Hazelcast (Embedded)

```xml
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast</artifactId>
</dependency>
<dependency>
    <groupId>com.hazelcast</groupId>
    <artifactId>hazelcast-spring</artifactId>
</dependency>
```

```java
import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.CacheManager;
import org.springframework.cache.hazelcast.HazelcastCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastCacheConfig {

    @Bean
    public HazelcastInstance hazelcastInstance() {
        Config config = new Config()
            .setInstanceName("enterprise-cache")
            .addMapConfig(new MapConfig()
                .setName("users")
                .setMaxSizeConfig(new MaxSizeConfig(1000, MaxSizePolicy.USED_HEAP_SIZE))
                .setEvictionPolicy(EvictionPolicy.LRU)
                .setTimeToLiveSeconds(1800)     // 30 minutes
                .setMaxIdleSeconds(600))         // 10 minutes idle
            .addMapConfig(new MapConfig()
                .setName("products")
                .setMaxSizeConfig(new MaxSizeConfig(5000, MaxSizePolicy.USED_HEAP_SIZE))
                .setEvictionPolicy(EvictionPolicy.LFU)
                .setTimeToLiveSeconds(3600))
            .addMapConfig(new MapConfig()
                .setName("sessions")
                .setTimeToLiveSeconds(300)       // 5 minutes
                .setMaxIdleSeconds(120))
            .addMapConfig(new MapConfig()
                .setName("reference-data")
                .setTimeToLiveSeconds(86400)     // 24 hours
                .setEvictionPolicy(EvictionPolicy.NONE)); // Never evict

        // Network config for clustered Hazelcast
        config.getNetworkConfig()
            .setPort(5701)
            .setPortAutoIncrement(true)
            .getJoin()
                .getMulticastConfig().setEnabled(true)
            .getTcpIpConfig()
                .setEnabled(true)
                .addMember("10.0.0.1")
                .addMember("10.0.0.2");

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean
    public CacheManager hazelcastCacheManager(HazelcastInstance instance) {
        return new HazelcastCacheManager(instance);
    }
}
```

## Custom Cache Key Generator

```java
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.util.Arrays;

@Component("customKeyGenerator")
public class CustomKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        // Generate a key from all params plus the method name
        return method.getName() + "_" + Arrays.deepToString(params);
    }
}

// Or use SpEL right in the annotation:
// @Cacheable(value = "users", keyGenerator = "customKeyGenerator")
// public Optional<User> findUser(String email, boolean includeInactive) { ... }
```

```java
@Component("userIdKeyGenerator")
public class UserIdKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        // Extract the first Long parameter as key
        return Arrays.stream(params)
            .filter(p -> p instanceof Long)
            .findFirst()
            .orElse(SimpleKey.EMPTY);
    }
}
```

```java
// @Cacheable(keyGenerator = "userIdKeyGenerator")
// public Optional<User> getUser(Long id) { ... }
```

## JCache (JSR-107) Annotations

```xml
<dependency>
    <groupId>javax.cache</groupId>
    <artifactId>cache-api</artifactId>
</dependency>
```

```java
import javax.cache.annotation.*;

@Service
public class JCacheExampleService {

    @CacheResult(cacheName = "users")
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @CachePut(cacheName = "users")
    public User update(User user) {
        return userRepository.save(user);
    }

    @CacheRemoveEntry(cacheName = "users")
    public void delete(Long id) {
        userRepository.deleteById(id);
    }

    @CacheRemoveAll(cacheName = "users")
    public void clearAll() {
        // Clear entire cache
    }
}
```

## Cache Eviction Policies

```java
// Eviction occurs automatically based on configured policies:

// Caffeine eviction policies:
// - maximumSize: evicts when count exceeds threshold (LRU approximation)
// - maximumWeight: evicts when total weight exceeds threshold
// - expireAfterWrite: evicts N after last write
// - expireAfterAccess: evicts N after last read/write

// Redis eviction policies (configured on Redis server):
// - volatile-lru: evict least recently used keys with TTL
// - allkeys-lru: evict LRU regardless of TTL
// - volatile-ttl: evict keys with shortest TTL
// - noeviction: return error on write when memory limit hit

// Hazelcast eviction policies:
// - LRU (Least Recently Used)
// - LFU (Least Frequently Used)
// - NONE (no eviction)
// - RANDOM

// Programmatic eviction:
@Service
public class CacheManagementService {

    private final CacheManager cacheManager;

    public CacheManagementService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    public void evictAll() {
        cacheManager.getCacheNames().forEach(name -> {
            Cache cache = cacheManager.getCache(name);
            if (cache != null) cache.clear();
        });
    }

    public void evictUser(Long userId) {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.evict(userId);
        }
    }

    public void putEntry(String cacheName, Object key, Object value) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.put(key, value);
        }
    }

    public Object getEntry(String cacheName, Object key) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            return cache.get(key);
        }
        return null;
    }
}
```
