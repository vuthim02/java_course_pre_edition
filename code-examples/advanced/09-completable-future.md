# CompletableFuture and Asynchronous Programming

This document covers Java's `CompletableFuture` for asynchronous programming, including factory methods, transformation, composition, combining, error handling, and custom executors.

## SupplyAsync / RunAsync

```java
import java.util.concurrent.*;

public class FactoryMethodsDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- supplyAsync / runAsync ---");

        // runAsync — no return value, runs asynchronously
        CompletableFuture<Void> runFut = CompletableFuture.runAsync(() -> {
            System.out.println("  runAsync on: " + Thread.currentThread().getName());
        });
        runFut.get(); // wait for completion

        // supplyAsync — returns a value asynchronously
        CompletableFuture<String> supplyFut = CompletableFuture.supplyAsync(() -> {
            System.out.println("  supplyAsync on: " + Thread.currentThread().getName());
            return "Hello from the future!";
        });
        String result = supplyFut.get(); // blocks until done
        System.out.println("  Result: " + result);

        // Using custom executor (common pool is default)
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CompletableFuture<Integer> customFut = CompletableFuture.supplyAsync(() -> {
            System.out.println("  Custom executor: " + Thread.currentThread().getName());
            return 42;
        }, executor);
        System.out.println("  Custom result: " + customFut.get());
        executor.shutdown();
    }
}
```

## thenApply (Transform), thenAccept (Consume), thenRun (Run After)

```java
import java.util.concurrent.*;

public class ThenApplyAcceptRunDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- thenApply / thenAccept / thenRun ---");

        // thenApply — transform the result (like Stream.map)
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> "Hello")
            .thenApply(s -> s + " World")          // transforms String -> String
            .thenApply(String::toUpperCase);        // transforms again

        System.out.println("  thenApply result: " + future.get());

        // thenAccept — consume the result (no return value)
        CompletableFuture.supplyAsync(() -> "Data loaded")
            .thenAccept(data -> System.out.println("  thenAccept: " + data))
            .get(); // wait for completion

        // thenRun — run action after completion (ignores result)
        CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) {}
            return "some result";
        })
            .thenRun(() -> System.out.println("  thenRun: completed (result ignored)"))
            .get();
    }
}
```

## thenCompose (Async Pipeline Chaining)

```java
import java.util.concurrent.*;

public class ThenComposeDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- thenCompose (flatMap) ---");
        // thenCompose: chains async operations where each returns a CompletableFuture
        // Avoids nested CompletableFuture<CompletableFuture<...>>

        CompletableFuture<String> result = getUserName()
            .thenCompose(name -> getGreeting(name)) // flatMap — returns CompletableFuture
            .thenCompose(greeting -> addExclamation(greeting));

        System.out.println("  Composed result: " + result.get());

        // Compare with thenApply (would nest):
        // thenApply would give: CompletableFuture<CompletableFuture<String>>
        // thenCompose gives: CompletableFuture<String>
    }

    static CompletableFuture<String> getUserName() {
        return CompletableFuture.supplyAsync(() -> "Alice");
    }

    static CompletableFuture<String> getGreeting(String name) {
        return CompletableFuture.supplyAsync(() -> "Hello, " + name);
    }

    static CompletableFuture<String> addExclamation(String s) {
        return CompletableFuture.supplyAsync(() -> s + "!");
    }
}
```

## thenCombine (Combine Two Futures)

```java
import java.util.concurrent.*;

public class ThenCombineDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- thenCombine ---");
        // thenCombine: wait for two independent futures and combine their results

        CompletableFuture<Integer> priceFut = getPriceAsync();
        CompletableFuture<Integer> quantityFut = getQuantityAsync();

        CompletableFuture<Integer> totalFut = priceFut.thenCombine(quantityFut,
            (price, quantity) -> price * quantity
        );

        System.out.println("  Total cost: $" + totalFut.get() + " (combine)");

        // thenAcceptBoth — consume both results without returning
        CompletableFuture.supplyAsync(() -> "Token-ABC")
            .thenAcceptBoth(
                CompletableFuture.supplyAsync(() -> "User-42"),
                (token, user) -> System.out.println("  thenAcceptBoth: " + user + " has " + token)
            ).get();
    }

    static CompletableFuture<Integer> getPriceAsync() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return 25;
        });
    }

    static CompletableFuture<Integer> getQuantityAsync() {
        return CompletableFuture.supplyAsync(() -> {
            sleep(200);
            return 3;
        });
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
```

## allOf / anyOf (Multiple Futures)

