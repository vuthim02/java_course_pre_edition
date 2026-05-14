# Concurrency Deep Dive

This document covers advanced concurrency utilities from `java.util.concurrent` including `ReentrantLock`, `ReadWriteLock`, `Condition`, `Semaphore`, `CountDownLatch`, `CyclicBarrier`, `Exchanger`, `Phaser`, concurrent collections, `BlockingQueue`, and atomic variables.

## ReentrantLock

```java
import java.util.concurrent.locks.*;
import java.util.concurrent.TimeUnit;

class SharedCounter {
    private int count = 0;
    private final ReentrantLock lock = new ReentrantLock();

    public void increment() {
        lock.lock(); // blocks until lock acquired
        try {
            count++;
        } finally {
            lock.unlock(); // always unlock in finally
        }
    }

    // tryLock — non-blocking attempt with timeout
    public boolean tryIncrement(long timeout, TimeUnit unit) throws InterruptedException {
        if (lock.tryLock(timeout, unit)) {
            try {
                count++;
                return true;
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    public int getCount() {
        return count;
    }

    public boolean isLocked() {
        return lock.isLocked();
    }

    public int getQueueLength() {
        return lock.getQueueLength();
    }
}

public class ReentrantLockDemo {
    public static void main(String[] args) throws Exception {
        System.out.println("--- ReentrantLock ---");

        SharedCounter counter = new SharedCounter();
        Runnable task = () -> {
            for (int i = 0; i < 1000; i++) {
                counter.increment();
            }
        };

        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Count (expected 2000): " + counter.getCount());

        // tryLock demonstration
        boolean acquired = counter.tryIncrement(100, TimeUnit.MILLISECONDS);
        System.out.println("tryLock acquired: " + acquired + ", count=" + counter.getCount());
    }
}
```

## ReadWriteLock

```java
import java.util.concurrent.locks.*;

class ThreadSafeCache {
    private String data = "initial";
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

    // Multiple readers can read simultaneously
    public String read() {
        rwLock.readLock().lock();
        try {
            System.out.println("  Reading: " + Thread.currentThread().getName());
            Thread.sleep(200); // simulate work
            return data;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    // Only one writer at a time, blocks all readers
    public void write(String newData) {
        rwLock.writeLock().lock();
        try {
            System.out.println("  Writing: " + newData);
            Thread.sleep(300);
            this.data = newData;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            rwLock.writeLock().unlock();
        }
    }
}

public class ReadWriteLockDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- ReadWriteLock ---");
        var cache = new ThreadSafeCache();

        // Multiple readers
        for (int i = 0; i < 3; i++) {
            new Thread(cache::read, "Reader-" + i).start();
        }

        Thread.sleep(100);
        // Single writer — blocks until all readers finish
        new Thread(() -> cache.write("updated"), "Writer").start();
    }
}
```

## Condition (await/signal)

```java
import java.util.concurrent.locks.*;
import java.util.*;

class BoundedBuffer<T> {
    private final LinkedList<T> queue = new LinkedList<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public BoundedBuffer(int capacity) {
        this.capacity = capacity;
    }

    public void put(T item) throws InterruptedException {
        lock.lock();
        try {
            while (queue.size() == capacity) {
                notFull.await(); // wait until not full
            }
            queue.addLast(item);
            notEmpty.signal(); // signal waiting consumers
        } finally {
            lock.unlock();
        }
    }

    public T take() throws InterruptedException {
        lock.lock();
        try {
            while (queue.isEmpty()) {
                notEmpty.await(); // wait until not empty
            }
            T item = queue.removeFirst();
            notFull.signal(); // signal waiting producers
            return item;
        } finally {
            lock.unlock();
        }
    }
}

public class ConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Condition (await/signal) ---");
        var buffer = new BoundedBuffer<String>(2);

        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    buffer.put("item-" + i);
                    System.out.println("Produced: item-" + i);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    String item = buffer.take();
                    System.out.println("Consumed: " + item);
                    Thread.sleep(500);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
    }
}
```

## Semaphore

