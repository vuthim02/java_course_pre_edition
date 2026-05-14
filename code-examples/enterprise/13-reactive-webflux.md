# Reactive Programming: WebFlux

Spring WebFlux provides a reactive, non-blocking alternative to Spring MVC using Project Reactor. It is ideal for high-concurrency, low-latency applications with streaming data or long-lived connections.

## Mono and Flux Creation

```java
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.function.Tuple2;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

public class ReactiveCreationExample {

    // Mono — 0 or 1 item
    Mono<String> empty = Mono.empty();
    Mono<String> single = Mono.just("hello");
    Mono<String> fromNullable = Mono.justOrNull(null);
    Mono<String> fromCallable = Mono.fromCallable(() -> "computed");

    // Flux — 0 to N items
    Flux<String> just = Flux.just("a", "b", "c");
    Flux<Integer> range = Flux.range(1, 10);
    Flux<String> fromArray = Flux.fromArray(new String[]{"x", "y", "z"});
    Flux<String> fromIterable = Flux.fromIterable(List.of("one", "two", "three"));
    Flux<Integer> fromStream = Flux.fromStream(Stream.of(1, 2, 3));

    // Infinite streams
    Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));

    // Defer — lazy creation
    Mono<String> deferred = Mono.defer(() ->
        Mono.just(computeExpensiveValue()));

    // Using Callable for blocking operations
    Mono<String> blocking = Mono.fromCallable(() -> {
        Thread.sleep(100); // blocking call
        return "result";
    }).subscribeOn(Schedulers.boundedElastic());

    // Error creation
    Mono<String> error = Mono.error(new RuntimeException("oops"));
    Flux<String> errorFlux = Flux.error(new RuntimeException("flux error"));

    // Combining
    Flux<String> merged = Flux.merge(
        Flux.just("a", "b"),
        Flux.just("c", "d"));

    Flux<String> zipped = Flux.zip(
        Flux.just("A", "B", "C"),
        Flux.just(1, 2, 3),
        (letter, number) -> letter + number);

    private String computeExpensiveValue() {
        return "expensive";
    }
}
```

## WebFlux — Functional Endpoints

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.*;
import reactor.core.publisher.Mono;

@Configuration
public class UserRouterConfig {

    @Bean
    public RouterFunction<ServerResponse> userRoutes(UserHandler handler) {
        return RouterFunctions
            .route()
            .path("/api/v1/users", builder -> builder
                .GET("", handler::getAllUsers)
                .GET("/{id}", handler::getUserById)
                .GET("/search", handler::searchUsers)
                .POST("", handler::createUser)
                .PUT("/{id}", handler::updateUser)
                .DELETE("/{id}", handler::deleteUser)
                .GET("/stream", handler::streamUsers))
            .build();
    }
}

@Component
public class UserHandler {

    private final ReactiveUserRepository userRepository;

    public UserHandler(ReactiveUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<ServerResponse> getAllUsers(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("20"));

        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .body(userRepository.findAll()
                .skip((long) page * size)
                .take(size), UserResponse.class);
    }

    public Mono<ServerResponse> getUserById(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return userRepository.findById(id)
            .flatMap(user -> ServerResponse.ok()
                .bodyValue(user))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> searchUsers(ServerRequest request) {
        String name = request.queryParam("name").orElse("");

        return ServerResponse.ok()
            .body(userRepository.findByNameContaining(name), UserResponse.class);
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
            .flatMap(userRepository::save)
            .flatMap(saved -> ServerResponse
                .created(java.net.URI.create("/api/v1/users/" + saved.id()))
                .bodyValue(saved));
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return request.bodyToMono(UpdateUserRequest.class)
            .flatMap(update -> userRepository.findById(id)
                .flatMap(existing -> {
                    User updated = existing.withName(update.name())
                                           .withEmail(update.email());
                    return userRepository.save(updated);
                }))
            .flatMap(user -> ServerResponse.ok().bodyValue(user))
            .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return userRepository.deleteById(id)
            .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> streamUsers(ServerRequest request) {
        return ServerResponse.ok()
            .contentType(MediaType.TEXT_EVENT_STREAM)
            .body(userRepository.findAll(), UserResponse.class);
    }
}
```

## WebFlux — Annotated Controllers

```java
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import java.time.Duration;
import java.time.Instant;

@RestController
@RequestMapping("/api/v2/users")
public class UserReactiveController {

    private final ReactiveUserRepository userRepository;

    // Sink for broadcasting events
    private final Sinks.Many<ServerSentEvent<UserResponse>> userEventSink =
        Sinks.many().multicast().onBackpressureBuffer();

    public UserReactiveController(ReactiveUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public Flux<UserResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return userRepository.findAll()
            .skip((long) page * size)
            .take(size);
    }

    @GetMapping("/{id}")
    public Mono<UserResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
            .switchIfEmpty(Mono.error(
                new ResourceNotFoundException("User", id)));
    }

    @PostMapping
    public Mono<UserResponse> createUser(@Valid @RequestBody Mono<CreateUserRequest> request) {
        return request
            .flatMap(userRepository::save)
            .doOnSuccess(user -> userEventSink.tryEmitNext(
                ServerSentEvent.builder(user)
                    .event("user-created")
                    .build()));
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UserResponse>> streamUsers() {
        return Flux.merge(
            // Periodic database polling
            userRepository.findAll()
                .map(u -> ServerSentEvent.builder(u)
                    .event("user-update")
                    .build()),
            // Real-time events
            userEventSink.asFlux()
        );
    }

    @GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> eventStream() {
        return Flux.interval(Duration.ofSeconds(1))
            .map(i -> ServerSentEvent.<String>builder()
                .event("tick")
                .data("Event #" + i + " at " + Instant.now())
                .build());
    }
}
```

## WebClient (Replacement for RestTemplate)

```java
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import java.time.Duration;

@Service
public class ReactiveUserClient {

    private final WebClient webClient;

    public ReactiveUserClient() {
        this.webClient = WebClient.builder()
            .baseUrl("http://user-service/api/v1/users")
            .defaultHeader("X-Client", "order-service")
            .defaultCookie("session", "abc123")
            .build();
    }

    public Mono<UserResponse> getUser(Long id) {
        return webClient.get()
            .uri("/{id}", id)
            .retrieve()
            .bodyToMono(UserResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof WebClientResponseException
                    && ((WebClientResponseException) throwable).getStatusCode().is5xxServerError()))
            .onErrorResume(e -> Mono.empty());
    }

    public Flux<UserResponse> getAllUsers() {
        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .queryParam("page", 0)
                .queryParam("size", 100)
                .build())
            .retrieve()
            .bodyToFlux(UserResponse.class);
    }

