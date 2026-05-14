# Core Java Advanced — Lesson 11: Memory Management & GC Tuning

## The JVM Memory Model (Review)

```
┌──────────────────────────────────────────────────────┐
│                    JVM MEMORY                         │
├──────────────────────────────────────────────────────┤
│  HEAP (shared)                                       │
│  ┌───────────────┬───────────────┬────────────────┐  │
│  │ Young Gen     │ Old Gen       │ Metaspace      │  │
│  │ ┌──┬──┬──┐   │               │ (class info)   │  │
│  │ │E │S0│S1│   │               │                │  │
│  │ └──┴──┴──┘   │               │                │  │
│  └───────────────┴───────────────┴────────────────┘  │
│                                                        │
│  THREAD STACKS (per-thread)                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐             │
│  │ Thread 1 │ │ Thread 2 │ │ Thread 3 │             │
│  │ (1MB)    │ │ (1MB)    │ │ (1MB)    │             │
│  └──────────┘ └──────────┘ └──────────┘             │
│                                                        │
│  NATIVE MEMORY (off-heap)                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │ DirectByteBuffers, JNI, threads, code cache      │  │
│  └──────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────┘
```

## Object Lifecycle

```
Created ──▶ Eden ──▶ Minor GC ──▶ S0 ──▶ S1 ──▶ Old Gen ──▶ Full GC ──▶ GONE
                 │            │                                   │
                 ▼            ▼                                   ▼
           GC collects     Survives (age++)                   Collected
```

## When to Tune GC?

**Only tune when you have a problem:**
- Pause time too long
- Throughput too low
- OutOfMemoryError
- Excessive GC CPU usage

### Step 1: Collect Data

```bash
# Enable GC logging (Java 9+ unified logging)
-Xlog:gc*:file=gc.log:time,uptime,level,tags

# Or with G1 specifically:
-Xlog:gc+region*=debug:file=g1.log

# View with tools:
jstat -gcutil <pid> 1000     # GC stats every second
jmap -heap <pid>             # Heap summary
jcmd <pid> GC.heap_info      # Detailed heap info
```

### Step 2: Analyze GC Logs

```bash
# Common GC log patterns:
# 1. Concurrent Mode Failure: Old Gen fills up before G1 finishes marking
#    → Increase heap or increase -XX:InitiatingHeapOccupancyPercent

# 2. Humongous Allocation: Objects > 50% of region size
#    → Increase region size with -XX:G1HeapRegionSize

# 3. To-space Exhaustion: Survivor spaces overflow
#    → Increase -XX:G1ReservePercent
```

### Step 3: Pick the Right GC

```bash
# General-purpose web app (most common):
java -Xms4g -Xmx4g -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -jar app.jar

# Low-latency (trading, gaming):
java -Xms8g -Xmx8g -XX:+UseZGC \
     -XX:AllocationRate=100 -jar app.jar

# Batch processing (throughput > latency):
java -Xms8g -Xmx8g -XX:+UseParallelGC \
     -XX:ParallelGCThreads=8 -jar app.jar

# Small heap (<4GB):
java -Xms512m -Xmx512m -XX:+UseSerialGC -jar app.jar
```

## Common GC Flags Reference

