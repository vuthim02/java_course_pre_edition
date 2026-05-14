# Project 4: Large-Scale API Gateway

**Concepts:** Reactive routing, Rate limiting (distributed), JWT validation, Circuit breaker, Request/response transformation, Load balancing, Caching, Distributed tracing, Metrics

## Architecture

```
                    ┌──────────────────────────────────┐
                    │         API GATEWAY               │
                    │                                   │
  Client ──────────▶│  ┌────┐  ┌─────┐  ┌──────────┐  │────▶ Service A
                    │  │Auth│─▶│Rate │─▶│ Router + │  │────▶ Service B
                    │  │    │  │Limit│  │ CB + LB  │  │────▶ Service C
                    │  └────┘  └─────┘  └──────────┘  │
                    │         │            │           │
                    │    ┌────▼────┐ ┌────▼───────┐   │
                    │    │ Redis   │ │ Cache       │   │
                    │    │(Rate+Tok│ │ (Redis)     │   │
                    │    │ en)     │ │             │   │
                    │    └─────────┘ └─────────────┘   │
                    └──────────────────────────────────┘
```

## Table of Contents
1. [Gateway Application](#1-gateway-application)
2. [Authentication Filter](#2-authentication-filter)
3. [Rate Limiter](#3-rate-limiter)
4. [Route Configuration](#4-route-configuration)
5. [Circuit Breaker](#5-circuit-breaker)
6. [Load Balancer](#6-load-balancer)
7. [Response Cache](#7-response-cache)
8. [Distributed Tracing](#8-distributed-tracing)
9. [Metrics & Monitoring](#9-metrics--monitoring)
10. [Docker Compose](#10-docker-compose)

---

## 1. Gateway Application

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.gateway</groupId>
    <artifactId>api-gateway</artifactId>
    <version>1.0.0</version>
    <name>api-gateway</name>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis-reactive</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.5</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.5</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### Main Application

```java
package com.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```

### application.yml

```yaml
spring:
  application:
    name: api-gateway

  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/users/**
          filters:
            - name: CircuitBreaker
              args:
                name: userServiceCB
                fallbackUri: forward:/fallback/users
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
            - name: JwtAuth

        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/v1/orders/**
          filters:
            - CircuitBreaker=orderServiceCB
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 50
                redis-rate-limiter.burstCapacity: 100
            - JwtAuth

        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/v1/products/**
          filters:
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 200
                redis-rate-limiter.burstCapacity: 400
            - JwtAuth
            - name: CacheRequestBody

        - id: public-stripe
          uri: https://api.stripe.com
          predicates:
            - Path=/api/v1/payments/webhook
          filters:
            - StripPrefix=2

      default-filters:
        - name: RequestLogging
        - name: CorrelationId

  redis:
    host: localhost
    port: 6379

server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,gateway
  tracing:
    sampling:
      probability: 1.0
    propagation:
      type: w3c

logging:
  level:
    com.gateway: INFO
    org.springframework.cloud.gateway: INFO

jwt:
  secret: your-256-bit-secret-key-that-must-be-at-least-256-bits-long
  expiration-ms: 3600000
```

---

## 2. Authentication Filter (JWT Validation)

```java
package com.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final SecretKey secretKey;
    private final List<String> publicPaths = List.of(
        "/api/v1/auth/login",
        "/api/v1/auth/register",
        "/api/v1/payments/webhook",
        "/actuator/health"
    );

    public JwtAuthGatewayFilterFactory(
            @Value("${jwt.secret}") String secret) {
        super(Object.class);
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();

            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest()
                .getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return unauthorized(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            try {
                Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

                // Add user info to headers for downstream services
                ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(r -> r
                        .header("X-User-Id", claims.getSubject())
                        .header("X-User-Roles",
                            String.join(",", claims.get("roles", List.class)))
                    )
                    .build();

                return chain.filter(modifiedExchange);
            } catch (JwtException e) {
                return unauthorized(exchange, "Invalid or expired token");
            }
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders()
            .add(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        return exchange.getResponse()
            .writeWith(Mono.just(exchange.getResponse()
                .bufferFactory().wrap(
                    ("{\"error\":\"" + message + "\"}").getBytes())));
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }
}
```

---

## 3. Distributed Rate Limiter (Redis + Reactive)

```java
package com.gateway.ratelimit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.AbstractRateLimiter;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Component
public class RedisRateLimiter extends AbstractRateLimiter<RedisRateLimiter.Config> {

    private static final String CONFIG_KEY = "redis-rate-limiter";
    private static final String LUA_SCRIPT = """
        local key = KEYS[1]
        local now = tonumber(ARGV[1])
        local window = tonumber(ARGV[2])
        local limit = tonumber(ARGV[3])
        
        redis.call('ZREMRANGEBYSCORE', key, 0, now - window)
        
        local count = redis.call('ZCARD', key)
        if count < limit then
            redis.call('ZADD', key, now, now .. ':' .. math.random())
            redis.call('EXPIRE', key, window)
            return {1, limit - count - 1}
        end
        
        return {0, 0}
        """;

    private final ReactiveStringRedisTemplate redis;
    private final RedisScript<List<Long>> script;

    public RedisRateLimiter(
            ReactiveStringRedisTemplate redis,
            @Qualifier("defaultRedisScript") RedisScript<List<Long>> script) {
        super(Config.class, CONFIG_KEY, new Config());
        this.redis = redis;
        this.script = script;
    }

    @Override
    public Mono<Response> isAllowed(String routeId, String id) {
        Config config = getConfig().get(routeId);
        if (config == null) config = new Config();

        String key = "rate_limit:" + routeId + ":" + id;
        long now = Instant.now().getEpochSecond();
        long window = config.windowSeconds;

        return redis.execute(script, List.of(key),
                List.of(String.valueOf(now), String.valueOf(window),
                        String.valueOf(config.limit)))
            .map(result -> {
                boolean allowed = result.get(0) == 1;
                long remaining = result.get(1);
                return new Response(allowed, java.util.Map.of(
                    "X-RateLimit-Limit", String.valueOf(config.limit),
                    "X-RateLimit-Remaining", String.valueOf(remaining),
                    "X-RateLimit-Reset", String.valueOf(now + window)
                ));
            });
    }

    public static class Config {
        private int limit = 100;
        private int windowSeconds = 60;

        public int getLimit() { return limit; }
        public int getWindowSeconds() { return windowSeconds; }
        public void setLimit(int limit) { this.limit = limit; }
        public void setWindowSeconds(int windowSeconds) { this.windowSeconds = windowSeconds; }
    }
}
```

### Redis Script Bean

```java
package com.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisScript<List<Long>> defaultRedisScript() {
        DefaultRedisScript<List<Long>> script = new DefaultRedisScript<>();
        script.setScriptText("""
            local key = KEYS[1]
            local now = tonumber(ARGV[1])
            local window = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])

            redis.call('ZREMRANGEBYSCORE', key, 0, now - window)

            local count = redis.call('ZCARD', key)
            if count < limit then
                redis.call('ZADD', key, now, now .. ':' .. math.random())
                redis.call('EXPIRE', key, window)
                return {1, limit - count - 1}
            end

            return {0, 0}
        """);
        script.setResultType(List.class);
        return script;
    }
}
```

---

## 4. Dynamic Route Configuration

```java
package com.gateway.route;

import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DynamicRouteDefinitionRepository implements RouteDefinitionRepository {

    private final Map<String, RouteDefinition> routes = new ConcurrentHashMap<>();
    private final ReactiveRedisTemplate<String, RouteDefinition> redis;

    public DynamicRouteDefinitionRepository(ReactiveRedisTemplate<String, RouteDefinition> redis) {
        this.redis = redis;
        loadInitialRoutes();
    }

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        return Flux.fromIterable(routes.values());
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return route.doOnNext(r -> {
            routes.put(r.getId(), r);
            redis.opsForValue().set("gateway:route:" + r.getId(), r).subscribe();
        }).then();
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return routeId.doOnNext(id -> {
            routes.remove(id);
            redis.delete("gateway:route:" + id).subscribe();
        }).then();
    }

    private void loadInitialRoutes() {
        // Routes are loaded from application.yml
        // This repository supports dynamic additions at runtime
    }
}
```

### Route Definition DTO

```java
package com.gateway.route;

public record RouteConfig(
    String id,
    String uri,
    String path,
    String method,
    int rateLimit,
    int rateLimitWindow,
    boolean requireAuth,
    boolean circuitBreaker,
    int timeoutMs,
    java.util.Map<String, String> headers
) {}
```

---

## 5. Circuit Breaker

```java
package com.gateway.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CircuitBreakerGatewayFilterFactory
        extends AbstractGatewayFilterFactory<CircuitBreakerGatewayFilterFactory.Config> {

    private final CircuitBreakerRegistry registry;
    private final Map<String, CircuitBreaker> breakers = new ConcurrentHashMap<>();

    public CircuitBreakerGatewayFilterFactory() {
        super(Config.class);

        this.registry = CircuitBreakerRegistry.of(CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(10))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build());
    }

    @Override
    public GatewayFilter apply(Config config) {
        CircuitBreaker cb = breakers.computeIfAbsent(config.name,
            name -> registry.circuitBreaker(name));

        return (exchange, chain) -> {
            return chain.filter(exchange)
                .transformDeferred(CircuitBreakerOperator.of(cb))
                .onErrorResume(throwable -> {
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    exchange.getResponse().getHeaders().add("X-Circuit-Breaker", "open");
                    return exchange.getResponse()
                        .writeWith(Mono.just(exchange.getResponse().bufferFactory()
                            .wrap(("{\"error\":\"Service " + config.name
                                + " is unavailable\"}").getBytes())));
                });
        };
    }

    public static class Config {
        private String name;
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}
```

---

## 6. Load Balancer — Weighted Response-Time

```java
package com.gateway.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class LatencyAwareLoadBalancer implements ReactorServiceInstanceLoadBalancer {

    private final String serviceId;
    private final ServiceInstanceListSupplier supplier;
    private final Map<String, LatencyStats> stats = new ConcurrentHashMap<>();

    public LatencyAwareLoadBalancer(ServiceInstanceListSupplier supplier, String serviceId) {
        this.supplier = supplier;
        this.serviceId = serviceId;
    }

    @Override
    public Mono<Response<ServiceInstance>> choose(Request request) {
        return supplier.get(request)
            .next()
            .map(instances -> {
                if (instances.isEmpty()) {
                    return new EmptyResponse();
                }

                // Weight by recent latency (lower is better)
                double totalWeight = 0;
                double[] weights = new double[instances.size()];

                for (int i = 0; i < instances.size(); i++) {
                    ServiceInstance instance = instances.get(i);
                    String key = instance.getHost() + ":" + instance.getPort();
                    LatencyStats s = stats.getOrDefault(key, new LatencyStats());
                    double weight = 1.0 / Math.max(1, s.avgLatencyMs);
                    weights[i] = weight;
                    totalWeight += weight;
                }

                // Weighted random selection
                double random = Math.random() * totalWeight;
                double cumulative = 0;
                for (int i = 0; i < instances.size(); i++) {
                    cumulative += weights[i];
                    if (random <= cumulative) {
                        return new DefaultResponse(instances.get(i));
                    }
                }

                return new DefaultResponse(instances.get(0));
            });
    }

    public void recordLatency(String host, int port, long latencyMs) {
        String key = host + ":" + port;
        stats.compute(key, (k, v) -> {
            if (v == null) v = new LatencyStats();
            v.avgLatencyMs = (v.avgLatencyMs * v.count + latencyMs) / (v.count + 1);
            v.count++;
            v.lastUpdated = Instant.now();
            return v;
        });
    }

    static class LatencyStats {
        double avgLatencyMs = 50;
        long count;
        Instant lastUpdated;
    }
}
```

### Load Balancer Config

```java
package com.gateway.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class LoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactiveLoadBalancer(
            Environment env,
            LoadBalancerClientFactory factory) {

        String serviceId = env.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
        return new LatencyAwareLoadBalancer(
            factory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class),
            serviceId
        );
    }
}
```

---

## 7. Response Cache

```java
package com.gateway.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Set;

@Component
public class ResponseCache {

    private final ReactiveRedisTemplate<String, String> redis;
    private final Set<String> cacheableMethods = Set.of("GET", "HEAD");
    private final Duration defaultTtl = Duration.ofSeconds(60);

    // Cacheable paths and their TTL in seconds
    private final java.util.Map<String, Integer> cacheConfig = java.util.Map.of(
        "/api/v1/products", 120,
        "/api/v1/categories", 300,
        "/api/v1/users/me", 30
    );

    public ResponseCache(ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }

    public Mono<Boolean> isCached(ServerWebExchange exchange) {
        String cacheKey = buildCacheKey(exchange);
        return redis.hasKey(cacheKey);
    }

    public Mono<String> get(ServerWebExchange exchange) {
        String cacheKey = buildCacheKey(exchange);
        return redis.opsForValue().get(cacheKey);
    }

    public Mono<Void> put(ServerWebExchange exchange, String body, HttpStatus status) {
        String cacheKey = buildCacheKey(exchange);
        int ttl = cacheConfig.entrySet().stream()
            .filter(e -> exchange.getRequest().getURI().getPath().startsWith(e.getKey()))
            .map(e -> e.getValue())
            .findFirst()
            .orElse((int) defaultTtl.toSeconds());

        return redis.opsForValue()
            .set(cacheKey, status.value() + ":" + body, Duration.ofSeconds(ttl))
            .then();
    }

    public boolean isCacheable(ServerWebExchange exchange) {
        HttpMethod method = exchange.getRequest().getMethod();
        if (method == null || !cacheableMethods.contains(method.name())) return false;

        String path = exchange.getRequest().getURI().getPath();
        return cacheConfig.keySet().stream().anyMatch(path::startsWith);
    }

    private String buildCacheKey(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        String query = exchange.getRequest().getURI().getQuery();
        String auth = exchange.getRequest().getHeaders()
            .getFirst(HttpHeaders.AUTHORIZATION);

        return "cache:" + (auth != null ? "auth:" : "anon:")
            + path + (query != null ? "?" + query : "");
    }
}
```

### Cache Filter

```java
package com.gateway.cache;

import com.gateway.security.JwtAuthGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CacheGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final ResponseCache cache;

    public CacheGatewayFilterFactory(ResponseCache cache) {
        super(Object.class);
        this.cache = cache;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            if (!cache.isCacheable(exchange)) {
                return chain.filter(exchange);
            }

            return cache.isCached(exchange).flatMap(isCached -> {
                if (isCached) {
                    return cache.get(exchange).flatMap(cached -> {
                        String[] parts = cached.split(":", 2);
                        HttpStatus status = HttpStatus.valueOf(Integer.parseInt(parts[0]));
                        exchange.getResponse().setStatusCode(status);
                        exchange.getResponse().getHeaders()
                            .add(HttpHeaders.CACHE_CONTROL, "public, max-age=60");
                        exchange.getResponse().getHeaders()
                            .add("X-Cache", "HIT");
                        return exchange.getResponse()
                            .writeWith(Mono.just(exchange.getResponse()
                                .bufferFactory().wrap(parts[1].getBytes())));
                    });
                }

                // Cache miss — proceed and cache response
                return chain.filter(exchange).then(Mono.defer(() -> {
                    // In production, capture response body and cache it
                    exchange.getResponse().getHeaders()
                        .add("X-Cache", "MISS");
                    return Mono.empty();
                }));
            });
        };
    }
}
```

---

## 8. Distributed Tracing & Correlation

```java
package com.gateway.config;

import brave.Tracing;
import brave.propagation.Propagation;
import brave.propagation.TraceContext;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private static final String CORRELATION_ID = "X-Correlation-Id";

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String correlationId = exchange.getRequest()
                .getHeaders()
                .getFirst(CORRELATION_ID);

            if (correlationId == null || correlationId.isBlank()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Add correlation ID to request headers for downstream
            var mutatedExchange = exchange.mutate()
                .request(r -> r.header(CORRELATION_ID, correlationId))
                .build();

            // Add to response headers
            mutatedExchange.getResponse()
                .getHeaders().add(CORRELATION_ID, correlationId);

            return chain.filter(mutatedExchange);
        };
    }
}
```

### Request Logging Filter

```java
package com.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RequestLoggingGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private static final Logger log = LoggerFactory.getLogger("GATEWAY");

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            long start = System.currentTimeMillis();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - start;
                log.info("{} {} -> {} [{}ms] [{}]",
                    request.getMethod(),
                    request.getURI().getPath(),
                    exchange.getResponse().getStatusCode(),
                    duration,
                    request.getHeaders().getFirst("X-Correlation-Id")
                );
            }));
        };
    }
}
```

---

## 9. Metrics & Monitoring

```java
package com.gateway.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MetricsGatewayFilterFactory
        extends AbstractGatewayFilterFactory<Object> {

    private final MeterRegistry meterRegistry;
    private final Map<String, Counter> requestCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> requestTimers = new ConcurrentHashMap<>();

    public MetricsGatewayFilterFactory(MeterRegistry meterRegistry) {
        super(Object.class);
        this.meterRegistry = meterRegistry;
    }

    @Override
    public GatewayFilter apply(Object config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getURI().getPath();
            String method = exchange.getRequest().getMethod().name();
            String routeId = exchange.getAttribute("gateway.routeId");
            if (routeId == null) routeId = "unknown";

            long start = System.nanoTime();

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                long duration = System.nanoTime() - start;
                int status = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value() : 0;

                // Record metrics
                requestCounters.computeIfAbsent(
                    routeId + "." + status,
                    k -> Counter.builder("gateway.requests")
                        .tag("route", routeId)
                        .tag("method", method)
                        .tag("status", String.valueOf(status))
                        .register(meterRegistry)
                ).increment();

                requestTimers.computeIfAbsent(
                    routeId,
                    k -> Timer.builder("gateway.request.duration")
                        .tag("route", routeId)
                        .register(meterRegistry)
                ).record(java.time.Duration.ofNanos(duration));

                // Record response size
                var response = exchange.getResponse();
                if (response.getHeaders().getContentLength() > 0) {
                    meterRegistry.summary("gateway.response.size", "route", routeId)
                        .record(response.getHeaders().getContentLength());
                }
            }));
        };
    }
}
```

---

## 10. Docker Compose

```yaml
# docker-compose.yml
version: '3.9'

services:
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data

  gateway:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_REDIS_HOST: redis
      JWT_SECRET: your-256-bit-secret-key-that-must-be-at-least-256-bits-long
    depends_on:
      - redis

  prometheus:
    image: prom/prometheus:latest
    ports:
      - "9090:9090"
    volumes:
      - ./prometheus.yml:/etc/prometheus/prometheus.yml

  grafana:
    image: grafana/grafana:latest
    ports:
      - "3000:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin
    volumes:
      - grafana_data:/var/lib/grafana

volumes:
  redis_data:
  grafana_data:
```

### prometheus.yml

```yaml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['gateway:8080']
```

---

## API Endpoints Summary

| Method | Path | Auth | Rate Limit | Cache | Description |
|--------|------|------|-----------|-------|-------------|
| ANY | `/api/v1/users/**` | JWT | 100/min | No | Proxy to user-service |
| ANY | `/api/v1/orders/**` | JWT | 50/min | No | Proxy to order-service |
| GET | `/api/v1/products/**` | JWT | 200/min | 120s | Proxy to product-service |
| POST | `/api/v1/payments/webhook` | No | No | No | Proxy to Stripe |
| GET | `/actuator/health` | No | No | No | Health check |
| GET | `/actuator/prometheus` | No | No | No | Metrics |