    public Mono<UserResponse> createUser(CreateUserRequest request) {
        return webClient.post()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .onStatus(status -> status.is4xxClientError(),
                response -> response.bodyToMono(String.class)
                    .map(body -> new RuntimeException("Client error: " + body)))
            .bodyToMono(UserResponse.class);
    }

    // Exchange (more control — access to response headers, status)
    public Mono<String> getUserWithHeaders(Long id) {
        return webClient.get()
            .uri("/{id}", id)
            .exchangeToMono(response -> {
                if (response.statusCode().is2xxSuccessful()) {
                    return response.bodyToMono(UserResponse.class)
                        .map(u -> u.name());
                }
                return response.createException()
                    .flatMap(Mono::error);
            });
    }

    // Streaming response
    public Flux<UserResponse> streamUsers() {
        return webClient.get()
            .uri("/stream")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(UserResponse.class);
    }
}
```

## Reactive MongoDB Repository

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
</dependency>
```

```java
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ReactiveUserRepository
        extends ReactiveMongoRepository<User, Long> {

    Flux<User> findByNameContaining(String name);

    Mono<User> findByEmail(String email);

    Flux<User> findByAgeGreaterThan(int age);
}
```

## Backpressure

```java
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class BackpressureExample {

    // Buffer — keeps items in memory when consumer is slow
    public Flux<String> bufferedStream() {
        return Flux.interval(Duration.ofMillis(10))
            .map(i -> "event-" + i)
            .onBackpressureBuffer(1000, // buffer size
                dropped -> System.out.println("Dropped: " + dropped));
    }

    // Drop — drops items when consumer can't keep up
    public Flux<String> dropStream() {
        return Flux.interval(Duration.ofMillis(10))
            .map(i -> "event-" + i)
            .onBackpressureDrop(dropped ->
                System.out.println("Dropped: " + dropped));
    }

    // Latest — keeps only the most recent item
    public Flux<String> latestStream() {
        return Flux.interval(Duration.ofMillis(10))
            .map(i -> "event-" + i)
            .onBackpressureLatest();
    }

    // Error — fails when consumer is too slow
    public Flux<String> errorOnOverflow() {
        return Flux.interval(Duration.ofMillis(10))
            .map(i -> "event-" + i)
            .onBackpressureError();
    }

    // Manual request control
    public void manualBackpressure() {
        Flux.range(1, 1000)
            .subscribe(new org.reactivestreams.Subscriber<>() {
                private org.reactivestreams.Subscription subscription;
                private int requested = 10;

                @Override
                public void onSubscribe(org.reactivestreams.Subscription s) {
                    this.subscription = s;
                    s.request(requested);
                }

                @Override
                public void onNext(Integer item) {
                    System.out.println("Processing: " + item);
                    requested--;
                    if (requested == 0) {
                        requested = 10;
                        subscription.request(10);
                    }
                }

                @Override
                public void onError(Throwable t) {
                    System.err.println("Error: " + t);
                }

                @Override
                public void onComplete() {
                    System.out.println("Done");
                }
            });
    }
}
```

## Schedulers

```java
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class SchedulerExample {

    public void demonstrateSchedulers() {
        Flux.range(1, 100)
            .map(i -> {
                System.out.println("Map on: " + Thread.currentThread().getName());
                return i * 2;
            })
            .publishOn(Schedulers.parallel()) // downstream runs on parallel scheduler
            .filter(i -> {
                System.out.println("Filter on: " + Thread.currentThread().getName());
                return i > 10;
            })
            .subscribeOn(Schedulers.boundedElastic()) // upstream runs here
            .subscribe();
    }

    // Common schedulers:
    // Schedulers.boundedElastic() — for blocking I/O (DB calls, file access)
    // Schedulers.parallel() — for CPU-intensive work (fixed pool = # of cores)
    // Schedulers.single() — single-threaded, for low-overhead tasks
    // Schedulers.immediate() — runs on current thread
    // Schedulers.fromExecutor(executor) — wrap a custom ExecutorService

    // Blocking call — must be offloaded
    public Flux<UserResponse> getUsersFromBlockingSource() {
        return Flux.defer(() ->
            Flux.fromIterable(blockingUserRepository.findAll()))
            .subscribeOn(Schedulers.boundedElastic());
    }

    // Parallel processing
    public Flux<Integer> parallelProcess() {
        return Flux.range(1, 1000)
            .parallel(10)
            .runOn(Schedulers.parallel())
            .map(this::intensiveCalculation)
            .sequential();
    }

    private Integer intensiveCalculation(Integer value) {
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        return value * value;
    }
}
```
