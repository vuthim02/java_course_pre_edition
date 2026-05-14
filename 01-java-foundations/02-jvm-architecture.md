# Java Foundations — Lesson 2: JVM Architecture Deep Dive

## The Mental Model

Think of Java programs like a **play**:

- **You (the developer)** = Playwright, write the script
- **javac (compiler)** = Translator, converts script to a universal language
- **.class file (bytecode)** = The translated script (same for all theaters)
- **JVM** = The theater, adapts the universal script to this specific stage
- **Operating System** = The building where the theater is located
- **Hardware** = The physical seats, lights, speakers

## The Compilation Pipeline

```
Hello.java (Source Code — human readable)
    │
    │  javac Hello.java
    ▼
Hello.class (Bytecode — JVM readable)
    │
    │  java Hello
    ▼
┌─────────────────────────────────────────────────────┐
│                     JVM                              │
│                                                      │
│  1. CLASS LOADER: Loads Hello.class into memory       │
│  2. BYTECODE VERIFIER: Checks for security violations │
│  3. INTERPRETER: Starts executing bytecode           │
│  4. JIT COMPILER: Finds "hot" code, compiles to      │
│     native machine code for that platform            │
│  5. NATIVE CODE: Runs directly on CPU (fast!)        │
└─────────────────────────────────────────────────────┘
    │
    ▼
CPU executes machine code
```

### Step-by-Step Execution

**Step 1: Compilation (javac)**
```bash
javac Hello.java
# Produces: Hello.class
```

The `.class` file contains **bytecode** — a platform-independent instruction set. This is the key to WORA.

**Step 2: Class Loading**
```bash
java Hello
```
The JVM starts and the **ClassLoader** subsystem loads your `Hello.class`:
1. **Loading** — Reads the binary data from .class file
2. **Linking** — Verifies bytecode, prepares static fields, resolves references
3. **Initialization** — Executes static initializers

**Step 3: Execution**
The JVM's execution engine runs your code:
1. **Interpreter** — Reads bytecode one instruction at a time (slow start)
2. **JIT Compiler** — Detects "hot methods" (run frequently), compiles them to **native machine code**
3. **Native code** — Runs directly on CPU (near-C speed)

### Why Two Phases? (Interpreter + JIT)

```
TIME ▶───────────────────────────────────────────▶
│                     │                           │
START               WARMUP                   FULL SPEED
│                     │                           │
└─────────────────────┴───────────────────────────┘
  Interpreter mode     JIT compiling         Running native
  (slow, gathering     hot methods           code (fast)
  profiling data)
```

This hybrid approach gives Java:
- **Fast startup** (interpreter starts immediately)
- **High peak performance** (JIT optimizes hot code)
- **Adaptive optimization** (JIT uses runtime profiling)

## JVM Runtime Data Areas

```
┌─────────────────────────────────────────────────────────────┐
│                     JVM MEMORY MODEL                         │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                     HEAP                              │    │
│  │  (Shared across ALL threads)                         │    │
│  │                                                      │    │
│  │  ┌───────────────────────────────────────────┐      │    │
│  │  │         YOUNG GENERATION                    │      │    │
│  │  │  ┌──────────┐ ┌────────┐ ┌────────┐       │      │    │
│  │  │  │  Eden    │ │  S0    │ │  S1    │       │      │    │
│  │  │  │ (new objs)│ │(survivor)│(survivor)│      │      │    │
│  │  │  └──────────┘ └────────┘ └────────┘       │      │    │
│  │  ├───────────────────────────────────────────┤      │    │
│  │  │         OLD GENERATION                     │      │    │
│  │  │  (long-lived objects)                     │      │    │
│  │  └───────────────────────────────────────────┘      │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │               METASPACE (NOT on heap)                 │    │
│  │  Class metadata, method bytecode, constant pools     │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│  ┌──────────────────────┐  ┌──────────────────────┐        │
│  │   THREAD 1 STACK     │  │   THREAD 2 STACK     │        │
│  │  ┌────────────────┐  │  │  ┌────────────────┐  │        │
│  │  │ Frame: foo()   │  │  │  │ Frame: bar()   │  │        │
│  │  │ locals, ops    │  │  │  │ locals, ops    │  │        │
│  │  ├────────────────┤  │  │  ├────────────────┤  │        │
│  │  │ Frame: main()  │  │  │  │ Frame: run()   │  │        │
│  │  └────────────────┘  │  │  └────────────────┘  │        │
│  └──────────────────────┘  └──────────────────────┘        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Heap (The Object Graveyard)

The heap is where ALL Java objects live. It's shared by all threads.

```java
public class Person {
    String name;  // 'name' String object lives on the heap
    int age;      // 'age' primitive lives on the stack
}

