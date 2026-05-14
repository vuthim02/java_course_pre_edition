# Virtual Threads (Project Loom)

This document covers Java 21+ virtual threads including creation with `Thread.ofVirtual()`, `Executors.newVirtualThreadPerTaskExecutor()`, `StructuredTaskScope`, `ScopedValue`, and scalability comparisons with platform threads.

## Creating Virtual Threads

```java
import java.util.concurrent.*;

public class VirtualThreadBasicsDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Virtual Thread Creation ---");

        // Method 1: Thread.ofVirtual().start()
        Thread vt1 = Thread.ofVirtual()
            .name("vt-1")
            .start(() -> {
                System.out.println("  Virtual thread: " + Thread.currentThread());
                System.out.println("  Is virtual: " + Thread.currentThread().isVirtual());
            });
        vt1.join();

        // Method 2: Thread.startVirtualThread(Runnable)
        Thread vt2 = Thread.startVirtualThread(() -> {
            System.out.println("  startVirtualThread: " + Thread.currentThread().getName());
        });
        vt2.join();

        // Method 3: Executors.newVirtualThreadPerTaskExecutor()
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> result = executor.submit(() -> {
                Thread.sleep(100);
                return "Virtual thread result";
            });
            System.out.println("  Executor result: " + result.get());
        }

        System.out.println("\nAll virtual threads completed.");
    }
}
```

## Scalability: 100K Virtual Threads vs Platform Threads

```java
import java.util.concurrent.*;
import java.util.stream.*;

public class ScalabilityDemo {
    public static void main(String[] args) throws InterruptedException {
        int count = 100_000;

        System.out.println("--- 100K Virtual Threads Demo ---");
        long start = System.nanoTime();

        // Virtual threads — lightweight, can create millions
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch latch = new CountDownLatch(count);
            for (int i = 0; i < count; i++) {
                int id = i;
                executor.submit(() -> {
                    // Simulate a small task (e.g., HTTP call, DB query)
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                    latch.countDown();
                });
            }
            latch.await();
        }

        long elapsed = System.nanoTime() - start;
        System.out.println("  Created " + count + " virtual threads in "
            + TimeUnit.NANOSECONDS.toMillis(elapsed) + " ms");

        // For comparison, platform threads are limited by OS.
        // 100K platform threads would likely crash the JVM or OS.
        System.out.println("\n  NOTE: 100K platform threads would consume ~100GB+ of stack memory");
        System.out.println("  Virtual threads use ~1MB total for the same workload.");
        System.out.println("  Virtual threads are mounted on carrier platform threads (ForkJoinPool).");
    }
}
```

## StructuredTaskScope

```java
import java.util.concurrent.*;
import java.util.concurrent.StructuredTaskScope.*;

public class StructuredTaskScopeDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- StructuredTaskScope ---");
        // StructuredTaskScope ensures all subtasks complete before the scope closes.
        // If a subtask fails, other subtasks are cancelled automatically.

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            // Fork subtasks (run in virtual threads)
            Subtask<String> user = scope.fork(() -> fetchUser("user-123"));
            Subtask<Integer> orders = scope.fork(() -> fetchOrderCount("user-123"));
            Subtask<String> preferences = scope.fork(() -> fetchPreferences("user-123"));

            // Wait for all or fail on first failure
            scope.join();
            scope.throwIfFailed(ex -> new RuntimeException("Subtask failed", ex));

            // All subtasks completed successfully
            System.out.println("  User: " + user.get());
            System.out.println("  Orders: " + orders.get());
            System.out.println("  Preferences: " + preferences.get());
        }

        System.out.println("\n--- ShutdownOnSuccess ---");
        // Returns the first successful result, cancels others
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
            scope.fork(() -> { Thread.sleep(200); return "Result from slow"; });
            scope.fork(() -> { Thread.sleep(50);  return "Result from fast"; });
            scope.fork(() -> { Thread.sleep(300); return "Result from slowest"; });

            String first = scope.join().result();
            System.out.println("  First successful result: " + first);
        }
    }

    static String fetchUser(String userId) throws Exception {
        Thread.sleep(100);
        return "Alice (id=" + userId + ")";
    }

    static int fetchOrderCount(String userId) throws Exception {
        Thread.sleep(150);
        return 5;
    }

    static String fetchPreferences(String userId) throws Exception {
        Thread.sleep(80);
        return "theme=dark, lang=en";
    }
}
```

## ScopedValue (Java 21+)

```java
import java.util.concurrent.*;

// ScopedValue — inheritable, bounded context for virtual threads
// Replaces ThreadLocal for virtual threads (ThreadLocal has caveats with VTs)
public class ScopedValueDemo {
    // Declare a ScopedValue (like a ThreadLocal but scoped)
    private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
    private static final ScopedValue<String> USER_ROLE = ScopedValue.newInstance();

    public static void main(String[] args) throws Exception {
        System.out.println("--- ScopedValue ---");
        // ScopedValue is bound to a scope — it's available within the lambda
        // and all methods called from it. Unavailable outside the scope.

        ScopedValue.where(REQUEST_ID, "req-001")
            .where(USER_ROLE, "admin")
            .run(() -> {
                // Inside the scope — ScopedValues are accessible
                System.out.println("  REQUEST_ID: " + REQUEST_ID.get());
                System.out.println("  USER_ROLE: " + USER_ROLE.get());
                processRequest();
            });

        // Outside scope — get() throws NoSuchElementException
        // String outside = REQUEST_ID.get(); // would throw

        System.out.println("\n  ScopedValue scope ended — values are gone.");
    }

    static void processRequest() {
        // Called within the scope — values propagate automatically
        System.out.println("  In processRequest, REQUEST_ID = " + REQUEST_ID.get());
        System.out.println("  In processRequest, USER_ROLE = " + USER_ROLE.get());

        // ScopedValue values are inherited by virtual threads created within scope
        CompletableFuture.runAsync(() -> {
            System.out.println("  In virtual thread, REQUEST_ID = " + REQUEST_ID.get());
        }).join();
    }
}
```

## Pin Prevention

```java
import java.util.concurrent.*;
import java.util.concurrent.locks.*;

public class PinPreventionDemo {
    private static final ReentrantLock reentrantLock = new ReentrantLock();
    private static final Object synchronizedLock = new Object();
    private static volatile int counter = 0;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Pin Prevention ---");
        // Virtual threads can be "pinned" to carrier threads when:
        // 1. Inside a synchronized block/method
        // 2. Inside a native method or JNI
        // Pinning limits scalability — the carrier thread can't multiplex other VTs

        // BAD (pinning): synchronized blocks pin the virtual thread
        Runnable pinnedTask = () -> {
            synchronized (synchronizedLock) { // <-- PINNING
                counter++;
            }
        };

        // GOOD: ReentrantLock does NOT pin
        Runnable nonPinnedTask = () -> {
            reentrantLock.lock();
            try {
                counter++;
            } finally {
                reentrantLock.unlock();
            }
        };

        System.out.println("  Prefer ReentrantLock over synchronized in virtual threads.");
        System.out.println("  Avoid synchronized in hot paths when using virtual threads.");

        // Demonstration: 10K tasks
        int taskCount = 10_000;
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < taskCount; i++) {
                executor.submit(nonPinnedTask);
            }
        }
        System.out.println("  Completed " + taskCount + " tasks without pinning. Counter: " + counter);
    }
}
```
