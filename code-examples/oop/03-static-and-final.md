# Static and Final

The `static` keyword associates members with the class rather than instances. The `final` keyword prevents modification: final fields are constants, final methods cannot be overridden, and final classes cannot be extended. A `static final` field is a true constant.

```java
import static java.lang.Math.PI;         // static import
import static java.lang.Math.sqrt;        // static import

// ============================================================
// 1. Static fields vs instance fields
// ============================================================

class Counter {
    static int totalCount = 0;    // shared across all instances
    int instanceId;               // per-instance

    public Counter() {
        totalCount++;
        instanceId = totalCount;
    }
}

// ============================================================
// 2. Static methods (utility / factory)
// ============================================================

class MathUtils {
    private MathUtils() {}   // prevent instantiation

    public static int max(int a, int b) {
        return a > b ? a : b;
    }

    // Static method using static import (see bottom)
    public static double hypotenuse(double a, double b) {
        return sqrt(a * a + b * b);
    }
}

// ============================================================
// 3. Static initialization block
// ============================================================

class DatabaseConfig {
    static String url;
    static int port;

    static {
        // Runs once when the class is first loaded
        url = "jdbc:postgresql://localhost";
        port = 5432;
        System.out.println("Static block: DatabaseConfig initialized");
    }
}

// ============================================================
// 4. Final fields (constants) and blank final fields
// ============================================================

class Circle {
    // Constant: public static final
    public static final double DEFAULT_RADIUS = 1.0;

    private final double radius;    // blank final — assigned in constructor

    public Circle(double radius) {
        this.radius = radius;       // blank final must be assigned exactly once
    }

    public double area() {
        return PI * radius * radius;   // PI from static import
    }
}

// ============================================================
// 5. Final method (cannot override)
// ============================================================

class Base {
    public final void cannotOverride() {
        System.out.println("This is final");
    }

    public void canOverride() {
        System.out.println("This can be overridden");
    }
}

class Derived extends Base {
    // @Override
    // public void cannotOverride() {}   // COMPILE ERROR

    @Override
    public void canOverride() {
        System.out.println("Overridden!");
    }
}

// ============================================================
// 6. Final class (cannot extend)
// ============================================================

final class ImmutableStringHolder {
    private final String value;
    public ImmutableStringHolder(String value) { this.value = value; }
    public String getValue() { return value; }
}

// class MyString extends ImmutableStringHolder {}  // COMPILE ERROR

// ============================================================
// 7. Main
// ============================================================

public class StaticFinalDemo {
    public static void main(String[] args) {
        // Static field shared across instances
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        System.out.println("totalCount = " + Counter.totalCount);   // 2
        System.out.println("c1.instanceId = " + c1.instanceId);     // 1
        System.out.println("c2.instanceId = " + c2.instanceId);     // 2

        // Static method
        System.out.println(MathUtils.max(10, 20));                  // 20
        System.out.println(MathUtils.hypotenuse(3, 4));             // 5.0

        // Static block already ran

        // Final field
        Circle c = new Circle(5);
        System.out.println("Area = " + c.area());                   // ~78.54
        // c.radius = 10;  // COMPILE ERROR

        // Constant via static import
        System.out.println("PI = " + PI);
    }
}
```
