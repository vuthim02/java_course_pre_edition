# Lesson 97: Enums

## Key Concepts
- `enum` defines a fixed set of named constants
- Enums can have fields, constructors, and methods
- Each enum constant can pass arguments to the constructor
- `values()` returns an array of all enum constants
- Enums are implicitly `final` and extend `java.lang.Enum`
- They can be used in `switch` statements and implement interfaces

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("=== Enum Demo ===\n");

        Day today = Day.WEDNESDAY;

        System.out.println("Today is: " + today);
        System.out.println("Is weekday? " + today.isWeekday());

        System.out.println("\nAll days:");
        for (Day day : Day.values()) {
            System.out.printf("%-10s %s%n", day, day.getType());
        }

        System.out.println("\nPlanet weights:");
        double earthWeight = 70.0;
        for (Planet planet : Planet.values()) {
            double weight = planet.weightOnPlanet(earthWeight);
            System.out.printf("Weight on %s: %.2f kg%n", planet, weight);
        }

        Level level = Level.HIGH;
        System.out.println("\nLevel: " + level);
        System.out.println("Level code: " + level.getLevelCode());
        System.out.println("Level description: " + level.getDescription());
    }
}

enum Day {
    MONDAY(true), TUESDAY(true), WEDNESDAY(true), THURSDAY(true), FRIDAY(true),
    SATURDAY(false), SUNDAY(false);

    private boolean weekday;

    Day(boolean weekday) {
        this.weekday = weekday;
    }

    boolean isWeekday() {
        return weekday;
    }

    String getType() {
        return weekday ? "Weekday" : "Weekend";
    }
}

enum Planet {
    MERCURY(3.303e23, 2.4397e6),
    VENUS(4.869e24, 6.0518e6),
    EARTH(5.976e24, 6.37814e6),
    MARS(6.421e23, 3.3972e6),
    JUPITER(1.9e27, 7.1492e7),
    SATURN(5.688e26, 6.0268e7),
    URANUS(8.686e25, 2.5559e7),
    NEPTUNE(1.024e26, 2.4746e7);

    private final double mass;
    private final double radius;
    private static final double G = 6.67300E-11;

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    double surfaceGravity() {
        return G * mass / (radius * radius);
    }

    double weightOnPlanet(double earthWeight) {
        return earthWeight / EARTH.surfaceGravity() * surfaceGravity();
    }
}

enum Level {
    LOW(1, "Low priority"),
    MEDIUM(2, "Medium priority"),
    HIGH(3, "High priority"),
    URGENT(4, "Urgent priority");

    private int levelCode;
    private String description;

    Level(int levelCode, String description) {
        this.levelCode = levelCode;
        this.description = description;
    }

    int getLevelCode() { return levelCode; }
    String getDescription() { return description; }
}
```

## Explanation
1. **Day enum** — each constant passes a `boolean` to the constructor indicating if it's a weekday. `getType()` returns "Weekday" or "Weekend".
2. **Planet enum** — a more advanced example. Each planet stores its mass and radius. The enum calculates surface gravity using Newton's gravitational constant and computes your weight on each planet relative to Earth.
3. **Level enum** — stores a numeric code and a description string, showing that enums can hold rich data.
4. `values()` is used to iterate over all constants in a `for-each` loop.
5. `System.out.printf("%-10s %s%n", ...)` left-aligns the day name in a 10-character field.

## Expected Output

```
=== Enum Demo ===

Today is: WEDNESDAY
Is weekday? true

All days:
MONDAY     Weekday
TUESDAY    Weekday
WEDNESDAY  Weekday
THURSDAY   Weekday
FRIDAY     Weekday
SATURDAY   Weekend
SUNDAY     Weekend

Planet weights:
Weight on MERCURY: 26.43 kg
Weight on VENUS: 63.40 kg
Weight on EARTH: 70.00 kg
Weight on MARS: 26.55 kg
Weight on JUPITER: 176.99 kg
Weight on SATURN: 74.63 kg
Weight on URANUS: 79.44 kg
Weight on NEPTUNE: 88.59 kg

Level: HIGH
Level code: 3
Level description: High priority
```
