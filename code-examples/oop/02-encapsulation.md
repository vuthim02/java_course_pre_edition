# Encapsulation

Encapsulation hides internal state and requires all interaction to happen through public methods. This protects invariants, enables validation, and lets you change internal implementation without breaking callers. The JavaBeans convention uses `getX()` / `setX()` naming for readable/writable properties.

```java
// ============================================================
// 1. Private fields with public getters/setters + validation
// ============================================================

class Person {
    private String name;
    private int age;

    public Person() {}

    public Person(String name, int age) {
        setName(name);
        setAge(age);
    }

    // --- JavaBeans getter/setter ---

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age <= 0) {
            throw new IllegalArgumentException("Age must be positive, got: " + age);
        }
        this.age = age;
    }
}

// ============================================================
// 2. Immutable class pattern
// ============================================================

import java.util.Arrays;

final class ImmutablePoint {
    private final int x;
    private final int y;
    private final int[] coordinates;  // mutable field — must defend

    public ImmutablePoint(int x, int y, int[] coordinates) {
        this.x = x;
        this.y = y;
        // Defensive copy of incoming array (prevents caller from mutating our state)
        this.coordinates = coordinates == null ? new int[0] : coordinates.clone();
    }

    public int getX() { return x; }
    public int getY() { return y; }

    // Defensive copy on the way out too
    public int[] getCoordinates() {
        return coordinates.clone();
    }

    // No setters — class is immutable
}

// ============================================================
// 3. Package-private and protected access
// ============================================================

class AccessDemo {
    public int    pub;    // accessible everywhere
    protected int prot;   // accessible in same package + subclasses
    int            pkg;   // package-private (default) — accessible in same package only
    private int   priv;   // accessible only within this class

    public AccessDemo(int pub, int prot, int pkg, int priv) {
        this.pub  = pub;
        this.prot = prot;
        this.pkg  = pkg;
        this.priv = priv;
    }
}

// In the same package, this works:
class AccessTest {
    void demo() {
        AccessDemo a = new AccessDemo(1, 2, 3, 4);
        System.out.println(a.pub);   // ok
        System.out.println(a.prot);  // ok  (same package)
        System.out.println(a.pkg);   // ok  (same package)
        // System.out.println(a.priv); // COMPILE ERROR
    }
}

// ============================================================
// 4. Main
// ============================================================

public class EncapsulationDemo {
    public static void main(String[] args) {
        // --- Validated setters ---
        Person p = new Person("Alice", 30);
        System.out.println(p.getName() + " is " + p.getAge());

        try {
            p.setAge(-5);           // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        try {
            p.setName("");          // throws
        } catch (IllegalArgumentException e) {
            System.out.println("Caught: " + e.getMessage());
        }

        // --- Immutable class ---
        int[] coords = {10, 20};
        ImmutablePoint pt = new ImmutablePoint(10, 20, coords);
        coords[0] = 999;            // mutates our local array, not the object's
        int[] got = pt.getCoordinates();
        got[0] = 0;                 // mutates the copy, not the internal array
        System.out.println(Arrays.toString(pt.getCoordinates()));  // still [10, 20]
    }
}
```
