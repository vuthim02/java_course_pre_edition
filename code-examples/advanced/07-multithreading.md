# Multithreading Fundamentals

This document covers Java multithreading basics including the `Thread` class, `Runnable` interface, thread states, `sleep`/`join`/`interrupt`, daemon threads, `synchronized`, `volatile`, and thread naming/priority.

## Thread Class and Runnable

```java
public class ThreadBasicsDemo {
    public static void main(String[] args) {
        System.out.println("Main thread: " + Thread.currentThread().getName());

        // Extending Thread class
        Thread threadByExtend = new MyThread();
        threadByExtend.setName("Worker-1");
        threadByExtend.start(); // calls run() in a new thread

        // Implementing Runnable (anonymous class)
        Thread threadByRunnable = new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Runnable running in: " + Thread.currentThread().getName());
            }
        });
        threadByRunnable.setName("Worker-2");
        threadByRunnable.start();

        // Runnable with lambda (modern preferred approach)
        Thread threadByLambda = new Thread(() ->
            System.out.println("Lambda thread: " + Thread.currentThread().getName())
        );
        threadByLambda.setName("Worker-3");
        threadByLambda.start();

        // Wait for all threads to finish
        try {
            threadByExtend.join();
            threadByRunnable.join();
            threadByLambda.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All threads completed.");
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("MyThread running: " + getName());
    }
}
```

## Thread States

```java
public class ThreadStatesDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Thread States ---");
        // States: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED

        Thread t = new Thread(() -> {
            try {
                System.out.println("  State in run(): " + Thread.currentThread().getState());
                Thread.sleep(500); // TIMED_WAITING
                synchronized (ThreadStatesDemo.class) {
                    ThreadStatesDemo.class.wait(100); // WAITING (with timeout)
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        System.out.println("After creation (NEW): " + t.getState());

        t.start();
        Thread.sleep(50);
        System.out.println("After start (RUNNABLE or TIMED_WAITING): " + t.getState());

        Thread.sleep(300);
        System.out.println("During sleep (TIMED_WAITING likely): " + t.getState());

        t.join();
        System.out.println("After completion (TERMINATED): " + t.getState());
    }
}
```

## Thread.sleep, join, and interrupt

```java
public class SleepJoinInterruptDemo {
    public static void main(String[] args) {
        System.out.println("--- sleep, join, interrupt ---");

        Thread worker = new Thread(() -> {
            try {
                for (int i = 1; i <= 5; i++) {
                    System.out.println("  Working... step " + i);
                    Thread.sleep(400); // TIMED_WAITING for 400ms
                }
            } catch (InterruptedException e) {
                // Interrupted while sleeping — clear flag and exit
                System.out.println("  Worker was interrupted during sleep!");
                Thread.currentThread().interrupt(); // preserve interrupt status
            }
        });

        worker.start();

        // Let worker run for a bit, then interrupt it
        try {
            Thread.sleep(1000);
            System.out.println("Interrupting worker...");
            worker.interrupt(); // sets interrupt flag, wakes from sleep
            worker.join();      // wait for worker to finish
            System.out.println("Worker joined.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

## Daemon Threads

```java
public class DaemonThreadDemo {
    public static void main(String[] args) {
        System.out.println("--- Daemon Threads ---");
        // Daemon threads run in background; JVM exits when only daemon threads remain.
        // Daemon threads are terminated abruptly when JVM exits.

        Thread daemon = new Thread(() -> {
            int count = 0;
            while (true) {
                System.out.println("  Daemon heartbeat: " + count++);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        daemon.setDaemon(true); // must be set before start()
        daemon.start();

        Thread userThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
                System.out.println("  User thread finished. JVM will exit now.");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        userThread.start();

        try {
            userThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // Daemon thread is killed when JVM exits
        System.out.println("Main exiting — daemon is killed automatically.");
    }
}
```

## Synchronized Methods and Blocks

```java
class Counter {
    private int count = 0;

    // Synchronized instance method — locks on 'this' (the Counter instance)
    public synchronized void increment() {
        count++;
    }

    // Synchronized static method — locks on Counter.class
    public static synchronized void staticMethod() {
        System.out.println("Static sync method — class-level lock");
    }

    // Block-level synchronization — finer-grained control
    public void decrement() {
        // Synchronized block using 'this' as lock
        synchronized (this) {
            count--;
        }
    }

    public int getCount() {
        return count;
    }
}

public class SynchronizedDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Synchronized ---");

        Counter counter = new Counter();

        // Two threads incrementing the same counter 1000 times each
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        // Without synchronized, would likely be < 2000 due to race conditions
        System.out.println("Final count (expected 2000): " + counter.getCount());

        // Intrinsic locks (monitors):
        // Every object has an intrinsic lock (monitor).
        // Synchronized acquires the lock — other threads block waiting.
        // Lock is released when the sync block/method exits (even via exception).
        System.out.println("\nIntrinsic lock released automatically via finally.");
    }
}
```

## Volatile Keyword

```java
import java.util.concurrent.atomic.AtomicBoolean;

public class VolatileDemo {
    // Without 'volatile', the reading thread may cache the value and never see updates.
    // 'volatile' guarantees visibility: writes by one thread are visible to all others.
    private static volatile boolean running = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Volatile ---");
        // volatile ensures:
        // 1. Visibility: changes are immediately visible to all threads
        // 2. Happens-before: reads/writes to volatile cannot be reordered
        // NOTE: volatile does NOT provide atomicity! i++ still needs sync.

        Thread worker = new Thread(() -> {
            long count = 0;
            while (running) {
                count++;
            }
            System.out.println("Worker stopped after " + count + " iterations");
        });

        worker.start();

        Thread.sleep(500);
        running = false; // without volatile, worker might never see this change
        System.out.println("Set running = false");

        worker.join();
        System.out.println("Main done.");
    }
}
```

## Thread Naming and Priority

```java
public class NamingPriorityDemo {
    public static void main(String[] args) {
        System.out.println("--- Thread Naming and Priority ---");

        // Priority constants: MIN_PRIORITY=1, NORM_PRIORITY=5, MAX_PRIORITY=10
        // Priority is a hint to the OS scheduler — not guaranteed!

        Runnable task = () -> {
            Thread t = Thread.currentThread();
            System.out.println("  " + t.getName() + " (priority=" + t.getPriority() + ") running");
        };

        Thread low = new Thread(task);
        low.setName("LowPriorityThread");
        low.setPriority(Thread.MIN_PRIORITY);

        Thread normal = new Thread(task);
        normal.setName("NormalPriorityThread");
        normal.setPriority(Thread.NORM_PRIORITY);

        Thread high = new Thread(task);
        high.setName("HighPriorityThread");
        high.setPriority(Thread.MAX_PRIORITY);

        high.start();
        normal.start();
        low.start();
        // Output order depends on OS scheduler
    }
}
```

## Complete Runnable Example

```java
public class RunnablePatternDemo {
    public static void main(String[] args) {
        System.out.println("--- Runnable Pattern Summary ---");

        // Best practice: separate task (Runnable) from execution (Thread)
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("Task executing on " + name);
        };

        // Pass to Thread constructor
        Thread thread = new Thread(task);
        thread.start();

        // Or submit to an ExecutorService (preferred for production)
        // ExecutorService exec = Executors.newFixedThreadPool(4);
        // exec.submit(task);
    }
}
```
