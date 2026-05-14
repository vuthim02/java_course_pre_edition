# Core Java Advanced — Lesson 6: Java Module System (JPMS)

## What is the Module System?

Introduced in **Java 9**, the module system (Project Jigsaw) adds another layer of encapsulation above packages.

```
Package level:     public, protected, default, private
Module level:      exports, opens, requires
```

Before modules, ALL public classes in a JAR were accessible. With modules, you control exactly which packages are visible.

## Module Basics

A module is a JAR with a `module-info.java` file:

```java
// src/main/java/module-info.java
module com.example.myapp {
    requires com.example.dependency;  // Module dependency
    exports com.example.myapp.api;    // Public API — visible to other modules
}
```

## Creating a Module

### Step 1: Create module-info.java

```java
// module-info.java
module com.example.calculator {
    exports com.example.calculator.api;
}
```

### Step 2: Your code

```java
// com/example/calculator/api/Calculator.java
package com.example.calculator.api;

public class Calculator {
    public int add(int a, int b) { return a + b; }
}
```

```java
// com/example/calculator/internal/SecretHelper.java
package com.example.calculator.internal;

// This class is NOT exported — hidden from other modules!
class SecretHelper {
    static void log(String msg) { System.out.println(msg); }
}
```

### Step 3: Compile and run

```bash
# Compile module
javac -d mods/com.example.calculator \
    src/module-info.java \
    src/com/example/calculator/api/Calculator.java

# Run module
java --module-path mods -m com.example.calculator/com.example.calculator.api.Calculator
```

## Module Keywords

```java
module com.example.myapp {
    // REQUIRES — declares dependency on another module
    requires java.base;                    // Always present (implicit)
    requires java.sql;                     // For JDBC
    requires transitive java.logging;      // Also makes java.logging available to consumers
    requires static java.desktop;          // Optional at runtime

    // EXPORTS — makes packages accessible
    exports com.example.myapp.api;         // Public API
    exports com.example.myapp.dto;

    // OPENS — allows reflection on packages
    opens com.example.myapp.model;         // For frameworks like Hibernate/Jackson
    opens com.example.myapp.internal to com.example.test;  // Only to specific module

    // PROVIDES / USES — ServiceLoader
    provides com.example.spi.Plugin with com.example.myapp.MyPlugin;
    uses com.example.spi.Plugin;
}
```

## Common Modules in java.base

The `java.base` module is always present. Key packages:

```
java.base
├── java.lang          — String, System, Thread, etc.
├── java.util          — Collections, Streams
├── java.io            — I/O
├── java.math          — BigInteger, BigDecimal
├── java.net           — Networking
├── java.nio           — NIO
├── java.time          — Date/Time API
├── java.security      — Security
└── java.text          — Formatting
```

## Why Modules Matter

### 1. Strong Encapsulation

```java
// Without modules: any public class is accessible
// With modules: only exported packages are accessible

// This class is in an unexported package
package com.example.internal;

public class PrivateHelper {
    // Can't be accessed from outside the module!
}
```

### 2. Explicit Dependencies

```java
// BEFORE MODULES: Hidden dependency
// You use com.sun.* internals without declaring it

// WITH MODULES: Explicit requires
module myapp {
    requires java.sql;  // Clear, documented dependency
}
```

### 3. Smaller Runtime (JLink)

```bash
# Create custom JRE with only needed modules
jlink --module-path $JAVA_HOME/jmods:mods \
      --add-modules com.example.myapp \
      --output myapp-runtime

# Now myapp-runtime/bin/java can run your app
# Size: ~40MB instead of ~300MB
```

## Migration Tips

For existing projects, start with:

```java
// For existing JARs that aren't modular:
module myapp {
    requires java.base;
    // For non-modular JARs:
    requires commons.lang;  // Auto-module from JAR name
}

// To open everything temporarily:
// --add-exports java.base/sun.security.provider=ALL-UNNAMED
```

---

### Exercises

1. Create two modules: `com.example.logger` and `com.example.app`. Have `app` use `logger`.
2. Export only an API package, keeping internal packages hidden. Verify from another module.
3. Use `opens` to allow a framework (like Hibernate) to access private fields via reflection.
4. Create a custom runtime image with `jlink` for a simple application.
