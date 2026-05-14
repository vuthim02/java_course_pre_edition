# 01 — Hello World & Compilation Pipeline

Your first Java program. Covers the source → compile → run pipeline, package declarations, and the three main ways to print output.

## Hello World — Minimal

```java
// HelloWorld.java — simplest possible Java program
class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
```

Compile and run:

```bash
javac HelloWorld.java   # produces HelloWorld.class (bytecode)
java  HelloWorld         # launches JVM, loads bytecode, runs main
```

## Hello World — With Package Declaration

```java
// File: com/example/Greeting.java
// Directory structure must match the package name.
package com.example;

/**
 * A simple greeting class demonstrating the compile-and-run pipeline
 * with a package declaration.
 */
public class Greeting {
    public static void main(String[] args) {
        System.out.println("Hello from a packaged class!");
    }
}
```

Compile and run with packages:

```bash
javac com/example/Greeting.java
java  com.example.Greeting
```

## Three Printing Methods

```java
// PrintMethods.java — System.out.println vs printf vs formatted
package com.example;

public class PrintMethods {
    public static void main(String[] args) {
        String name = "Alice";
        int age = 30;
        double pi = 3.141592653589793;

        // 1 — println: simplest, appends a newline
        System.out.println("Hello, " + name + "!");
        System.out.println("You are " + age + " years old.");

        // 2 — printf: C-style format string (does NOT append newline)
        System.out.printf("Name: %s | Age: %d | Pi: %.3f%n", name, age, pi);
        // %s  = string, %d  = integer, %f  = floating-point
        // %.3f = 3 decimal places, %n  = platform-independent newline

        // 3 — formatted: Java 15+ instance method, returns a String
        String msg = "Name: %s | Age: %d | Pi: %.3f%n".formatted(name, age, pi);
        System.out.print(msg);
        // print (without ln) does NOT append a newline

        // formatted can also be called on a String literal directly
        System.out.println("Pi to 5 places: %.5f".formatted(pi));
    }
}
```

## Source → Compile → Run Pipeline

```
┌──────────────┐    javac     ┌───────────────┐    java    ┌──────────┐
│  Hello.java  │ ──────────→  │  Hello.class  │ ─────────→ │   JVM    │
│  (source)    │              │  (bytecode)   │            │ (runtime)│
└──────────────┘              └───────────────┘            └──────────┘

Step 1 — Write:      Hello.java  (plain text, .java extension)
Step 2 — Compile:    javac Hello.java   → produces Hello.class
Step 3 — Run:        java Hello         → JVM loads & executes main()
```

**Key points:**
- `javac` is the **Java compiler** — translates `.java` source to `.class` bytecode.
- `java` is the **Java launcher** — starts the JVM, loads the main class, and invokes `main`.
- The `main` method signature must be exactly `public static void main(String[] args)`.
- A `.java` file can have at most one `public` class, and the filename must match that class name.
- With packages, the directory tree must mirror the package hierarchy.
