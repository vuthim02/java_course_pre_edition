# OOP — Lesson 8: Enums & Annotations

## Enums — Type-Safe Constants

An **enum** (enumeration) is a special type that defines a fixed set of constants:

```java
public enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
```

### Why Not Just Use int/String Constants?

```java
// BAD: Using integers for days
public static final int MONDAY = 0;
public static final int TUESDAY = 1;
// ...

int today = 42;  // No error! But 42 is not a valid day!

// BAD: Using strings
public static final String MONDAY = "Monday";
String today = "Potato";  // No error!

// GOOD: Using enum
Day today = Day.MONDAY;  // Type-safe, only valid values
```

### Enums with Fields and Methods

```java
public enum Planet {
    MERCURY(3.303e23, 2.4397e6),
    VENUS(4.869e24, 6.0518e6),
    EARTH(5.976e24, 6.37814e6),
    MARS(6.421e23, 3.3972e6);

    private final double mass;    // in kg
    private final double radius;  // in meters

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double getMass() { return mass; }
    public double getRadius() { return radius; }

    // Calculated property
    public double getSurfaceGravity() {
        return G * mass / (radius * radius);
    }

    private static final double G = 6.67300E-11;  // Gravitational constant
}
```

### Enum with Behavior

```java
public enum Operation {
    ADD("+") {
        @Override public double apply(double x, double y) { return x + y; }
    },
    SUBTRACT("-") {
        @Override public double apply(double x, double y) { return x - y; }
    },
    MULTIPLY("*") {
        @Override public double apply(double x, double y) { return x * y; }
    },
    DIVIDE("/") {
        @Override public double apply(double x, double y) { return x / y; }
    };

    private final String symbol;

    Operation(String symbol) { this.symbol = symbol; }

    public String getSymbol() { return symbol; }

    // Abstract method — each constant implements it differently
    public abstract double apply(double x, double y);
}
```

```java
Operation op = Operation.ADD;
double result = op.apply(10, 5);  // 15.0
System.out.println(op.getSymbol());  // "+"
```

### Enum Methods

```java
Day today = Day.WEDNESDAY;

// ordinal() — position in enum (0-based)
System.out.println(today.ordinal());  // 2

// name() — exact string name
System.out.println(today.name());     // "WEDNESDAY"

// toString() — same as name() by default
System.out.println(today.toString()); // "WEDNESDAY"

// valueOf() — String → enum
Day d = Day.valueOf("MONDAY");        // Day.MONDAY

// values() — all enum values
for (Day day : Day.values()) {
    System.out.println(day);
}

// Compare enums
Day d1 = Day.MONDAY;
Day d2 = Day.FRIDAY;
System.out.println(d1.compareTo(d2)); // negative (MONDAY < FRIDAY)
```

## Annotations — Metadata for Code

Annotations add **metadata** to your code — they don't change behavior directly but can be read by tools, frameworks, and the compiler.

```java
@Override  // Tells compiler: "I intend to override a method"
@Deprecated  // Marks code as outdated
@SuppressWarnings("unchecked")  // Suppress compiler warnings
@FunctionalInterface  // Interface with exactly one abstract method
```

### Built-in Annotations

```java
public class Parent {
    public void doSomething() { }
}

public class Child extends Parent {
    @Override  // Compiler checks: is there actually a method to override?
    public void doSomething() { }

    @Deprecated  // Marks as outdated
    public void oldMethod() { }

    @SuppressWarnings("unchecked")  // Suppress specific warnings
    public void castSomething() {
        List<String> list = new ArrayList();
        List<String> unchecked = (List<String>)(List<?>) list;  // Warning suppressed
    }
}
```

### Creating Custom Annotations

```java
import java.lang.annotation.*;

// Define annotation
@Retention(RetentionPolicy.RUNTIME)   // Available at runtime via reflection
@Target(ElementType.METHOD)           // Can only be used on methods
public @interface LogExecution {
    String value() default "";        // Element with default
    boolean logParams() default true;
    boolean logResult() default true;
}
```

```java
public class MyService {
    @LogExecution(value = "doWork", logParams = true)
    public String doWork(String input) {
        return "Processed: " + input;
    }
}
```

### Reading Annotations at Runtime (Reflection)

```java
import java.lang.reflect.Method;

public class AnnotationProcessor {
    public static void main(String[] args) throws Exception {
        for (Method method : MyService.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(LogExecution.class)) {
                LogExecution annotation = method.getAnnotation(LogExecution.class);
                System.out.println("Method: " + method.getName());
                System.out.println("  Log value: " + annotation.value());
                System.out.println("  Log params: " + annotation.logParams());
            }
        }
    }
}
```

### Common Annotation Types

| Annotation | Purpose | Example Framework |
|-----------|---------|------------------|
| `@Override` | Compiler check | Built-in |
| `@Deprecated` | Mark outdated | Built-in |
| `@SuppressWarnings` | Hide warnings | Built-in |
| `@FunctionalInterface` | Lambda target | Built-in |
| `@SpringBootApplication` | Spring Boot app | Spring |
| `@GetMapping` | HTTP GET endpoint | Spring MVC |
| `@Autowired` | Dependency injection | Spring |
| `@Entity` | JPA entity | Hibernate |
| `@Test` | Test method | JUnit |
| `@Mock` | Mock object | Mockito |

---

### Exercises

1. Create an enum `TrafficLight` with `RED`, `YELLOW`, `GREEN`. Each should have a `duration` (seconds) and a `next()` method returning the next light.
2. Create an enum `HttpStatus` with code and description. Add a static method `fromCode(int code)`.
3. Create a custom annotation `@NotNull` with retention RUNTIME and target FIELD. Write a validator that checks fields marked with this annotation for null.
4. Use `@SuppressWarnings` properly to handle unchecked casts.
