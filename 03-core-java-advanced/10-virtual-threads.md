# Core Java Advanced — Lesson 10: Virtual Threads (Project Loom)

## What Are Virtual Threads?

**Virtual threads** (Java 21+, final) are lightweight threads that let you write simple sequential code while achieving high concurrency.

```
Platform Thread (OS Thread):      Virtual Thread (JVM-managed):
┌────────────────────┐           ┌────────────────────┐
│ Expensive (~1MB    │           │ Cheap (~few KB)    │
│ stack)             │           │                    │
│                    │           │ 10,000+ per GB     │
│ Max ~4,000 per GB  │           │                    │
│ OS-managed         │           │ JVM-managed        │
│ Heavy context      │           │ Lightweight        │
│ switch             │           │ context switch     │
└────────────────────┘           └────────────────────┘
```

**Mental Model:** Virtual threads are like a small number of waiters (platform threads) serving many customers (virtual threads). When a customer is busy eating (blocking I/O), the waiter serves someone else.

## Creating Virtual Threads

```java
// Method 1: Thread.ofVirtual()
Thread vThread = Thread.ofVirtual()
    .name("virtual-thread-1")
    .start(() -> {
        System.out.println("Hello from: " + Thread.currentThread());
    });

vThread.join();

// Method 2: Executors.newVirtualThreadPerTaskExecutor()
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> System.out.println("Task 1"));
    executor.submit(() -> System.out.println("Task 2"));
    executor.submit(() -> System.out.println("Task 3"));
}
// Automatically waits for all tasks!
```

## Virtual Threads in Action

```java
// Platform threads — limited!
public class PlatformThreadDemo {
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 10_000; i++) {
            threads.add(new Thread(() -> {
                try {
                    Thread.sleep(100);  // Blocking I/O
                } catch (InterruptedException e) {}
            }));
        }
        // Likely crashes or runs out of memory!
    }
}
```

```java
// Virtual threads — handles 100K+ easily!
public class VirtualThreadDemo {
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < 100_000; i++) {
            threads.add(Thread.ofVirtual().start(() -> {
                try {
                    Thread.sleep(100);  // Blocking I/O — doesn't block OS thread!
                } catch (InterruptedException e) {}
            }));
        }

        for (Thread t : threads) {
            t.join();
        }

        System.out.println("100K virtual threads done in " +
            (System.currentTimeMillis() - start) + "ms");
    }
}
```

## How Virtual Threads Work

```
PLATFORM THREADS (Carrier threads):
┌─────────────┐ ┌─────────────┐ ┌─────────────┐ ┌─────────────┐
│   Carrier 1  │ │   Carrier 2  │ │   Carrier 3  │ │   Carrier 4  │
└──────┬───────┘ └──────┬──────┘ └──────┬──────┘ └──────┬──────┘
       │                │               │               │
       ▼                ▼               ▼               ▼
┌──────────────────────────────────────────────────────────────┐
│              VIRTUAL THREAD SCHEDULER (ForkJoinPool)          │
│                                                               │
│  VT1 ──▶ Running ──▶ Blocked (I/O) ──▶ Mount another VT      │
│  VT2 ──▶ Ready                                          │
│  VT3 ──▶ Blocked ──▶ Unblocked ──▶ Ready                  │
│  ...                                                       │
│  100K virtual threads multiplexed onto 4 carrier threads    │
└──────────────────────────────────────────────────────────────┘
```

**Key insight:** When a virtual thread performs blocking I/O (like `sleep()`, `socket.read()`, `database query`), it "yields" its carrier thread, which immediately picks up another virtual thread. **No expensive OS context switch!**

## Virtual Threads with Spring Boot

```java
// Spring Boot 3.2+ — single config change!
# application.properties
spring.threads.virtual.enabled=true

// Or programmatically:
@Bean
public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutor() {
    return protocolHandler -> {
        protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    };
}
```

## Virtual Thread Best Practices

### DO: Use with blocking I/O

```java
// Virtual threads EXCEL at blocking I/O
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    List<Future<Data>> futures = urls.stream()
        .map(url -> executor.submit(() -> fetchUrl(url)))
        .toList();
    for (var future : futures) {
        process(future.get());
    }
}
```

### DO NOT: Use for CPU-intensive work

```java
// BAD — Virtual threads don't help CPU
for (int i = 0; i < 100_000; i++) {
    Thread.ofVirtual().start(() -> {
        computePi(10000);  // CPU-bound! Use platform threads or parallel streams
    });
}

// GOOD — Use parallel stream for CPU work
IntStream.range(0, 100_000).parallel()
    .forEach(i -> computePi(10000));
```

### Pitfall: ThreadLocal with Many Virtual Threads

```java
// BAD — ThreadLocal with virtual threads can use a LOT of memory
// (each virtual thread creates its own copy)
private static final ThreadLocal<MyContext> context = new ThreadLocal<>();

// GOOD — Consider passing context explicitly
public void handle(Request req, Context ctx) {
    // ...
}
```

### Pitfall: Synchronized Blocks

```java
// synchronized BLOCKS virtual threads to carrier threads!
// Use ReentrantLock instead:
private final Lock lock = new ReentrantLock();

public void doSomething() {
    lock.lock();
    try {
        // critical section
    } finally {
        lock.unlock();
    }
}
```

---

### Exercises

1. Create 10,000 virtual threads that each sleep for 1 second. Measure total time and memory usage.
2. Compare: create 10,000 platform threads vs 10,000 virtual threads. Note the difference.
3. Use `Executors.newVirtualThreadPerTaskExecutor()` to fetch 100 URLs concurrently.
4. Create a simple web server that uses a virtual thread per request.
5. Benchmark virtual threads vs platform threads for I/O-bound tasks vs CPU-bound tasks.