Person p = new Person(); // The Person object is on the heap
// The REFERENCE 'p' is on the stack
```

**Generational Design:**
- **Eden Space:** New objects are born here. Most die quickly.
- **Survivor Spaces (S0, S1):** Objects that survive one GC cycle get moved here.
- **Old Generation:** Objects that survive many GC cycles are "tenured" here.
- **Metaspace:** Class metadata (replaced Permanent Generation in Java 8).

### Stack (Per-Thread Workspace)

Each thread has its OWN stack. Stacks contain **frames** — one per method call.

```java
public void a() {
    int x = 5;      // Local variable on stack
    b();            // New frame pushed
}

public void b() {
    String s = "hi"; // Reference on stack, String object on heap
    c();            // New frame pushed
}
```

**Stack Frame Contents:**
- **Local Variables Array** — All method parameters and local variables
- **Operand Stack** — Where computations happen (push/pop)
- **Frame Data** — Constant pool resolution, exception handler table

### Stack vs Heap — The Great Analogy

| Aspect | Stack | Heap |
|--------|-------|------|
| **Analogy** | A desk — you work here, limited space | A warehouse — everything is stored here |
| **Size** | Small (default ~1MB per thread) | Large (can be GB) |
| **Speed** | Very fast (LIFO access) | Slower (random access) |
| **Lifetime** | Scope-based — pops when method returns | GC-based — lives until unreachable |
| **Threads** | Each thread has its own | Shared across all threads |
| **Contains** | Primitives, object references | Actual objects |
| **Error** | `StackOverflowError` (too deep recursion) | `OutOfMemoryError` (heap full) |

## Garbage Collection

### Why Automatic GC?

In C/C++: `malloc()` → use → `free()` or `delete`. Problems:
- **Memory leak:** Forget to free → memory fills up → crash
- **Dangling pointer:** Free too early → use after free → crash/corruption
- **Double free:** Free twice → crash

In Java: `new` → use → **GC automatically reclaims when unreachable**.

### How GC Works (Simplified)

```
1. ALLOCATE: new Object() → placed in Eden
2. MINOR GC: Eden fills up → pause threads → copy live objects to S0 → clear Eden
3. PROMOTE: Objects surviving multiple Minor GCs → promoted to Old
4. MAJOR GC: Old fills up → full GC, compact heap
```

### The Generational Hypothesis

**"Most objects die young."** ~90% of objects are short-lived.

```
OBJECT LIFETIME DISTRIBUTION
Count
│
│  ████████████████  Most objects (temporary, loop variables, etc.)
│  ████████
│  ████            Some objects (request handlers, etc.)
│  ██              Few objects (services, caches, etc.)
│  █               Rare objects (application singletons)
└──────────────────────────▶ Time
```

This is why GC focuses on the Young Generation — collecting there is fast and reclaims most memory.

### GC Algorithms Compared

| GC | Philosophy | Pause Time | Throughput | Best For |
|----|-----------|------------|------------|----------|
| **Serial** | "Single thread, stop the world" | Long (seconds) | Low | Small apps, single CPU |
| **Parallel** | "Many GC threads, stop the world" | Long | High | Batch processing, analytics |
| **G1** (default since Java 9) | "Region-based, predictable pauses" | Medium (~200ms) | High | **Most applications** |
| **ZGC** (Java 15+) | "Concurrent, almost no pauses" | <1ms | Medium | Low-latency trading, gaming |
| **Shenandoah** (Java 12+) | "Concurrent compaction" | <10ms | Medium | Large heaps, consistent response |

### Visualizing G1 GC

```
HEAP DIVIDED INTO ~2048 REGIONS (1-32MB each)