```bash
# HEAP SIZING
-Xms4g                 # Initial heap (start small, grow as needed)
-Xmx4g                 # Maximum heap (never exceed)
-XX:NewRatio=2         # Old:Young ratio (2:1 = Old is 2/3 of heap)
-XX:SurvivorRatio=8    # Eden:Survivor ratio (8:1:1)
-XX:MaxMetaspaceSize=256m  # Metaspace limit

# G1 SPECIFIC
-XX:MaxGCPauseMillis=200        # Target pause time (not a hard limit)
-XX:G1HeapRegionSize=4m         # Region size (1-32MB)
-XX:InitiatingHeapOccupancyPercent=45  # Start concurrent cycle at 45% heap
-XX:G1ReservePercent=10          # Reserve space for "to-space"
-XX:G1NewSizePercent=5           # Initial young generation size
-XX:G1MaxNewSizePercent=60       # Max young generation size
-XX:ConcGCThreads=2              # Concurrent marking threads
-XX:ParallelGCThreads=4          # Parallel STW threads

# DIAGNOSTICS
-Xlog:gc*:file=gc.log           # GC logging
-XX:+HeapDumpOnOutOfMemoryError  # Auto heap dump
-XX:HeapDumpPath=/tmp/dump.hprof  # Dump location
-XX:+PrintClassHistogram         # Class histogram at OOM
-XX:+UnlockDiagnosticVMOptions   # Unlock diagnostic flags
```

## Memory Leaks in Java

### Yes, Java CAN leak memory! Common patterns:

```java
// 1. Static collection that grows forever
public class Cache {
    private static final Map<String, Data> cache = new HashMap<>();

    public static void put(String key, Data data) {
        cache.put(key, data);  // NEVER removed!
    }
}

// 2. Inner class holding outer reference
public class Outer {
    private List<Listener> listeners = new ArrayList<>();

    class Inner {
        // Holds implicit reference to Outer!
        void doSomething() { }
    }

    public void addListener() {
        listeners.add(new Inner());  // Prevents Outer from being GC'd!
    }
}

// 3. Unclosed resources
public void readFile() throws IOException {
    FileInputStream fis = new FileInputStream("file.txt");
    // Read but NEVER close!
    // Garbage collector eventually closes, but not guaranteed timing
}

// 4. String.intern() abuse
String data = readLargeFile();
String interned = data.intern();  // Goes into permgen/metaspace FOREVER!
```

### Detecting Memory Leaks

```bash
# 1. Get a heap dump
jmap -dump:live,format=b,file=heap.hprof <pid>

# 2. Analyze with Eclipse MAT (Memory Analyzer Tool)
#    - Open heap.hprof
#    - Run "Leak Suspects" report
#    - Look for: biggest objects, accidental retains

# 3. Monitor with JConsole / VisualVM
#    - Watch: Heap, Metaspace, GC time
#    - If heap grows continuously (sawtooth but trending up) → LEAK!
```

## Profiling Tools

```bash
# Built-in (no installation needed):
jps           # List Java processes
jstat         # JVM statistics
jstack        # Thread dumps
jmap          # Memory maps and heap dumps
jcmd          # Comprehensive diagnostic command
jinfo         # JVM configuration
jhat          # Heap analysis (basic)

# Standalone tools:
VisualVM      # Visual monitoring (bundled with JDK up to 8)
JProfiler     # Commercial profiler — excellent
YourKit       # Commercial profiler — excellent
Async Profiler # Low-overhead sampling profiler (free)
```

### jcmd — The Swiss Army Knife

```bash
jcmd <pid> help                          # List all commands
jcmd <pid> VM.version                    # JVM version
jcmd <pid> VM.flags                      # All JVM flags
jcmd <pid> GC.heap_info                  # Heap details
jcmd <pid> GC.class_stats                # Class statistics
jcmd <pid> Thread.print                  # Thread dump
jcmd <pid> VM.native_memory summary      # Native memory
jcmd <pid> VM.uptime                     # How long running
```

---

### Exercises

1. Write a program that creates a memory leak. Run with `-Xmx100m -XX:+HeapDumpOnOutOfMemoryError`. Analyze the dump with Eclipse MAT.
2. Use `jvisualvm` or `jconsole` to connect to a running Java app and observe GC activity.
3. Generate a GC log from a running app and analyze it. Identify: pause times, GC frequency, heap usage after GC.
4. Experiment with different GC algorithms on the same program. Measure: throughput, average pause time, max pause time.
5. Use `jstack` to get a thread dump from a running app. Identify thread states and potential deadlocks.
