# Spring Cloud: Microservices

Spring Cloud provides tools for building distributed systems: service discovery, API gateways, configuration management, load balancing, circuit breakers, and distributed tracing.

## Eureka Server and Client

### Eureka Server

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

```yaml
# application.yml
server:
  port: 8761

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
```

### Eureka Client

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

```yaml
spring:
  application:
    name: user-service

server:
  port: 0  # random port for local dev

eureka:
  instance:
    instance-id: ${spring.application.name}:${random.value}
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## Spring Cloud Gateway

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import reactor.core.publisher.Mono;
import java.time.Duration;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            // User service
            .route("user-service", r -> r
                .path("/api/v1/users/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .circuitBreaker(config -> config
                        .setName("userServiceCB")
                        .setFallbackUri("forward:/fallback/users"))
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods(HttpMethod.GET))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())))
                .uri("lb://user-service"))

            // Order service
            .route("order-service", r -> r
                .path("/api/v1/orders/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Gateway", "true")
                    .addResponseHeader("X-Response-Time", "gateway"))
                .uri("lb://order-service"))

            // Admin routes — authentication header check
            .route("admin-service", r -> r
                .path("/api/v1/admin/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .filter((exchange, chain) -> {
                        String token = exchange.getRequest()
                            .getHeaders().getFirst("Authorization");
                        if (token == null || !token.startsWith("Bearer ")) {
                            exchange.getResponse().setStatusCode(
                                org.springframework.http.HttpStatus.UNAUTHORIZED);
                            return exchange.getResponse().setComplete();
                        }
                        return chain.filter(exchange);
                    }))
                .uri("lb://admin-service"))

            // WebSocket route
            .route("notification-service", r -> r
                .path("/ws/notifications/**")
                .uri("lb:ws://notification-service"))

            .build();
    }

    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, Duration.ofSeconds(1));
    }
}
```

```yaml
spring:
  cloud:
    gateway:
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin
      globalcors:
        cors-configurations:
          '[/**]':
            allowed-origins: "https://app.example.com"
            allowed-methods: "*"
            allowed-headers: "*"
            allow-credentials: true
      routes:
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/v1/products/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 10
                redis-rate-limiter.burstCapacity: 20
```

## Spring Cloud Config Server

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```yaml
server:
  port: 8888

spring:
  profiles:
    active: native  # or git
  cloud:
    config:
      server:
        native:
          search-locations: classpath:/config-repo/
        # Git-backed configuration:
        # git:
        #   uri: https://github.com/org/config-repo.git
        #   default-label: main
        #   search-paths: '{application}'
```

**Config client bootstrap.yml:**

```yaml
spring:
  application:
    name: user-service
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 5
        multiplier: 1.5
```

## OpenFeign Client

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

```java
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import java.util.Optional;

@SpringBootApplication
@EnableFeignClients
public class OrderServiceApplication {
    // ...
}
```

```java
@FeignClient(
    name = "user-service",
    url = "${services.user-service.url}",     // direct URL (optional)
    path = "/api/v1/users",
    fallbackFactory = UserClientFallbackFactory.class,
    configuration = FeignClientConfig.class)
public interface UserServiceClient {

    @GetMapping("/{id}")
    UserResponse getUser(@PathVariable Long id);

    @GetMapping
    List<UserResponse> getUsers(@RequestParam("ids") List<Long> ids);

    @GetMapping("/search")
    Page<UserResponse> searchUsers(@RequestParam("name") String name,
                                    @RequestParam("page") int page,
                                    @RequestParam("size") int size);

    @PostMapping
    UserResponse createUser(@RequestBody CreateUserRequest request);
}
```

```java
import feign.codec.ErrorDecoder;
import feign.Logger;
import org.springframework.context.annotation.Bean;

public class FeignClientConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return (methodKey, response) -> {
            int status = response.status();
            return switch (status) {
                case 404 -> new ResourceNotFoundException("Resource not found");
                case 409 -> new DuplicateResourceException("Resource conflict");
                default -> new RuntimeException("Feign error: " + status);
            };
        };
    }

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // NONE, BASIC, HEADERS, FULL
    }
}
```