┌──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┬──┐
│E │E │E │E │E │E │S │S │O │O │O │O │O │O │H │H │
├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
│E │E │E │E │O │O │O │O │O │O │O │O │O │H │H │H │
├──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──┤
│E │E │E │E │E │E │E │S │S │O │O │O │O │O │O │O │
└──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┴──┘
 E = Eden (young)    S = Survivor    O = Old    H = Humongous
```

G1 chooses which regions to collect based on which have the **most garbage** — hence "Garbage-First."

## Bytecode: The Universal Language

### Reading Bytecode (javap)

```java
// SimpleClass.java
public class SimpleClass {
    public int add(int a, int b) {
        return a + b;
    }
}
```

Compile and decompile:
```bash
javac SimpleClass.java
javap -c SimpleClass
```

Output:
```
Compiled from "SimpleClass.java"
public class SimpleClass {
  public SimpleClass();
    Code:
       0: aload_0
       1: invokespecial #1   // Method java/lang/Object."<init>":()V
       4: return

  public int add(int, int);
    Code:
       0: iload_1            // Push 'a' (local var 1) onto stack
       1: iload_2            // Push 'b' (local var 2) onto stack
       2: iadd               // Pop both, add them, push result
       3: ireturn            // Return the result
}
```

### Common Bytecode Instructions

| Category | Instructions | What They Do |
|----------|-------------|--------------|
| **Load** | `aload_0`, `iload_1` | Push local variable onto stack |
| **Store** | `astore_0`, `istore_1` | Pop stack into local variable |
| **Arithmetic** | `iadd`, `isub`, `imul` | Pop two values, operate, push result |
| **Stack** | `dup`, `pop`, `swap` | Manipulate operand stack |
| **Control** | `ifeq`, `goto`, `tableswitch` | Conditional/unconditional jumps |
| **Invoke** | `invokevirtual`, `invokestatic` | Call methods |
| **Return** | `ireturn`, `areturn`, `return` | Return from method |
| **Object** | `new`, `getfield`, `putfield` | Create/access objects |

## JVM Tuning Essentials

```bash
# Heap sizing
-Xms512m          # Initial heap (start with this)
-Xmx4g            # Maximum heap (never allocate more than this)
-Xss1m            # Thread stack size (default 1MB per thread)

# GC selection
-XX:+UseG1GC      # Default since Java 9 — balanced throughput/pause
-XX:+UseZGC       # Sub-millisecond pauses (Java 15+, large heaps)
-XX:+UseShenandoahGC  # Concurrent compaction (Java 12+)

# GC tuning (G1)
-XX:MaxGCPauseMillis=200   # Target max pause time
-XX:ParallelGCThreads=4     # Parallel GC threads
-XX:ConcGCThreads=2         # Concurrent GC threads

# Diagnostics
-XX:+PrintGCDetails
-XX:+PrintHeapAtGC
-Xlog:gc*                   # Java 9+ unified logging
```

### Real-World Example: Tuning a Web Server

Problem: Web server pauses for 2 seconds during GC every 5 minutes.

```bash
# Before (defaults):
java -jar myapp.jar
# GC logs show: 2.0s pauses, Old Gen filling up

# After tuning:
java -Xms4g -Xmx4g -XX:MaxGCPauseMillis=100 -XX:+UseG1GC \
     -Xlog:gc:gc.log -jar myapp.jar
# Result: 100ms pauses, more frequent but shorter
```

---

### Exercises

1. Run `java -XX:+PrintFlagsFinal -version | grep -i gc` to see all GC flags for your JVM.
2. Write a program that causes `StackOverflowError`. Read the stack trace.
3. Write a program with an infinite object creation loop. Run with `-Xmx10m` and watch the `OutOfMemoryError`.
4. Compile a simple class and use `javap -c -verbose` to read the full bytecode.
5. Use `jvisualvm` or `jconsole` to connect to a running Java process and observe heap usage.
