# Lesson 61: Polymorphism

## Key Concepts
- Polymorphism means "many forms" — it allows objects of different classes to be treated as objects of a common superclass.
- A superclass reference can point to a subclass object.
- When a method is called on a superclass reference, Java determines at runtime which subclass method to execute (dynamic method dispatch).
- The `@Override` annotation indicates a subclass method overrides a superclass method.
- Polymorphism enables writing flexible and reusable code (e.g., looping through an array of superclass references).

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Vehicle[] vehicles = new Vehicle[3];
        vehicles[0] = new Car();
        vehicles[1] = new Boat();
        vehicles[2] = new Bicycle();

        System.out.println("=== Polymorphism in action ===");
        for (Vehicle v : vehicles) {
            v.go();
        }
    }
}

class Vehicle {
    void go() {
        System.out.println("The vehicle is moving.");
    }
}

class Car extends Vehicle {
    @Override
    void go() {
        System.out.println("The car is driving on the road.");
    }
}

class Boat extends Vehicle {
    @Override
    void go() {
        System.out.println("The boat is sailing on the water.");
    }
}

class Bicycle extends Vehicle {
    @Override
    void go() {
        System.out.println("The bicycle is pedaling on the path.");
    }
}
```

## Explanation
1. A `Vehicle` array is declared: `Vehicle[] vehicles = new Vehicle[3]`.
2. Each element is assigned a different subclass: `Car`, `Boat`, or `Bicycle`.
3. The enhanced for loop iterates over the array. On each iteration, `v.go()` is called.
4. At runtime, Java calls the correct overridden `go()` method based on the actual object type — this is polymorphism.
5. `Car`, `Boat`, and `Bicycle` each override `go()` from `Vehicle` with their own behavior.

## Expected Output

```
=== Polymorphism in action ===
The car is driving on the road.
The boat is sailing on the water.
The bicycle is pedaling on the path.
```
