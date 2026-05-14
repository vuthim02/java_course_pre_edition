# Memory Management and GC Tuning

This document covers Java memory management, garbage collection observation, memory leak patterns, reference types (`WeakReference`, `SoftReference`, `PhantomReference`), `Cleaner` (replacing `finalize()`), stack vs heap allocation, and JVM flags for GC tuning.

## Runtime Memory Methods

```java
public class MemoryInfoDemo {
    public static void main(String[] args) {
        System.out.println("--- JVM Memory Info ---");

        Runtime rt = Runtime.getRuntime();

        // Memory in bytes (convert to MB)
        long maxMemory = rt.maxMemory();        // max JVM may use (-Xmx)
        long totalMemory = rt.totalMemory();    // currently allocated heap size
        long freeMemory = rt.freeMemory();      // free within allocated heap

        System.out.println("  Max memory (-Xmx):     " + maxMemory / 1024 / 1024 + " MB");
        System.out.println("  Total allocated heap:  " + totalMemory / 1024 / 1024 + " MB");
        System.out.println("  Free in heap:          " + freeMemory / 1024 / 1024 + " MB");
        System.out.println("  Used in heap:          " + (totalMemory - freeMemory) / 1024 / 1024 + " MB");

        // Available processors
        System.out.println("  Available processors:  " + rt.availableProcessors());
    }
}
```

## GC Observation

```java
import java.util.*;

public class GCObservationDemo {
    // Some objects that will be garbage collected
    static class Garbage {
        private final byte[] data = new byte[1024 * 100]; // 100KB each
        private final int id;

        Garbage(int id) {
            this.id = id;
        }

        @Override
        protected void finalize() {
            // Note: finalize() is deprecated since Java 18!
            System.out.println("  finalize() called for #" + id);
        }
    }

    public static void main(String[] args) {
        System.out.println("--- GC Observation ---");
        Runtime rt = Runtime.getRuntime();

        // Create lots of garbage
        System.out.println("Creating garbage objects...");
        List<Garbage> list = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            list.add(new Garbage(i));
        }

        long before = rt.freeMemory();
        list.clear(); // remove references
        System.out.println("Cleared references. Suggesting GC...");

        // System.gc() is a *hint* — JVM may ignore it
        long gcStart = System.nanoTime();
        System.gc();
        // System.gc() triggers Full GC — expensive, use sparingly in production

        // Give GC time to run
        try { Thread.sleep(1000); } catch (InterruptedException e) {}

        long after = rt.freeMemory();
        System.out.println("  Free memory before: " + before / 1024 / 1024 + " MB");
        System.out.println("  Free memory after:  " + after / 1024 / 1024 + " MB");
        System.out.println("  Freed:              " + (after - before) / 1024 / 1024 + " MB");

        /*
         * JVM FLAGS for GC tuning:
         *
         * -Xms2g -Xmx4g                     Initial and max heap size
         * -XX:+UseG1GC                      Use G1 garbage collector (default since Java 9)
         * -XX:+UseZGC                       Use ZGC (low latency, Java 15+)
         * -XX:+UseShenandoahGC              Use Shenandoah GC (low pause, Java 12+)
         * -XX:MaxGCPauseMillis=200          Target max GC pause (G1)
         * -XX:G1HeapRegionSize=16m          Region size for G1
         * -XX:+PrintGCDetails               Print GC details (Java 8)
         * -Xlog:gc*:file=gc.log            Unified GC logging (Java 9+)
         * -XX:+HeapDumpOnOutOfMemoryError   Auto heap dump on OOM
         * -XX:HeapDumpPath=/path/to/dump    Heap dump location
         * -XX:+ExitOnOutOfMemoryError       Exit JVM on OOM
         * -XX:+UseStringDeduplication      Deduplicate String objects (G1)
         */
    }
}
```

## Memory Leak Patterns

```java
import java.util.*;
import java.io.*;

public class MemoryLeakPatternsDemo {
    // Pattern 1: Forgotten references (objects stored but never removed)
    static class LeakyCache {
        private final Map<String, byte[]> cache = new HashMap<>();

        void addData(String key) {
            cache.put(key, new byte[1024 * 1024]); // 1MB each — never removed!
        }

        // Fix: use WeakHashMap or implement eviction
        void addDataFixed(String key) {
            // WeakHashMap entries are removed when key is no longer referenced
        }
    }

    // Pattern 2: Incorrect equals/hashCode causes HashMap growth
    static class BadKey {
        private final int id;

        BadKey(int id) { this.id = id; }

        // Missing equals() and hashCode() — HashMap stores duplicates forever
        // @Override public boolean equals(Object o) { ... }
        // @Override public int hashCode() { return id; }
    }

    public static void main(String[] args) {
        System.out.println("--- Memory Leak Patterns ---");

        System.out.println("""
            Common memory leak patterns:

            1. Forgotten references — objects stored in collections but never removed
               Fix: use WeakHashMap, LRU cache, or explicit removal

            2. Unclosed resources — file handles, streams, connections
               Fix: always use try-with-resources

            3. Incorrect equals/hashCode — HashMap grows unbounded with duplicate keys
               Fix: always implement equals() and hashCode() for key classes

            4. Internal classes holding outer class references
               Fix: use static nested classes when possible

            5. ThreadLocal variables not removed
               Fix: always call ThreadLocal.remove() in finally block

            6. String.substring() in Java 6 (keeps reference to original char[])
               Not an issue in Java 7+ where substring creates a new char[]
            """);
    }
}
```