```java
@Component
public class UserClientFallbackFactory
        implements FallbackFactory<UserServiceClient> {

    @Override
    public UserServiceClient create(Throwable cause) {
        return new UserServiceClient() {
            @Override
            public UserResponse getUser(Long id) {
                log.warn("Fallback for getUser({}): {}", id, cause.getMessage());
                return null;
            }

            @Override
            public List<UserResponse> getUsers(List<Long> ids) {
                return List.of();
            }

            @Override
            public Page<UserResponse> searchUsers(String name, int page, int size) {
                return Page.empty();
            }

            @Override
            public UserResponse createUser(CreateUserRequest request) {
                throw new ServiceUnavailableException("User service unavailable");
            }
        };
    }
}
```

## Load Balancing (Spring Cloud LoadBalancer)

```yaml
spring:
  cloud:
    loadbalancer:
      cache:
        enabled: true
        ttl: 10s
      retry:
        enabled: true
        max-retries-on-same-service-instance: 0
        max-retries-on-next-service-instance: 3
      health-check:
        path: /actuator/health
        initial-delay: 5000
        interval: 10000
```

```java
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class LoadBalancerConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
```

```java
@Service
public class OrderOrchestrator {

    private final RestClient.Builder restClientBuilder;

    public OrderOrchestrator(RestClient.Builder restClientBuilder) {
        this.restClientBuilder = restClientBuilder;
    }

    public UserResponse getUser(Long userId) {
        return restClientBuilder.build()
            .get()
            .uri("http://user-service/api/v1/users/{id}", userId)
            .retrieve()
            .body(UserResponse.class);
    }

    public List<ProductResponse> getProducts(List<Long> ids) {
        return restClientBuilder.build()
            .get()
            .uri("http://inventory-service/api/v1/products?ids={ids}", ids)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }
}
```

## Circuit Breaker (Resilience4j)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s
        permitted-number-of-calls-in-half-open-state: 3
        minimum-number-of-calls: 5
        automatic-transition-from-open-to-half-open-enabled: true
        record-exceptions:
          - java.io.IOException
          - java.net.ConnectException
        ignore-exceptions:
          - com.example.ResourceNotFoundException
    instances:
      user-service:
        base-config: default
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        exponential-backoff-multiplier: 2
    instances:
      user-service-retry:
        base-config: default
  bulkhead:
    configs:
      default:
        max-concurrent-calls: 10
        max-wait-duration: 500ms
    instances:
      user-service-bulkhead:
        base-config: default
  timelimiter:
    configs:
      default:
        timeout-duration: 2s
        cancel-running-future: true
```

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Service
public class ResilienceUserService {

    private final UserServiceClient userServiceClient;

    public ResilienceUserService(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "getUserFallback")
    @Retry(name = "user-service-retry")
    @Bulkhead(name = "user-service-bulkhead")
    public UserResponse getUser(Long userId) {
        return userServiceClient.getUser(userId);
    }

    public UserResponse getUserFallback(Long userId, Throwable t) {
        log.warn("Circuit breaker triggered for userId {}: {}", userId, t.getMessage());
        return new UserResponse(userId, "Unknown", "unavailable@example.com");
    }

    @CircuitBreaker(name = "user-service")
    @TimeLimiter(name = "default")
    public CompletableFuture<UserResponse> getUserAsync(Long userId) {
        return CompletableFuture.supplyAsync(() -> userServiceClient.getUser(userId));
    }

    @Retry(name = "user-service-retry")
    public void updateUser(Long userId, UpdateUserRequest request) {
        userServiceClient.updateUser(userId, request);
    }
}
```

## Distributed Tracing (Micrometer Tracing)

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

```yaml
management:
  tracing:
    enabled: true
    sampling:
      probability: 1.0   # 100% in dev; 0.1 = 10% in prod
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans

logging:
  pattern:
    level: "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]"
```
