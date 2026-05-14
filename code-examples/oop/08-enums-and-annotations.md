# Enums and Annotations

Enums are full-featured classes with a fixed set of constants. They can have fields, constructors, methods, and implement interfaces. Annotations provide metadata; you can define custom annotations with retention and target policies and process them at runtime via reflection.

```java
import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;

// ============================================================
// 1. Custom annotation definition
// ============================================================

@Retention(RetentionPolicy.RUNTIME)   // available at runtime via reflection
@Target({ElementType.TYPE, ElementType.METHOD})
@interface AuthorInfo {
    String author();
    String date();
    int version() default 1;
}

// ============================================================
// 2. Simple enum
// ============================================================

enum Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

// ============================================================
// 3. Enum with fields, constructor, methods
// ============================================================

enum Planet {
    MERCURY(3.303e23, 2.4397e6),
    VENUS  (4.869e24, 6.0518e6),
    EARTH  (5.976e24, 6.37814e6),
    MARS   (6.421e23, 3.3972e6);

    private final double mass;      // kg
    private final double radius;    // m

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double mass()   { return mass; }
    public double radius() { return radius; }

    // Universal gravitational constant (m^3 kg^-1 s^-2)
    public static final double G = 6.67430e-11;

    public double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    public double surfaceWeight(double otherMass) {
        return otherMass * surfaceGravity();
    }
}

// ============================================================
// 4. Enum with abstract method (constant-specific behavior)
// ============================================================

enum Operation {
    PLUS   { double apply(double x, double y) { return x + y; } },
    MINUS  { double apply(double x, double y) { return x - y; } },
    TIMES  { double apply(double x, double y) { return x * y; } },
    DIVIDE { double apply(double x, double y) { return x / y; } };

    abstract double apply(double x, double y);
}

// ============================================================
// 5. EnumSet and EnumMap
// ============================================================

class EnumCollectionsDemo {
    static void demo() {
        // EnumSet — highly efficient bit-vector implementation
        Set<Day> weekend = EnumSet.of(Day.SATURDAY, Day.SUNDAY);
        Set<Day> weekdays = EnumSet.range(Day.MONDAY, Day.FRIDAY);
        Set<Day> all = EnumSet.allOf(Day.class);
        System.out.println("Weekend: " + weekend);
        System.out.println("Weekdays: " + weekdays);

        // EnumMap — array-backed, very fast
        Map<Day, String> schedule = new EnumMap<>(Day.class);
        schedule.put(Day.MONDAY, "Meeting at 9am");
        schedule.put(Day.FRIDAY, "Team standup");
        System.out.println("Monday: " + schedule.get(Day.MONDAY));
    }
}

// ============================================================
// 6. Using custom annotations
// ============================================================

@AuthorInfo(author = "Alice", date = "2025-01-15")
class AnnotatedService {

    @AuthorInfo(author = "Bob", date = "2025-02-01", version = 2)
    public void serve() {
        System.out.println("Serving...");
    }

    @Deprecated
    @AuthorInfo(author = "Alice", date = "2024-12-01")
    public void oldMethod() {
        System.out.println("This is old");
    }
}

// ============================================================
// 7. Processing annotations at runtime via reflection
// ============================================================

class AnnotationProcessor {
    static void inspect(Class<?> clazz) {
        // Class-level annotation
        AuthorInfo classInfo = clazz.getAnnotation(AuthorInfo.class);
        if (classInfo != null) {
            System.out.printf("Class %s: author=%s, date=%s, version=%d%n",
                clazz.getSimpleName(), classInfo.author(), classInfo.date(), classInfo.version());
        }

        // Method-level annotations
        for (Method m : clazz.getDeclaredMethods()) {
            AuthorInfo mi = m.getAnnotation(AuthorInfo.class);
            if (mi != null) {
                System.out.printf("  Method %s: author=%s, date=%s, version=%d%n",
                    m.getName(), mi.author(), mi.date(), mi.version());
            }

            if (m.isAnnotationPresent(Deprecated.class)) {
                System.out.printf("  Method %s is @Deprecated%n", m.getName());
            }
        }
    }
}

// ============================================================
// Main
// ============================================================

public class EnumsAnnotationsDemo {
    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        // --- Basic enum usage ---
        Day today = Day.WEDNESDAY;
        System.out.println("Today: " + today);

        // --- Enum in switch expression ---
        String type = switch (today) {
            case SATURDAY, SUNDAY -> "Weekend!";
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
        };
        System.out.println("Day type: " + type);

        // --- Enum with fields ---
        double earthWeight = 80;  // kg
        double mass = earthWeight / Planet.EARTH.surfaceGravity();
        for (Planet p : Planet.values()) {
            System.out.printf("Weight on %s: %.2f N%n", p, p.surfaceWeight(mass));
        }

        // --- Enum with abstract methods ---
        System.out.println("3 + 4 = " + Operation.PLUS.apply(3, 4));
        System.out.println("10 / 3 = " + Operation.DIVIDE.apply(10, 3));

        // --- EnumSet / EnumMap ---
        EnumCollectionsDemo.demo();

        // --- Annotation processing ---
        System.out.println("\n--- Annotation Processing ---");
        AnnotationProcessor.inspect(AnnotatedService.class);
    }
}
```