## Reference Types: Weak, Soft, Phantom

```java
import java.lang.ref.*;

public class ReferenceTypesDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- WeakReference ---");
        // WeakReference — collected as soon as GC runs (used by WeakHashMap)
        Object strong = new Object();
        WeakReference<Object> weakRef = new WeakReference<>(strong);
        System.out.println("  Before nulling: " + weakRef.get());

        strong = null; // remove strong reference
        System.gc();
        Thread.sleep(500);
        System.out.println("  After GC: " + weakRef.get()); // likely null

        System.out.println("\n--- SoftReference ---");
        // SoftReference — collected only when memory is low (useful for caches)
        SoftReference<byte[]> softRef = new SoftReference<>(new byte[1024 * 1024]);
        System.out.println("  Soft ref before: " + (softRef.get() != null));

        // Under memory pressure, soft refs are cleared before OutOfMemoryError
        // softRef.get() would return null after GC under pressure

        System.out.println("\n--- PhantomReference + ReferenceQueue ---");
        // PhantomReference — unable to reach referent via get() (always returns null)
        // Used for post-mortem cleanup (e.g., native memory deallocation)

        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        Object referent = new Object();
        PhantomReference<Object> phantomRef = new PhantomReference<>(referent, queue);

        System.out.println("  Phantom get(): " + phantomRef.get()); // always null

        // When referent is GC'd, the PhantomReference is enqueued
        referent = null;
        System.gc();
        Thread.sleep(500);

        Reference<?> polled = queue.poll();
        System.out.println("  Queue poll: " + (polled == phantomRef ? "== phantomRef (enqueued!)" : "null"));
    }
}
```

## Cleaner (Replacing finalize())

```java
import java.lang.ref.Cleaner;

class NativeResource implements AutoCloseable {
    // Simulates a native resource (e.g., file handle, native memory pointer)
    private final long nativeHandle;
    private final Cleaner.Cleanable cleanable;

    private static final Cleaner CLEANER = Cleaner.create();

    public NativeResource(String name) {
        this.nativeHandle = allocateNative(name);

        // Register a cleanup action — runs when the object becomes phantom-reachable
        this.cleanable = CLEANER.register(this, new CleanupAction(nativeHandle));
        System.out.println("  Allocated resource: " + name + " (handle=" + nativeHandle + ")");
    }

    private static long allocateNative(String name) {
        System.out.println("  [native] allocate(" + name + ")");
        return System.nanoTime(); // simulated handle
    }

    // Prefer explicit cleanup over relying on Cleaner
    @Override
    public void close() {
        cleanable.clean(); // run cleanup immediately
    }

    // Cleanup action — must not reference the object (would prevent GC!)
    private record CleanupAction(long handle) implements Runnable {
        @Override
        public void run() {
            System.out.println("  [Cleaner] Freeing native handle: " + handle);
        }
    }
}

public class CleanerDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("--- Cleaner (replacing finalize()) ---");

        // Using try-with-resources (preferred — deterministic cleanup)
        try (NativeResource resource = new NativeResource("file-a.txt")) {
            System.out.println("  Using resource...");
        }
        System.out.println("  Resource explicitly closed via try-with-resources.\n");

        // Cleaner as backup (if user forgets to close)
        System.out.println("  Creating resource without closing (relying on Cleaner)...");
        new NativeResource("leaky-file.txt");
        // When the NativeResource becomes unreachable, Cleaner will eventually free it

        System.gc();
        Thread.sleep(1000);
        System.out.println("  GC triggered — Cleaner should have run by now.");
    }
}
```

## Stack vs Heap Allocation

```java
public class StackVsHeapDemo {
    // Simple value object
    record Point(int x, int y) {}

    public static void main(String[] args) {
        System.out.println("--- Stack vs Heap Allocation ---");

        // Primitives and references are allocated on the *stack*
        int a = 5;                       // stack (primitive)
        Point p = new Point(3, 4);       // reference on stack, object on *heap*
        String s = "hello";              // reference on stack, string in string pool (heap)

        System.out.println("  Primitives (int, double, boolean, etc.): stack");
        System.out.println("  Object references: stack");
        System.out.println("  Object data: heap");

        // Escape Analysis (JIT optimization):
        // If an object never "escapes" the method, JIT may allocate it on the stack
        // or even inline its fields into registers — called "scalar replacement"
        //
        // -XX:+DoEscapeAnalysis (enabled by default in server compilers)
        // -XX:+EliminateAllocations (scalar replacement)
        //
        // Example of non-escaping object (may be optimized by JIT):
        int sum = sumPoints(new Point(10, 20), new Point(30, 40));
        System.out.println("  Sum of points: " + sum);
        System.out.println("  (May be stack-allocated or scalar-replaced by JIT)");
    }

    static int sumPoints(Point p1, Point p2) {
        // p1 and p2 don't escape — JIT may eliminate heap allocation
        return p1.x() + p1.y() + p2.x() + p2.y();
    }
}
```
