# Core Java Advanced — Lesson 7: Multithreading Fundamentals

## What is Multithreading?

**Multithreading** = running MULTIPLE threads (paths of execution) within the same program.

```
Single-threaded:
┌─────────────────────────────────────────┐
│ main: [───task1───][───task2───]        │
│         ↑ Only one thing at a time       │
└─────────────────────────────────────────┘

Multi-threaded:
┌─────────────────────────────────────────┐
│ main: [───task1───]                     │
│ thread-1:          [───task2───]        │
│ thread-2: [───task3───]                 │
│         ↑ Multiple things in parallel    │
└─────────────────────────────────────────┘
```

## Creating Threads

### Method 1: Extend Thread

```java
class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread running: " + getName());
    }
}

MyThread t = new MyThread();
t.start();  // DON'T call run() directly! Call start()!
```

### Method 2: Implement Runnable (PREFERRED)

```java
class MyTask implements Runnable {
    @Override
    public void run() {
        System.out.println("Runnable running: " + Thread.currentThread().getName());
    }
}

Thread t = new Thread(new MyTask());
t.start();

// Lambda version:
Thread t = new Thread(() -> System.out.println("Lambda thread"));
t.start();
```

**Why Runnable is preferred:** You can still extend another class. Separates task from thread.

## Thread States

```
NEW ──▶ RUNNABLE ──▶ RUNNING ──▶ TERMINATED
             │            │
             ▼            ▼
          BLOCKED    WAITING/TIMED_WAITING
```

```java
Thread t = new Thread(task);
System.out.println(t.getState());  // NEW

t.start();
System.out.println(t.getState());  // RUNNABLE

// During execution, the thread may be:
// - RUNNABLE: ready to run, waiting for CPU
// - TIMED_WAITING: Thread.sleep(), wait(timeout)
// - WAITING: wait(), join()
// - BLOCKED: waiting for a monitor lock
// - TERMINATED: finished execution
```

## Thread Lifecycle

```java
public class ThreadLifecycle {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            try {
                System.out.println("  Running in: " + Thread.currentThread().getName());
                Thread.sleep(100);  // TIMED_WAITING
                System.out.println("  Waking up...");
            } catch (InterruptedException e) {
                System.out.println("  Interrupted!");
            }
        };

        Thread t = new Thread(task);
        System.out.println("State: " + t.getState());  // NEW

        t.start();
        System.out.println("State: " + t.getState());  // RUNNABLE

        Thread.sleep(50);
        System.out.println("State: " + t.getState());  // TIMED_WAITING (during sleep)

        t.join();  // Wait for thread to finish
        System.out.println("State: " + t.getState());  // TERMINATED
    }
}
```

## Thread Synchronization

### The Problem — Race Conditions

```java
class Counter {
    private int count = 0;

    public void increment() {
        count++;  // NOT atomic! Read → Add → Write (3 steps)
    }

    public int getCount() { return count; }
}

// If two threads call increment() at the same time:
// Thread A reads count (0)
// Thread B reads count (0)
// Thread A writes 1
// Thread B writes 1  ← Lost update! Should be 2!
```

### Solution 1: Synchronized Methods

```java
class SafeCounter {
    private int count = 0;

    public synchronized void increment() {  // Only ONE thread at a time
        count++;
    }

    public synchronized int getCount() {
        return count;
    }
}
```

### Solution 2: Synchronized Blocks

```java
class SafeList {
    private final List<String> list = new ArrayList<>();
    private final Object lock = new Object();

    public void add(String item) {
        synchronized (lock) {  // Fine-grained locking
            list.add(item);
        }
    }

    public String get(int index) {
        synchronized (lock) {
            return list.get(index);
        }
    }
}
```

### The `volatile` Keyword

`volatile` ensures that reads/writes go directly to **main memory** (not cached):

```java
public class VisibilityDemo {
    private volatile boolean running = true;  // Don't cache this!

    public void stop() {
        running = false;
    }

    public void run() {
        while (running) {  // Without volatile, might loop forever!
            // Do work
        }
    }
}
```

**Without volatile**, one thread might keep seeing a cached value of `running = true` forever.

## Inter-thread Communication

### wait() / notify()

```java
class MessageQueue {
    private final Queue<String> queue = new LinkedList<>();
    private final int capacity;

    public MessageQueue(int capacity) {
        this.capacity = capacity;
    }

    public synchronized void produce(String msg) throws InterruptedException {
        while (queue.size() == capacity) {
            wait();  // Wait until there's space
        }
        queue.add(msg);
        notifyAll();  // Wake up consumers
    }

    public synchronized String consume() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();  // Wait until there's data
        }
        String msg = queue.poll();
        notifyAll();  // Wake up producers
        return msg;
    }
}
```

## Thread Pools (ExecutorService)

Creating threads is expensive. **Thread pools** reuse threads:

```java
import java.util.concurrent.*;

// Fixed thread pool — 4 threads
ExecutorService executor = Executors.newFixedThreadPool(4);

// Submit tasks
executor.submit(() -> System.out.println("Task 1"));
executor.submit(() -> System.out.println("Task 2"));

// Submit with return value
Future<Integer> future = executor.submit(() -> {
    Thread.sleep(1000);
    return 42;
});

// Get result (blocks until done)
Integer result = future.get();  // 42

// Shutdown
executor.shutdown();  // No new tasks, wait for running tasks
executor.awaitTermination(10, TimeUnit.SECONDS);
```

### Types of Thread Pools

```java
ExecutorService fixed = Executors.newFixedThreadPool(4);     // Fixed size
ExecutorService cached = Executors.newCachedThreadPool();     // Grows/shrinks
ExecutorService single = Executors.newSingleThreadExecutor(); // One thread
ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(2); // Delayed tasks

// Schedule a delayed task
scheduled.schedule(() -> System.out.println("Delayed!"), 5, TimeUnit.SECONDS);

// Schedule a repeating task
scheduled.scheduleAtFixedRate(() -> System.out.println("Every 2s"), 0, 2, TimeUnit.SECONDS);
```

---

### Exercises

1. Create two threads that print numbers 1-10 alternately (Thread A: 1, Thread B: 2, Thread A: 3, ...).
2. Implement a thread-safe `BankAccount` where multiple threads deposit/withdraw simultaneously.
3. Create a producer-consumer pattern using `BlockingQueue`.
4. Use `ExecutorService` to process a list of 100 URLs concurrently (max 10 at a time).
5. Demonstrate a race condition, then fix it with synchronization.
6. Show the difference between `sleep()` and `wait()`.
