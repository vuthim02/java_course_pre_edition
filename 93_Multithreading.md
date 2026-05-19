# Lesson 93: Multithreading

## Key Concepts
- A thread is a lightweight process that runs concurrently with other threads
- Two ways to create a thread: extend `Thread` or implement `Runnable`
- `start()` launches a thread (calls the `run()` method internally)
- `sleep()` pauses the current thread for a given number of milliseconds
- `join()` waits for a thread to finish before continuing
- `synchronized` prevents race conditions by allowing only one thread at a time into a method

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Multithreading Demo ===\n");

        MyThread thread1 = new MyThread("Thread-1");
        MyThread thread2 = new MyThread("Thread-2");

        thread1.start();
        thread2.start();

        Runnable task1 = new MyRunnable("Runnable-1");
        Runnable task2 = new MyRunnable("Runnable-2");

        Thread thread3 = new Thread(task1);
        Thread thread4 = new Thread(task2);

        thread3.start();
        thread4.start();

        try {
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\nAll threads completed!");

        System.out.println("\n=== Counter with synchronization ===");
        Counter counter = new Counter();

        Thread incrementer1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        Thread incrementer2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) counter.increment();
        });

        incrementer1.start();
        incrementer2.start();

        try {
            incrementer1.join();
            incrementer2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Final count (expected 2000): " + counter.getCount());
    }
}

class MyThread extends Thread {
    MyThread(String name) {
        super(name);
    }

    @Override
    public void run() {
        for (int i = 1; i <= 5; i++) {
            System.out.println(getName() + ": " + i);
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class MyRunnable implements Runnable {
    private String name;

    MyRunnable(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        for (int i = 1; i <= 3; i++) {
            System.out.println(name + ": " + i);
            try {
                Thread.sleep(700);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Counter {
    private int count = 0;

    synchronized void increment() {
        count++;
    }

    int getCount() {
        return count;
    }
}
```

## Explanation
1. **Extending Thread**: `MyThread` extends `Thread` and overrides `run()`. Call `start()` to begin execution.
2. **Implementing Runnable**: `MyRunnable` implements `Runnable` and is passed to a `Thread` constructor. This is preferred because it allows the class to extend another class if needed.
3. **sleep()**: Each thread pauses for 500ms (MyThread) or 700ms (MyRunnable) between iterations, letting other threads run.
4. **join()**: The main thread calls `join()` on each thread, waiting for all of them to finish before printing "All threads completed!".
5. **synchronized**: Without `synchronized` on `increment()`, two threads could read the same count, both increment it, and write back the same value — losing an increment. The `synchronized` keyword ensures mutual exclusion.

## Expected Output

```
=== Multithreading Demo ===

Thread-1: 1
Thread-2: 1
Runnable-1: 1
Thread-1: 2
Runnable-2: 1
Thread-2: 2
Runnable-1: 2
Thread-1: 3
Thread-2: 3
Runnable-2: 2
Thread-1: 4
Runnable-1: 3
Thread-2: 4
Thread-1: 5
Thread-2: 5
Runnable-2: 3

All threads completed!

=== Counter with synchronization ===
Final count (expected 2000): 2000
```

*(The interleaving of thread output may vary each run.)*