```java
import java.util.concurrent.*;

public class AllOfAnyOfDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- allOf / anyOf ---");

        // allOf — wait for ALL to complete
        CompletableFuture<String> fut1 = CompletableFuture.supplyAsync(() -> "Result-1");
        CompletableFuture<String> fut2 = CompletableFuture.supplyAsync(() -> "Result-2");
        CompletableFuture<String> fut3 = CompletableFuture.supplyAsync(() -> "Result-3");

        CompletableFuture<Void> allDone = CompletableFuture.allOf(fut1, fut2, fut3);
        allDone.get(); // blocks until all complete

        // Collect results (allOf returns Void, so gather manually)
        String combined = fut1.get() + ", " + fut2.get() + ", " + fut3.get();
        System.out.println("  allOf combined: " + combined);

        // anyOf — wait for the FIRST to complete
        CompletableFuture<String> fast = CompletableFuture.supplyAsync(() -> {
            sleep(100);
            return "Fast result";
        });
        CompletableFuture<String> slow = CompletableFuture.supplyAsync(() -> {
            sleep(300);
            return "Slow result";
        });

        Object firstResult = CompletableFuture.anyOf(fast, slow).get();
        System.out.println("  anyOf (first completed): " + firstResult);

        // CompletableFuture.allOf with stream
        java.util.List<CompletableFuture<String>> futures = java.util.List.of(fut1, fut2, fut3);
        CompletableFuture<Void> all = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );
        all.thenRun(() -> System.out.println("  All futures completed"));
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
```

## Error Handling: exceptionally, handle, whenComplete

```java
import java.util.concurrent.*;

public class ErrorHandlingDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- exceptionally ---");
        // Recover from a specific exception — provides fallback value
        CompletableFuture<String> safe = CompletableFuture.supplyAsync(() -> {
            if (Math.random() > 0.3) throw new RuntimeException("API failed");
            return "API response";
        }).exceptionally(ex -> {
            System.err.println("  exceptionally: " + ex.getMessage());
            return "Fallback response";
        });
        System.out.println("  exceptionally result: " + safe.get());

        System.out.println("\n--- handle ---");
        // Handle both success and failure (always called)
        CompletableFuture<String> handled = CompletableFuture.supplyAsync(() -> {
            return "42";
        }).handle((result, ex) -> {
            if (ex != null) {
                System.err.println("  handle failed: " + ex.getMessage());
                return "0";
            }
            return "Parsed: " + result;
        });
        System.out.println("  handle result: " + handled.get());

        System.out.println("\n--- whenComplete ---");
        // Called when future completes (success or fail), does not alter result
        CompletableFuture<String> completed = CompletableFuture.supplyAsync(() -> "Data")
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("  whenComplete: success with " + result);
                } else {
                    System.err.println("  whenComplete: failed with " + ex.getMessage());
                }
            });
        System.out.println("  whenComplete result: " + completed.get());
    }
}
```

## Custom Executor

```java
import java.util.concurrent.*;

public class CustomExecutorDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Custom Executor ---");
        // Default is ForkJoinPool.commonPool() — shared across all CompletableFutures
        // Custom executor isolates workloads and controls parallelism

        ExecutorService cpuTasks = Executors.newFixedThreadPool(4);
        ExecutorService ioTasks = Executors.newCachedThreadPool();

        CompletableFuture<String> future = CompletableFuture
            .supplyAsync(() -> {
                // CPU-intensive task
                System.out.println("  CPU task on: " + Thread.currentThread().getName());
                return "processed data";
            }, cpuTasks)
            .thenApplyAsync(data -> {
                // I/O task (e.g., database call)
                System.out.println("  I/O task on: " + Thread.currentThread().getName());
                return data + " -> saved to DB";
            }, ioTasks);

        System.out.println("  Result: " + future.get());

        cpuTasks.shutdown();
        ioTasks.shutdown();
    }
}
```

## Future vs CompletableFuture

```java
import java.util.concurrent.*;

public class FutureComparisonDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- Future vs CompletableFuture ---");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        // Future (Java 5) — limited, blocking
        Future<String> future = executor.submit(() -> {
            Thread.sleep(500);
            return "Future result";
        });

        // Blocking call — no way to compose or transform
        String futureResult = future.get();
        System.out.println("  Future.get(): " + futureResult);

        // CompletableFuture (Java 8+) — rich composable API
        CompletableFuture<String> cf = CompletableFuture.supplyAsync(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            return "CF result";
        });

        // Non-blocking composition
        cf.thenApply(String::toUpperCase)
          .thenAccept(r -> System.out.println("  CF transformed: " + r));

        // Wait for completion
        cf.get();

        // Comparison summary:
        // Future:        get() blocks, no chaining, no error handling, no manual completion
        // CompletableFuture: non-blocking, chaining, compose, combine, error handling,
        //                    manual completion (complete/completeExceptionally)

        executor.shutdown();
    }
}
```
