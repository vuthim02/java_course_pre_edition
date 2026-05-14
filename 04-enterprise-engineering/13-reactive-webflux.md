# Enterprise Engineering — Lesson 13: Reactive Programming with WebFlux

## Why Reactive?

Traditional Spring MVC allocates **one thread per request**. Under high load, threads block on I/O (database, API calls, file reads). Blocked threads waste memory and limit throughput.

```
Spring MVC (Thread-per-Request):          Spring WebFlux (Event Loop):
┌──────────────────────────────────┐      ┌──────────────────────────────────┐
│ Request 1 ──▶ Thread 1 (BLOCKED) │      │ Request 1 ──▶                    │
│ Request 2 ──▶ Thread 2 (BLOCKED) │      │ Request 2 ──▶  Event Loop        │
│ Request 3 ──▶ Thread 3 (WAITING) │      │ Request 3 ──▶  (NON-BLOCKING)    │
│ Request 4 ──▶ Thread 4 (BLOCKED) │      │ Request 4 ──▶                    │
│ Request 5 ──▶ Thread 5 (BLOCKED) │      │ Request 5 ──▶                    │
├──────────────────────────────────┤      ├──────────────────────────────────┤
│ 200 threads = 200 concurrent max │      │ Few threads handle MILLIONS of   │
│ Thread stack: ~1MB each = 200MB  │      │ concurrent requests (asynchronous)│
└──────────────────────────────────┘      └──────────────────────────────────┘
```

## Reactive Streams Specification

```
┌────────────┐          ┌────────────┐
│ Publisher   │────subscribe()─────▶│ Subscriber │
│ (Produces   │                      │ (Consumes  │
│  data)      │◀─────onSubscribe────│   data)    │
└────────────┘                      └────────────┘
       │                                  │
       │─────request(n) (backpressure)────▶│
       │                                  │
       │─────onNext(item)────────────────▶│
       │─────onNext(item)────────────────▶│
       │─────onComplete()────────────────▶│
       │─────onError(e)──────────────────▶│
```

**Backpressure:** The subscriber tells the publisher how much data it can handle. This prevents overwhelming slow consumers.

## Project Reactor — The Foundation

Reactor provides two reactive types:

```java
// Mono — 0 or 1 item (like Optional)
Mono<String> mono = Mono.just("hello");
Mono<String> empty = Mono.empty();
Mono<String> error = Mono.error(new RuntimeException("oops"));

// Flux — 0 to N items (like Stream)
Flux<String> flux = Flux.just("a", "b", "c");
Flux<Integer> range = Flux.range(1, 100);
Flux<String> fromStream = Flux.fromStream(Stream.of("x", "y", "z"));
```

## Spring WebFlux

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

### Reactive Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();  // Returns Flux<User>
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<User>> getUserById(@PathVariable String id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<User>> createUser(@RequestBody User user) {
        return userService.save(user)
            .map(saved -> ResponseEntity.created(
                URI.create("/api/users/" + saved.getId())
            ).body(saved));
    }
}
```

### Reactive Repository

```java
// With R2DBC (reactive relational database)
public interface UserRepository extends ReactiveCrudRepository<User, Long> {
    Flux<User> findByLastName(String lastName);
    Mono<User> findByEmail(String email);
}

// With MongoDB (natively reactive)
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Flux<User> findByAgeGreaterThan(int age);
}
```

## Operators

### Transforming

```java
Flux<User> adults = users
    .filter(user -> user.getAge() >= 18)
    .map(user -> user.withAdult(true))
    .flatMap(this::enrichWithOrders);  // 1 user → Flux<Order> then flatten
```

### Combining

```java
// Zip — combine items pairwise
Mono<UserWithOrders> combined = Mono.zip(
    userService.findById(1L),
    orderService.findByUserId(1L).collectList(),
    (user, orders) -> new UserWithOrders(user, orders)
);

// Merge — interleave streams
Flux<String> merged = Flux.merge(
    serviceA.findAll().map(Object::toString),
    serviceB.findAll().map(Object::toString)
);
```

### Error Handling

```java
Flux<User> safeUsers = userService.findAll()
    .onErrorResume(e -> {
        log.error("Failed to fetch users", e);
        return Flux.empty();  // Return empty on error
    })
    .timeout(Duration.ofSeconds(5))
    .retry(3);  // Retry on failure
```

### Backpressure

```java
userService.findAll()
    .onBackpressureBuffer(100)       // Buffer up to 100 items
    .onBackpressureDrop()             // Drop excess items
    .onBackpressureLatest()           // Keep only latest
    .subscribe(System.out::println);
```

## WebClient (Reactive HTTP Client)

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("http://user-service")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}

@Service
public class UserServiceClient {

    private final WebClient webClient;

    public UserServiceClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<User> getUsers() {
        return webClient.get()
            .uri("/api/users")
            .retrieve()
            .bodyToFlux(User.class);
    }

    public Mono<User> getUserById(Long id) {
        return webClient.get()
            .uri("/api/users/{id}", id)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, response ->
                Mono.error(new UserNotFoundException("User not found: " + id))
            )
            .bodyToMono(User.class);
    }
}
```

## WebFlux vs MVC

| Aspect | Spring MVC | Spring WebFlux |
|--------|-----------|----------------|
| Model | Thread-per-request | Event loop |
| Concurrency | Thread pool (blocking) | Non-blocking I/O |
| Database | JDBC / JPA (blocking) | R2DBC / MongoDB Driver (reactive) |
| Performance | Good for CPU-heavy | Excellent for I/O-heavy |
| Learning curve | Easier | Steeper |
| When to use | Standard CRUD, CPU-bound | High concurrency, I/O-heavy |

## Reactive Transactions

```java
@Service
public class OrderService {

    @Transactional
    public Mono<Order> createOrder(Order order) {
        return orderRepository.save(order)
            .flatMap(saved ->
                inventoryService.reserveItems(saved)
                    .thenReturn(saved)
            );
    }
}
```

## Testing Reactive Code

```java
import reactor.test.StepVerifier;

@Test
void shouldReturnAllUsers() {
    Flux<User> users = userController.getAllUsers();

    StepVerifier.create(users)
        .expectNextMatches(u -> u.getName().equals("Alice"))
        .expectNextMatches(u -> u.getName().equals("Bob"))
        .expectComplete()
        .verify();
}

@Test
void shouldHandleError() {
    Flux<User> result = brokenService.findAll()
        .onErrorResume(e -> Flux.just(new User("fallback")));

    StepVerifier.create(result)
        .expectNextMatches(u -> u.getName().equals("fallback"))
        .expectComplete()
        .verify();
}
```

## Reactive All The Way Down

⚠️ **Important:** Reactive benefits only apply when the ENTIRE chain is non-blocking:

```
✅ Correct:  Controller → Service → WebClient → R2DBC
             (all non-blocking, one event loop handles all)

❌ Wrong:    Controller → Service → JPA (blocking) → WebClient
             (blocking JPA kills event loop performance)
```

## Exercises

1. Create a WebFlux controller with CRUD endpoints using a reactive repository.
2. Use WebClient to call an external API reactively.
3. Combine data from two reactive sources using `Mono.zip`.
4. Implement backpressure handling with `onBackpressureBuffer`.
5. Write StepVerifier tests for a reactive flow.