```java
import java.util.concurrent.*;

public class SemaphoreDemo {
    public static void main(String[] args) {
        System.out.println("--- Semaphore ---");
        // Semaphore controls access to a pool of resources
        // permits = 3 (up to 3 concurrent accesses)
        Semaphore pool = new Semaphore(3);

        for (int i = 1; i <= 6; i++) {
            int taskId = i;
            new Thread(() -> {
                try {
                    System.out.println("Task " + taskId + " waiting for permit...");
                    pool.acquire(); // blocks until a permit is available
                    System.out.println("Task " + taskId + " acquired permit (available: " + pool.availablePermits() + ")");
                    Thread.sleep(1000); // use resource
                    System.out.println("Task " + taskId + " releasing permit");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    pool.release();
                }
            }).start();
        }
    }
}
```

## CountDownLatch and CyclicBarrier

```java
import java.util.concurrent.*;

public class LatchBarrierDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- CountDownLatch ---");
        // One-shot: threads wait until count reaches zero (cannot be reset)
        int workerCount = 3;
        CountDownLatch latch = new CountDownLatch(workerCount);

        for (int i = 1; i <= workerCount; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep(id * 500L);
                    System.out.println("  Worker " + id + " done");
                    latch.countDown(); // decrement count
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }

        latch.await(); // blocks until count reaches zero
        System.out.println("All workers done (latch triggered)");

        System.out.println("\n--- CyclicBarrier ---");
        // Reusable: threads wait at a barrier until all arrive, then proceed
        int parties = 3;
        CyclicBarrier barrier = new CyclicBarrier(parties, () ->
            System.out.println("  ** Barrier action: all parties arrived! **")
        );

        for (int i = 1; i <= parties; i++) {
            int id = i;
            new Thread(() -> {
                try {
                    Thread.sleep(id * 300L);
                    System.out.println("  Party " + id + " waiting at barrier");
                    barrier.await(); // wait for all parties
                    System.out.println("  Party " + id + " passed barrier");
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }
}
```

## Exchanger and Phaser

```java
import java.util.concurrent.*;

public class ExchangerPhaserDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Exchanger ---");
        // Two threads swap data at a synchronization point
        Exchanger<String> exchanger = new Exchanger<>();

        Thread t1 = new Thread(() -> {
            try {
                String data1 = "Data from Thread-1";
                String received = exchanger.exchange(data1);
                System.out.println("  Thread-1 received: " + received);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        Thread t2 = new Thread(() -> {
            try {
                String data2 = "Data from Thread-2";
                String received = exchanger.exchange(data2);
                System.out.println("  Thread-2 received: " + received);
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("\n--- Phaser ---");
        // Reusable synchronization barrier (evolution of CyclicBarrier + CountDownLatch)
        Phaser phaser = new Phaser(1); // register main thread

        for (int i = 1; i <= 3; i++) {
            int id = i;
            phaser.register(); // register each party
            new Thread(() -> {
                System.out.println("  Phase-" + id + " arrived at phase " + phaser.getPhase());
                phaser.arriveAndAwaitAdvance(); // synchronize

                System.out.println("  Phase-" + id + " passed phase " + (phaser.getPhase() - 1));
                phaser.arriveAndDeregister(); // leave the phaser
            }).start();
        }

        phaser.arriveAndDeregister(); // main thread leaves
        System.out.println("Main done");
    }
}
```

## ConcurrentHashMap

```java
import java.util.*;
import java.util.concurrent.*;

public class ConcurrentHashMapDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- ConcurrentHashMap ---");

        // ConcurrentHashMap: fully concurrent reads, segmented writes
        // Hashtable: synchronized on all operations (slow)
        // Collections.synchronizedMap: synchronized on all operations (slow)

        ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();

        // ConcurrentHashMap allows concurrent modifications
        Runnable writer = () -> {
            for (int i = 0; i < 500; i++) {
                map.merge("key-" + (i % 10), 1, Integer::sum); // atomic update
            }
        };

        Thread t1 = new Thread(writer);
        Thread t2 = new Thread(writer);
        t1.start();
        t2.start();
        t1.join();
        t2.join();

        System.out.println("Map size: " + map.size());
        map.forEach((k, v) -> System.out.println("  " + k + " -> " + v));

        // ConcurrentHashMap provides atomic methods
        map.computeIfAbsent("new-key", k -> 42);
        System.out.println("computeIfAbsent: " + map.get("new-key"));

        // Search, reduce, forEach with parallelism threshold
        int sum = map.reduceValues(1, Integer::intValue, Integer::sum);
        System.out.println("Sum of all values: " + sum);
    }
}
```

