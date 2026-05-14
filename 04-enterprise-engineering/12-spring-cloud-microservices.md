# Enterprise Engineering — Lesson 12: Spring Cloud & Microservices

## Why Microservices?

```
Monolith:                          Microservices:
┌──────────────────────┐           ┌──────┐ ┌──────┐ ┌──────┐
│   ENTIRE APP         │           │Users │ │Orders│ │Payment│
│                      │           │Service││Service││Service│
│  ┌────┐ ┌────┐      │           └──┬───┘ └──┬───┘ └──┬───┘
│  │User│ │Order│      │              │         │         │
│  └────┘ └────┘      │              ├─────────┴─────────┤
│  ┌────┐ ┌────┐      │              │   API Gateway     │
│  │Pay │ │Ship│      │              └────────┬──────────┘
│  └────┘ └────┘      │                       │
│                      │                   ┌───▼───┐
│  ONE deploy, ONE     │                   │Client │
│  tech stack, SCALE   │                   └───────┘
│  ENTIRE app          │
└──────────────────────┘
```

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| Deployment | One big deploy | Each service independently |
| Scaling | Scale everything | Scale only what's needed |
| Failure | One bug kills all | Isolated failures |
| Tech stack | One language | Polyglot (Java, Go, Python...) |
| Team size | One big team | Small, focused teams |
| Complexity | Simple code, complex scaling | Complex code, simple scaling |

## Spring Cloud Components

```
┌─────────────────────────────────────────────────────────────┐
│                     SPRING CLOUD ECOSYSTEM                    │
│                                                               │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │Eureka      │  │Config      │  │API Gateway (Spring   │   │
│  │(Service    │  │Server      │  │Cloud Gateway)        │   │
│  │ Discovery) │  │(External   │  │Routing, Filtering,   │   │
│  └────────────┘  │ Config)    │  │Rate Limiting         │   │
│                   └────────────┘  └──────────────────────┘   │
│  ┌────────────┐  ┌────────────┐  ┌──────────────────────┐   │
│  │Resilience4j│  │Sleuth/Zipkin│  │Spring Cloud Bus      │   │
│  │(Circuit    │  │(Distributed│  │(Config refresh,      │   │
│  │ Breaker)   │  │ Tracing)   │  │ messaging)           │   │
│  └────────────┘  └────────────┘  └──────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Service Discovery (Eureka)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

### Eureka Server

```java
@SpringBootApplication
@EnableEurekaServer
public class ServiceRegistryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceRegistryApplication.class, args);
    }
}
```

```properties
server.port=8761
eureka.client.register-with-eureka=false
eureka.client.fetch-registry=false
```

### Eureka Client

```java
@SpringBootApplication
@EnableDiscoveryClient  // Auto-configured, can be omitted
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
```

```properties
spring.application.name=user-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```

## API Gateway (Spring Cloud Gateway)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```

```java
@SpringBootApplication
public class GatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service          # Load-balanced via Eureka
          predicates:
            - Path=/api/users/**
          filters:
            - StripPrefix=1
            - name: RequestRateLimiter
              args:
                redis-rate-limiter.replenishRate: 100
                redis-rate-limiter.burstCapacity: 200
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - StripPrefix=1
```

## Centralized Configuration (Config Server)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```

```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```

```properties
server.port=8888
spring.cloud.config.server.git.uri=https://github.com/company/config-repo
```

### Client Configuration

```properties
spring.config.import=configserver:http://localhost:8888
spring.cloud.config.name=user-service
spring.cloud.config.profile=prod
```

## Circuit Breaker (Resilience4j)

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
```

```java
@Service
public class OrderServiceClient {

    @CircuitBreaker(name = "orderService", fallbackMethod = "getDefaultOrders")
    public List<Order> getUserOrders(Long userId) {
        return restTemplate.getForObject(
            "http://order-service/api/orders/user/" + userId,
            List.class
        );
    }

    public List<Order> getDefaultOrders(Long userId, Throwable t) {
        log.warn("Order service unavailable, returning empty orders", t);
        return List.of();
    }
}
```

```properties
resilience4j.circuitbreaker.instances.orderService.sliding-window-size=10
resilience4j.circuitbreaker.instances.orderService.minimum-number-of-calls=5
resilience4j.circuitbreaker.instances.orderService.failure-rate-threshold=50
resilience4j.circuitbreaker.instances.orderService.wait-duration-in-open-state=30s
```

## Inter-Service Communication

```
┌──────────┐     HTTP/REST (Synchronous)    ┌──────────┐
│ Service A ├───────────────────────────────▶│ Service B │
└──────────┘     Request-Response, simple    └──────────┘
                                                
┌──────────┐     Message Queue (Async)        ┌──────────┐
│ Service A ├────────────────────────────────▶│ Service B │
└──────────┘     Event-driven, decoupled     └──────────┘
```

### Synchronous: RestTemplate / WebClient

```java
// RestTemplate (Blocking)
List<Order> orders = restTemplate.getForObject(
    "http://order-service/api/orders/user/{userId}",
    List.class, userId
);

// WebClient (Reactive, Non-blocking)
Flux<Order> orders = webClient.get()
    .uri("http://order-service/api/orders/user/{userId}", userId)
    .retrieve()
    .bodyToFlux(Order.class);
```

### Asynchronous: RabbitMQ/Kafka

```java
@Service
public class OrderEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent("ORDER_CREATED", order);
        rabbitTemplate.convertAndSend("order.exchange", "order.created", event);
    }
}

@Service
public class InventoryService {

    @RabbitListener(queues = "inventory.order.queue")
    public void handleOrderCreated(OrderEvent event) {
        // Reserve inventory items
        log.info("Reserving inventory for order: {}", event.getOrder().getId());
    }
}
```

## Distributed Tracing (Micrometer)

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

```properties
management.tracing.sampling.probability=1.0  # Trace ALL requests
```

Every service auto-propagates trace IDs. A single request across 5 services produces one unified trace.

## Microservices Patterns

| Pattern | Problem Solved | Spring Cloud Solution |
|---------|---------------|----------------------|
| Service Discovery | Services need to find each other | Eureka / Consul |
| API Gateway | Single entry point, cross-cutting concerns | Spring Cloud Gateway |
| Configuration Management | Externalize config per environment | Config Server |
| Circuit Breaker | Prevent cascading failures | Resilience4j |
| Distributed Tracing | Debug requests across services | Micrometer + Zipkin |
| Event-Driven Comms | Loose coupling between services | RabbitMQ / Kafka |
| Centralized Logging | Aggregate logs from all services | ELK / Loki |
| Health Checks | Monitor service health | Actuator + Eureka |

## When NOT to Use Microservices

- Small team (< 5 developers)
- Simple CRUD application
- No need for independent scaling
- Startup building MVP (speed > scalability)
- Team lacks DevOps experience

Start with a **modular monolith**. Extract services as needed.

## Exercises

1. Set up Eureka server and register two Spring Boot services.
2. Configure Spring Cloud Gateway to route requests to both services.
3. Add a circuit breaker with Resilience4j for inter-service calls.
4. Implement distributed tracing with Micrometer and Zipkin.
5. Set up a Config Server with a Git-backed configuration repository.
