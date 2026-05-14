# Core Java Advanced — Lesson 8: Concurrency Deep Dive

## The `java.util.concurrent` Package

Java provides high-level concurrency utilities beyond basic `synchronized`.

## Locks

### ReentrantLock — More Flexible Than synchronized

```java
import java.util.concurrent.locks.*;

public class BankAccount {
    private double balance;
    private final Lock lock = new ReentrantLock();

    public void withdraw(double amount) {
        lock.lock();
        try {
            if (balance >= amount) {
                balance -= amount;
            }
        } finally {
            lock.unlock();  // ALWAYS unlock in finally!
        }
    }

    // Try lock with timeout
    public boolean tryWithdraw(double amount, long timeout, TimeUnit unit)
            throws InterruptedException {
        if (lock.tryLock(timeout, unit)) {
            try {
                if (balance >= amount) {
                    balance -= amount;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
        return false;  // Couldn't acquire lock in time
    }
}
```

### ReadWriteLock — Multiple Readers, Single Writer

```java
public class Cache {
    private final Map<String, Object> data = new HashMap<>();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();

    public Object get(String key) {
        readLock.lock();  // Multiple readers allowed!
        try {
            return data.get(key);
        } finally {
            readLock.unlock();
        }
    }

    public void put(String key, Object value) {
        writeLock.lock();  // Exclusive access!
        try {
            data.put(key, value);
        } finally {
            writeLock.unlock();
        }
    }
}
```

## Atomic Classes

Lock-free, thread-safe operations using hardware-level CAS (Compare-And-Swap):

```java
import java.util.concurrent.atomic.*;

AtomicInteger count = new AtomicInteger(0);
AtomicLong total = new AtomicLong(0);
AtomicBoolean flag = new AtomicBoolean(false);
AtomicReference<String> ref = new AtomicReference<>("hello");

// Operations
count.incrementAndGet();       // ++count (thread-safe!)
count.getAndIncrement();       // count++ (thread-safe!)
count.addAndGet(5);            // count += 5
count.compareAndSet(10, 20);   // If count==10, set to 20 (atomic!)

// Performance: AtomicInteger is MUCH faster than synchronized int
```

## Concurrent Collections

Thread-safe collections designed for concurrent access:

```java
// HashMap equivalent — NO null keys/values!
ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
map.put("key", 1);
map.get("key");
map.putIfAbsent("key", 2);    // Only puts if key doesn't exist
map.computeIfAbsent("key", k -> expensiveCalculation(k));  // Atomic! Only runs once!

// List equivalent
CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
// Good for: mostly reads, few writes (creates copy on write)

// Queue/Deque
ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
// Lock-free, high throughput

// Blocking queues (for producer-consumer)
BlockingQueue<String> bq = new ArrayBlockingQueue<>(100);  // Bounded
LinkedBlockingQueue<String> lbq = new LinkedBlockingQueue<>();  // Optionally bounded
```

## CountDownLatch

Wait for N threads to complete:

```java
public class LatchDemo {
    public static void main(String[] args) throws InterruptedException {
        int workers = 3;
        CountDownLatch latch = new CountDownLatch(workers);

        for (int i = 0; i < workers; i++) {
            new Thread(() -> {
                System.out.println(Thread.currentThread().getName() + " working...");
                try { Thread.sleep(1000); } catch (InterruptedException e) {}
                latch.countDown();  // Decrement
                System.out.println(Thread.currentThread().getName() + " done");
            }).start();
        }

        latch.await();  // Main thread waits until latch reaches 0
        System.out.println("All workers done! Continuing main thread");
    }
}
```

## CyclicBarrier

Wait for N threads to reach a common point, then proceed together:

```java
int parties = 3;
CyclicBarrier barrier = new CyclicBarrier(parties, () -> {
    System.out.println("All parties reached barrier! Let's go!");
});

for (int i = 0; i < parties; i++) {
    new Thread(() -> {
        System.out.println(Thread.currentThread().getName() + " at barrier");
        try {
            barrier.await();  // Wait for all 3 threads
        } catch (Exception e) {}
        System.out.println(Thread.currentThread().getName() + " passed barrier");
    }).start();
}
```

## Semaphore

Control access to a limited resource:

```java
Semaphore parkingSlots = new Semaphore(5);  // 5 parking spots

public void park() {
    try {
        parkingSlots.acquire();  // Wait for a spot
        System.out.println("Parked! Available: " + parkingSlots.availablePermits());
        Thread.sleep(5000);  // Park for 5 seconds
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    } finally {
        parkingSlots.release();  // Leave
    }
}
```

## Phaser (Java 7+)

A more flexible barrier that supports dynamic party registration:

```java
Phaser phaser = new Phaser(1);  // Register main thread

new Thread(() -> {
    phaser.register();  // Register this thread
    System.out.println("Thread 1 working...");
    phaser.arriveAndAwaitAdvance();  // Wait for phase to complete
    System.out.println("Thread 1 continuing...");
    phaser.arriveAndDeregister();  // Leave
}).start();

phaser.arriveAndDeregister();  // Main thread done registering
```

## The `ForkJoinPool` — Work-Stealing

```java
public class FibonacciTask extends RecursiveTask<Integer> {
    private final int n;

    public FibonacciTask(int n) { this.n = n; }

    @Override
    protected Integer compute() {
        if (n <= 1) return n;
        FibonacciTask f1 = new FibonacciTask(n - 1);
        f1.fork();  // Fork — runs in parallel
        FibonacciTask f2 = new FibonacciTask(n - 2);
        return f2.compute() + f1.join();  // Join results
    }
}

ForkJoinPool pool = ForkJoinPool.commonPool();
int result = pool.invoke(new FibonacciTask(30));
System.out.println(result);  // 832040
```

## Performance Comparison

| Synchronization | Relative Speed | Best For |
|----------------|---------------|----------|
| `synchronized` | 1x (baseline) | Simplicity, short operations |
| `ReentrantLock` | ~1.5x faster | Fine-grained control, timeouts |
| `AtomicInteger` | ~3-5x faster | Simple counters, flags |
| `LongAdder` | ~10x faster | High-contention counters |
| Lock-free algorithms | ~10-100x faster | Specialized, very hot paths |

---

### Exercises

1. Use `ReadWriteLock` to create a thread-safe cache that allows multiple concurrent readers.
2. Use `CountDownLatch` to coordinate 5 worker threads that must all finish before a main thread proceeds.
3. Implement a rate limiter using `Semaphore`.
4. Use `ConcurrentHashMap.computeIfAbsent()` for thread-safe lazy initialization.
5. Benchmark `synchronized` vs `AtomicInteger` for incrementing a counter 1 million times from 10 threads.