## CopyOnWriteArrayList and ConcurrentLinkedQueue

```java
import java.util.concurrent.*;

public class ConcurrentCollectionsDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- CopyOnWriteArrayList ---");
        // Thread-safe: snapshot iterator, copy on mutation (good for read-heavy)
        CopyOnWriteArrayList<String> list = new CopyOnWriteArrayList<>();
        list.add("A");
        list.add("B");
        list.add("C");

        // Iterator sees snapshot — safe to modify during iteration
        for (String s : list) {
            System.out.println("  Reading: " + s);
            if (s.equals("B")) list.add("D"); // no ConcurrentModificationException
        }
        System.out.println("  List after iteration: " + list);

        System.out.println("\n--- ConcurrentLinkedQueue ---");
        // Lock-free, non-blocking, FIFO queue
        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<>();
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");

        // Poll (retrieve and remove)
        String item;
        while ((item = queue.poll()) != null) {
            System.out.println("  Polled: " + item);
        }
        System.out.println("Queue empty: " + queue.isEmpty());
    }
}
```

## BlockingQueue

```java
import java.util.concurrent.*;

public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- ArrayBlockingQueue ---");
        // Bounded blocking queue
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(2);

        // Producer-consumer pattern
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    String item = "item-" + i;
                    queue.put(item); // blocks if full
                    System.out.println("  Produced: " + item);
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    String item = queue.take(); // blocks if empty
                    System.out.println("  Consumed: " + item);
                    Thread.sleep(400);
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });

        producer.start();
        consumer.start();
        producer.join();
        consumer.join();

        System.out.println("\n--- LinkedBlockingQueue ---");
        // Optionally bounded linked-node queue (default Integer.MAX_VALUE)
        BlockingQueue<Integer> linkedQueue = new LinkedBlockingQueue<>(10);
        linkedQueue.offer(1);
        linkedQueue.offer(2, 1, TimeUnit.SECONDS); // timed offer
        System.out.println("LinkedBlockingQueue: " + linkedQueue);
    }
}
```

## Atomic Variables

```java
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.LockSupport;

public class AtomicDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- AtomicInteger ---");
        // Lock-free, thread-safe counter via CAS (Compare-And-Swap)
        AtomicInteger atomicInt = new AtomicInteger(0);

        Runnable incrementTask = () -> {
            for (int i = 0; i < 1000; i++) {
                atomicInt.incrementAndGet(); // atomic ++
            }
        };

        Thread t1 = new Thread(incrementTask);
        Thread t2 = new Thread(incrementTask);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        System.out.println("AtomicInteger (expected 2000): " + atomicInt.get());

        System.out.println("\n--- AtomicReference ---");
        // Atomic update of object references
        AtomicReference<String> ref = new AtomicReference<>("initial");
        boolean updated = ref.compareAndSet("initial", "modified");
        System.out.println("CAS succeeded: " + updated + ", value: " + ref.get());

        // Atomic update functions
        ref.updateAndGet(s -> s + "_and_more");
        System.out.println("After updateAndGet: " + ref.get());

        System.out.println("\n--- LongAdder ---");
        // Higher throughput than AtomicInteger under high contention
        // (sacrifices precision for speed — uses striping)
        LongAdder adder = new LongAdder();
        Runnable adderTask = () -> {
            for (int i = 0; i < 1000; i++) adder.increment();
        };

        Thread t3 = new Thread(adderTask);
        Thread t4 = new Thread(adderTask);
        t3.start();
        t4.start();
        t3.join();
        t4.join();
        System.out.println("LongAdder (expected 2000): " + adder.sum());
        System.out.println("LongAdder sumThenReset: " + adder.sumThenReset());

        System.out.println("\n--- AtomicBoolean ---");
        AtomicBoolean flag = new AtomicBoolean(false);
        boolean oldValue = flag.getAndSet(true);
        System.out.println("getAndSet (old): " + oldValue + ", now: " + flag.get());
    }
}
```
