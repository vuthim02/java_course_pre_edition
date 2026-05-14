# OOP — Lesson 3: static & final

## The `static` Keyword

**`static` means "belongs to the CLASS, not to any INSTANCE."**

```
Class: Counter                   Objects:
┌────────────────────┐           ┌──────────────────┐
│ count: int (static)│◀─shared──▶│ Counter c1       │
│                    │           │ count = 3        │
│ + increment()      │           └──────────────────┘
│ + getCount()       │           ┌──────────────────┐
└────────────────────┘           │ Counter c2       │
                                 │ count = 3 (same!)│
                                 └──────────────────┘
```

### Static Fields — One Value for ALL Instances

```java
public class Counter {
    private static int count = 0;  // Shared across ALL Counter objects
    private int instanceId;        // Unique to EACH Counter object

    public Counter() {
        count++;                  // Increment shared counter
        this.instanceId = count;  // Each gets a unique ID
    }

    public static int getCount() {  // Static method
        return count;
    }

    public int getInstanceId() {
        return instanceId;
    }
}
```

```java
Counter c1 = new Counter();  // count = 1, c1.id = 1
Counter c2 = new Counter();  // count = 2, c2.id = 2
Counter c3 = new Counter();  // count = 3, c3.id = 3

System.out.println(Counter.getCount());  // 3 (call via CLASS)
System.out.println(c1.getCount());      // 3 (also works, but don't)
System.out.println(c1.getInstanceId()); // 1
System.out.println(c3.getInstanceId()); // 3
```

### Static Methods — No Object Needed

```java
public class MathUtils {
    public static int add(int a, int b) {
        return a + b;
    }

    public static int max(int a, int b) {
        return a > b ? a : b;
    }
}

// Called on the CLASS, not an instance:
int sum = MathUtils.add(5, 3);  // No 'new MathUtils()' needed!
int max = MathUtils.max(10, 7);
```

**Static methods CANNOT access instance fields/methods:**
```java
public class Example {
    private int instanceVar = 5;
    private static int staticVar = 10;

    public static void staticMethod() {
        System.out.println(instanceVar);  // COMPILE ERROR!
        System.out.println(staticVar);    // OK
        instanceMethod();                 // COMPILE ERROR!
    }

    public void instanceMethod() {
        System.out.println(instanceVar);  // OK
        System.out.println(staticVar);    // OK (static is always visible)
        staticMethod();                   // OK
    }
}
```

### Static Initializer Block

Runs ONCE when the class is first loaded:

```java
public class DatabaseConfig {
    private static String dbUrl;
    private static String dbUser;
    private static String dbPassword;

    static {
        // This runs ONCE when the class is loaded
        System.out.println("Loading database configuration...");
        dbUrl = System.getenv("DB_URL");
        dbUser = System.getenv("DB_USER");
        dbPassword = System.getenv("DB_PASSWORD");

        if (dbUrl == null) {
            throw new RuntimeException("DB_URL environment variable not set!");
        }
    }

    public static String getDbUrl() { return dbUrl; }
}
```

### Static Inner Classes

```java
public class Outer {
    private static String staticOuterField = "static";

    // Static inner class — can exist without Outer instance
    public static class Nested {
        public void display() {
            System.out.println(staticOuterField);  // Can access static outer
            // System.out.println(instanceField);  // ERROR: can't access instance
        }
    }
}

// Usage — no Outer instance needed:
Outer.Nested nested = new Outer.Nested();
```

### When to Use Static

| Use Case | Example |
|----------|---------|
| **Constants** | `public static final double PI = 3.14159;` |
| **Utility methods** | `Collections.sort()`, `Math.max()` |
| **Factory methods** | `Integer.valueOf(42)`, `List.of(1,2,3)` |
| **Singleton instance** | `private static final Singleton INSTANCE = new Singleton()` |
| **Counters/IDs** | Auto-incrementing IDs |
| **Main method** | `public static void main(String[] args)` |

## The `final` Keyword

`final` means "this can be set ONCE and never changed."

### final Variables — Constants

```java
public class Constants {
    // Primitive constant
    public static final double PI = 3.14159265359;

    // Object constant (reference can't change, object CAN change!)
    public static final List<String> DAYS = List.of("Mon", "Tue", "Wed");
    // DAYS = new ArrayList<>();  // ERROR!
    // BUT: DAYS is already immutable (List.of)

    // Instance constant — set once in constructor
    private final int id;

    public Constants(int id) {
        this.id = id;  // Set once
        // this.id = 5;  // ERROR! Already set
    }
}
```

### final Methods — Cannot Be Overridden

```java
public class Parent {
    public final void importantMethod() {
        System.out.println("This CANNOT be overridden");
    }
}

public class Child extends Parent {
    // @Override  — COMPILE ERROR!
    public void importantMethod() {
        System.out.println("Trying to override");
    }
}
```

### final Classes — Cannot Be Extended

```java
public final class String {
    // String class in Java is final — you can't extend it
}

public class MyString extends String {  // COMPILE ERROR!
}
```

## static + final = Constants

The most common combination:

```java
public class AppConstants {
    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final String APP_NAME = "MyApplication";
    public static final long TIMEOUT_MS = 30_000L;
    public static final Path CONFIG_PATH = Path.of("/etc/myapp/config.yml");
}
```

**Naming convention:** ALL_CAPS with underscores for constants.

---

### Exercises

1. Create a `Library` class with a static count of total books. Each new book increments the count. Create a static method to get total books.
2. Create a `StringUtils` utility class with ONLY static methods: `reverse()`, `countVowels()`, `isPalindrome()`.
3. Create a `final` class `MathConstants` with `public static final` constants for PI, E, GOLDEN_RATIO.
4. Use a static initializer to load configuration from a properties file.
5. Show the difference between `final` on a primitive vs `final` on an object reference (you can modify the object but not reassign the reference).
