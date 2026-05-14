# OOP — Lesson 7: Records, Sealed Classes & Pattern Matching

## Records (Java 16+)

A **record** is a concise way to create "data carriers" — classes that just hold data. Java generates the constructor, getters, equals, hashCode, and toString automatically.

### Before Records (The Boilerplate)

```java
public class Point {
    private final int x;
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return x == point.x && y == point.y;
    }

    @Override
    public int hashCode() { return Objects.hash(x, y); }

    @Override
    public String toString() {
        return "Point[x=" + x + ", y=" + y + "]";
    }
}
```

### After Records (ONE line!)

```java
public record Point(int x, int y) { }
// Java generates: constructor, x(), y(), equals(), hashCode(), toString()
// ALL fields are private final
```

### Using Records

```java
Point p = new Point(3, 5);
System.out.println(p.x());       // 3 (automatic getter, NOT getX())
System.out.println(p.y());       // 5
System.out.println(p);           // Point[x=3, y=5]

Point p2 = new Point(3, 5);
System.out.println(p.equals(p2)); // true

// Records are immutable — can't change fields
// p.x = 10;  // COMPILE ERROR!
```

### Customizing Records

```java
public record Rectangle(double width, double height) {
    // Compact constructor — no need to assign fields
    public Rectangle {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Dimensions must be positive");
        }
        // this.width = width;  ← Java does this automatically
    }

    // Additional methods
    public double area() {
        return width * height;
    }

    // Static factory
    public static Rectangle square(double side) {
        return new Rectangle(side, side);
    }
}
```

### When to Use Records vs Classes

| Use Records When | Use Classes When |
|-----------------|-----------------|
| Data carrier/DTO | Has behavior beyond data |
| Immutable data | Needs mutable state |
| Transparent data | Needs implementation hiding |
| Interchange/API types | Complex business logic |

## Sealed Classes (Java 17+)

A **sealed class** controls WHICH classes can extend it. This creates a **closed hierarchy**.

```java
// Sealed: only PermittedChild1 and PermittedChild2 can extend
public sealed class Shape permits Circle, Rectangle, Triangle {
    // ...
}

// Child must be: final, sealed, or non-sealed
public final class Circle extends Shape { ... }       // Can't be extended further
public sealed class Rectangle extends Shape permits Square { ... }  // Can be extended by Square only
public non-sealed class Triangle extends Shape { ... }  // Open for extension
```

### Why Sealed Classes?

Without sealed classes, ANYONE could extend your class:

```java
// Someone else could create:
public class Pentagon extends Shape { ... }  // What? Not in our system!
```

With sealed classes, the hierarchy is **exhaustive** — the compiler knows ALL subtypes:

```java
// Pattern matching can be exhaustive:
double area = switch (shape) {
    case Circle c -> Math.PI * c.radius() * c.radius();
    case Rectangle r -> r.width() * r.height();
    case Triangle t -> 0.5 * t.base() * t.height();
    // NO default needed! All subtypes covered.
};
```

## Pattern Matching for instanceof (Java 16+)

Old way:
```java
if (obj instanceof String) {
    String s = (String) obj;  // Explicit cast
    System.out.println(s.length());
}
```

New way:
```java
if (obj instanceof String s) {  // Pattern variable — automatically cast!
    System.out.println(s.length());
}
```

### Combined with Records (Deconstruction)

```java
record Point(int x, int y) {}

void printPoint(Object obj) {
    if (obj instanceof Point(int x, int y)) {  // Deconstruct!
        System.out.println("Point at (" + x + ", " + y + ")");
    }
}
```

## Pattern Matching for switch (Java 21+)

```java
Object obj = "Hello, Java 21!";

String result = switch (obj) {
    case null        -> "It's null!";
    case Integer i   -> "Integer: " + i;
    case String s    -> "String: " + s;
    case Long l      -> "Long: " + l;
    case Point(int x, int y) -> "Point at (" + x + ", " + y + ")";
    case int[] arr   -> "Array of length " + arr.length;
    default          -> "Unknown type: " + obj.getClass();
};

// With guards:
String description = switch (obj) {
    case Integer i && i > 100 -> "Large integer: " + i;
    case Integer i            -> "Small integer: " + i;
    case String s && !s.isEmpty() -> "Non-empty string: " + s;
    case String s             -> "Empty string";
    default                   -> "Something else";
};
```

---

### Exercises

1. Convert a `Book` class (with title, author, year) into a record. Add a compact constructor that validates the year.
2. Create a sealed hierarchy `Vehicle` with `Car`, `Truck`, `Motorcycle`. Add an `area()` method to each.
3. Use pattern matching for switch to process different types in a `List<Object>`.
4. Combine records with pattern matching: create a record `Pair<T>` and destructure it in a switch.
