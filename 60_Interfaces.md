# Lesson 60: Interfaces

## Key Concepts
- An **interface** is a completely abstract type that defines **method signatures** (no body)
- A class `implements` an interface and must provide implementations for **all** of its methods
- Java supports **multiple inheritance of type** — a class can implement multiple interfaces
- Interfaces define a **contract**: any class that implements the interface guarantees those methods exist
- Interface methods are implicitly `public` and `abstract`
- Interfaces cannot be instantiated directly

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Rabbit rabbit = new Rabbit();
        Hawk hawk = new Hawk();
        Fish fish = new Fish();

        System.out.println("=== RABBIT (Prey) ===");
        rabbit.flee();

        System.out.println("\n=== HAWK (Predator) ===");
        hawk.hunt();

        System.out.println("\n=== FISH (Both) ===");
        fish.flee();
        fish.hunt();
    }
}

interface Prey {
    void flee();
}

interface Predator {
    void hunt();
}

class Rabbit implements Prey {
    @Override
    public void flee() {
        System.out.println("The rabbit is fleeing.");
    }
}

class Hawk implements Predator {
    @Override
    public void hunt() {
        System.out.println("The hawk is hunting.");
    }
}

class Fish implements Prey, Predator {
    @Override
    public void flee() {
        System.out.println("The fish swims away.");
    }

    @Override
    public void hunt() {
        System.out.println("The fish hunts smaller fish.");
    }
}
```

### Explanation
`Prey` and `Predator` are interfaces defining `flee()` and `hunt()` respectively. `Rabbit` implements only `Prey`, `Hawk` implements only `Predator`, and `Fish` implements **both** interfaces. This demonstrates multiple inheritance of behavior — a `Fish` can both flee and hunt.

### Expected Output
```
=== RABBIT (Prey) ===
The rabbit is fleeing.

=== HAWK (Predator) ===
The hawk is hunting.

=== FISH (Both) ===
The fish swims away.
The fish hunts smaller fish.
```
