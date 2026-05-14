# Core Java Advanced — Lesson 9: CompletableFuture & Async Programming

## What is CompletableFuture?

`CompletableFuture<T>` is Java's modern approach to **asynchronous programming** — running tasks without blocking threads.

```
Synchronous (blocking):               Asynchronous (non-blocking):
┌─────────────────────────┐           ┌─────────────────────────┐
│ Thread:                 │           │ Thread:                 │
│ callAPI() ──BLOCKED──▶  │           │ callAPIAsync()          │
│                     ◀───│           │  return CF              │
│ doNextThing()           │           │                        │
│                         │           │ (thread is FREE!)       │
└─────────────────────────┘           │                        │
                                       │ When API responds:      │
                                       │ trigger callback        │
                                       └─────────────────────────┘
```

## Creating CompletableFuture

```java
// Already completed
CompletableFuture<String> done = CompletableFuture.completedFuture("result");

// Run async (no return value)
CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
    System.out.println("Running in: " + Thread.currentThread().getName());
});

// Supply async (with return value)
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "Hello from " + Thread.currentThread().getName();
});

// Custom thread pool
ExecutorService executor = Executors.newFixedThreadPool(4);
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    return "Custom pool";
}, executor);
```

## Getting Results

```java
CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
    try { Thread.sleep(1000); } catch (InterruptedException e) {}
    return "Result";
});

// Block and get (defeats the purpose!)
String result = future.get();                     // Blocks until done
String result = future.get(5, TimeUnit.SECONDS);  // Timeout

// Non-blocking — callback when done (PREFERRED)
future.thenAccept(result -> System.out.println("Got: " + result));

// Join (like get, but throws unchecked exception)
String result = future.join();
```

## Chaining CompletableFuture

```java
CompletableFuture.supplyAsync(() -> "Hello")
    .thenApply(s -> s + " World")           // Transform result
    .thenApply(String::toUpperCase)          // Another transform
    .thenAccept(System.out::println)         // Consume final result
    .thenRun(() -> System.out.println("Done!"));  // Run after

// thenApply vs thenAccept vs thenRun:
// thenApply  — returns T → returns CompletableFuture<U> (transform)
// thenAccept — returns T → CompletableFuture<Void> (consume)
// thenRun    — just runs, no input → CompletableFuture<Void>
```

## Composing Futures

```java
// thenCompose — flatMap for futures (one depends on another)
CompletableFuture.supplyAsync(() -> getUserById(1))
    .thenCompose(user -> fetchOrders(user.getId()))  // Returns CF
    .thenAccept(orders -> System.out.println(orders));

// thenCombine — combine TWO independent futures
CompletableFuture<String> future1 = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> "World");

future1.thenCombine(future2, (a, b) -> a + " " + b)
       .thenAccept(System.out::println);  // "Hello World"
```

## Handling Errors

```java
CompletableFuture.supplyAsync(() -> {
    if (Math.random() > 0.5) throw new RuntimeException("Failed!");
    return "Success";
})
.exceptionally(ex -> {  // Recover from error
    System.err.println("Error: " + ex.getMessage());
    return "Fallback result";
})
.thenAccept(System.out::println);

// Handle both success and failure
.handle((result, ex) -> {
    if (ex != null) {
        return "Recovered from: " + ex.getMessage();
    }
    return result;
});
```

## Multiple Futures

```java
// allOf — wait for ALL to complete
CompletableFuture<String> f1 = CompletableFuture.supplyAsync(() -> "a");
CompletableFuture<String> f2 = CompletableFuture.supplyAsync(() -> "b");
CompletableFuture<String> f3 = CompletableFuture.supplyAsync(() -> "c");

CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2, f3);
all.thenRun(() -> System.out.println("All done!"));

// anyOf — wait for the FIRST to complete
CompletableFuture<Object> first = CompletableFuture.anyOf(f1, f2, f3);
first.thenAccept(result -> System.out.println("First done: " + result));
```

## Real-World Example: Parallel API Calls

```java
public class UserDashboardService {
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    public CompletableFuture<UserDashboard> getDashboard(long userId) {
        CompletableFuture<User> userFuture =
            CompletableFuture.supplyAsync(() -> fetchUser(userId), executor);
        CompletableFuture<List<Order>> ordersFuture =
            CompletableFuture.supplyAsync(() -> fetchOrders(userId), executor);
        CompletableFuture<List<Product>> recommendationsFuture =
            CompletableFuture.supplyAsync(() -> getRecommendations(userId), executor);

        return userFuture.thenCombine(ordersFuture, (user, orders) ->
            new UserDashboard(user, orders, null))
            .thenCombine(recommendationsFuture, (dashboard, recs) ->
                new UserDashboard(dashboard.user(), dashboard.orders(), recs));
    }
}
```

## Timeouts (Java 9+)

```java
CompletableFuture.supplyAsync(() -> {
        Thread.sleep(10000);  // Slow!
        return "Result";
    })
    .completeOnTimeout("Timeout!", 2, TimeUnit.SECONDS)  // Fallback after 2s
    .thenAccept(System.out::println);

// orTimeout — throws TimeoutException
CompletableFuture.supplyAsync(() -> {
        Thread.sleep(10000);
        return "Result";
    })
    .orTimeout(2, TimeUnit.SECONDS)
    .exceptionally(ex -> "Timed out: " + ex.getMessage())
    .thenAccept(System.out::println);
```

---

### Exercises

1. Use `CompletableFuture.supplyAsync` to fetch data from 3 different APIs in parallel, then combine results.
2. Create a chain: `supplyAsync → thenApply → thenApply → thenAccept`.
3. Handle an error in a CompletableFuture chain using `exceptionally`.
4. Use `allOf` to wait for multiple database queries, then process all results together.
5. Build an async image processing pipeline: load → resize → add watermark → save, all using CompletableFuture.
