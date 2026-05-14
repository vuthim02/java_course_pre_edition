# Records, Sealed Classes, and Pattern Matching

Records (Java 16+) are transparent carriers for immutable data — they automatically generate constructor, accessors, `equals`, `hashCode`, and `toString`. Sealed classes (Java 17+) restrict which subclasses are permitted, enabling exhaustive pattern matching. Pattern matching for `instanceof` (Java 16+) and `switch` (Java 21+) makes code safer and more readable.

```java
// ============================================================
// 1. Records — compact data carriers
// ============================================================

record Point(int x, int y) {
    // Compact constructor — no field assignment needed
    public Point {
        if (x < 0 || y < 0) {
            throw new IllegalArgumentException("Coordinates must be non-negative");
        }
    }

    // Additional methods
    public double distanceFromOrigin() {
        return Math.sqrt(x * x + y * y);
    }

    // Static field and method
    public static Point ORIGIN = new Point(0, 0);

    public static Point midpoint(Point a, Point b) {
        return new Point((a.x() + b.x()) / 2, (a.y() + b.y()) / 2);
    }
}

// ============================================================
// 2. Sealed class hierarchy (Java 17+)
// ============================================================

sealed abstract class Vehicle permits Car, Truck, Motorcycle {
    protected final String licensePlate;

    public Vehicle(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public abstract int getMaxPassengers();
}

// Subclasses must be final, sealed, or non-sealed
final class Car extends Vehicle {
    public Car(String plate) { super(plate); }

    @Override
    public int getMaxPassengers() { return 5; }
}

non-sealed class Truck extends Vehicle {
    public Truck(String plate) { super(plate); }

    @Override
    public int getMaxPassengers() { return 2; }
}

final class Motorcycle extends Vehicle {
    public Motorcycle(String plate) { super(plate); }

    @Override
    public int getMaxPassengers() { return 1; }
}

// non-sealed means this could be extended further
class PickupTruck extends Truck {
    public PickupTruck(String plate) { super(plate); }

    @Override
    public int getMaxPassengers() { return 4; }
}

// ============================================================
// 3. Sealed interface
// ============================================================

sealed interface JsonValue permits JsonString, JsonNumber, JsonArray {}

record JsonString(String value) implements JsonValue {}
record JsonNumber(double value) implements JsonValue {}
record JsonArray(JsonValue... values) implements JsonValue {}

// ============================================================
// 4. Pattern matching for instanceof (Java 16+)
// ============================================================

class InstanceofPatternDemo {
    static String describe(Object obj) {
        if (obj instanceof String s && !s.isEmpty()) {
            return "Non-empty string of length " + s.length();
        }
        if (obj instanceof Integer i && i > 0) {
            return "Positive integer: " + i;
        }
        if (obj instanceof Point p) {
            return "Point(" + p.x() + ", " + p.y() + ")";
        }
        return "Unknown: " + obj;
    }
}

// ============================================================
// 5. Pattern matching for switch (Java 21+)
// ============================================================

class SwitchPatternDemo {
    static String classify(Object obj) {
        return switch (obj) {
            case null        -> "null";
            case String s    -> "String: " + s;
            case Integer i   -> "Integer: " + i;
            case Point p     -> "Point: " + p;
            case Vehicle v   -> "Vehicle with " + v.getMaxPassengers() + " seats";
            default          -> "Unknown type: " + obj.getClass().getSimpleName();
        };
    }

    // Exhaustive switch on sealed type (no default needed)
    static String describeVehicle(Vehicle v) {
        return switch (v) {
            case Car c         -> "Car [" + c.licensePlate + "]";
            case Truck t       -> "Truck [" + t.licensePlate + "]";
            case Motorcycle m  -> "Motorcycle [" + m.licensePlate + "]";
        };
    }

    // Guarded patterns (Java 17+)
    static String describeNumber(Object obj) {
        return switch (obj) {
            case Integer i when i < 0  -> "Negative integer: " + i;
            case Integer i when i == 0 -> "Zero";
            case Integer i             -> "Positive integer: " + i;
            default -> "Not an integer";
        };
    }

    // Record patterns (Java 21+)
    static String describePoint(Object obj) {
        return switch (obj) {
            case Point(int x, int y) -> "Point decomposed: x=" + x + ", y=" + y;
            default                  -> "Not a point";
        };
    }

    // Nested record pattern
    record LabeledPoint(String label, Point point) {}
    static String describeLabeledPoint(Object obj) {
        return switch (obj) {
            case LabeledPoint(var label, Point(int x, int y))
                -> label + " at (" + x + ", " + y + ")";
            default -> "Not a labeled point";
        };
    }
}

// ============================================================
// Main
// ============================================================

public class RecordsSealedPatternsDemo {
    public static void main(String[] args) {
        // --- Records ---
        Point p1 = new Point(3, 4);
        Point p2 = new Point(0, 0);
        System.out.println(p1);                         // Point[x=3, y=4]
        System.out.println("Distance: " + p1.distanceFromOrigin());  // 5.0
        System.out.println("Midpoint: " + Point.midpoint(p1, p2));   // Point[x=1, y=2]

        // --- Sealed classes ---
        Vehicle v1 = new Car("ABC-123");
        Vehicle v2 = new PickupTruck("XYZ-789");
        System.out.println(SwitchPatternDemo.describeVehicle(v1));

        // --- Pattern matching instanceof ---
        System.out.println(InstanceofPatternDemo.describe("hello"));
        System.out.println(InstanceofPatternDemo.describe(42));
        System.out.println(InstanceofPatternDemo.describe(p1));

        // --- Pattern matching switch ---
        System.out.println(SwitchPatternDemo.classify("Java 21"));
        System.out.println(SwitchPatternDemo.describeNumber(-5));

        // --- Record patterns ---
        System.out.println(SwitchPatternDemo.describePoint(p1));
        var lp = new SwitchPatternDemo.LabeledPoint("Home", new Point(10, 20));
        System.out.println(SwitchPatternDemo.describeLabeledPoint(lp));
    }
}
```
