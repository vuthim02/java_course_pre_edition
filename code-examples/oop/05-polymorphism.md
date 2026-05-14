# Polymorphism

Polymorphism means "many forms." **Runtime polymorphism** (dynamic method dispatch) occurs when the JVM calls the actual object's method at runtime through a superclass reference. **Compile-time polymorphism** is method overloading — same name, different parameter lists. Polymorphism lets you write generic code that works with any subclass.

```java
import java.util.List;
import java.util.ArrayList;

// ============================================================
// Interface for polymorphism demonstration
// ============================================================

interface SoundMaker {
    void makeSound();
}

// ============================================================
// Base class
// ============================================================

class Animal implements SoundMaker {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    @Override
    public void makeSound() {
        System.out.println(name + " makes a generic sound");
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + name;
    }
}

// ============================================================
// Subclasses with overriding
// ============================================================

class Dog extends Animal {
    public Dog(String name) { super(name); }

    @Override
    public void makeSound() {
        System.out.println(name + " barks");
    }
}

class Cat extends Animal {
    public Cat(String name) { super(name); }

    @Override
    public void makeSound() {
        System.out.println(name + " meows");
    }
}

class Duck extends Animal {
    public Duck(String name) { super(name); }

    @Override
    public void makeSound() {
        System.out.println(name + " quacks");
    }
}

// ============================================================
// Compile-time polymorphism (method overloading)
// ============================================================

class Calculator {
    public int add(int a, int b) {
        return a + b;
    }

    public double add(double a, double b) {
        return a + b;
    }

    public int add(int a, int b, int c) {
        return a + b + c;
    }
}

// ============================================================
// Main
// ============================================================

public class PolymorphismDemo {
    public static void main(String[] args) {
        // --- Runtime polymorphism (dynamic method dispatch) ---
        System.out.println("=== Runtime Polymorphism ===");

        Animal myPet = new Dog("Rex");
        myPet.makeSound();   // Rex barks  (Dog's override runs)

        myPet = new Cat("Luna");
        myPet.makeSound();   // Luna meows (Cat's override runs)

        // --- Polymorphic collections ---
        System.out.println("\n=== Polymorphic Collections ===");

        List<Animal> animals = new ArrayList<>();
        animals.add(new Dog("Rex"));
        animals.add(new Cat("Luna"));
        animals.add(new Duck("Daffy"));

        for (Animal a : animals) {
            a.makeSound();
        }

        // --- Compile-time polymorphism ---
        System.out.println("\n=== Compile-time Polymorphism (Overloading) ===");

        Calculator calc = new Calculator();
        System.out.println(calc.add(2, 3));          // 5
        System.out.println(calc.add(2.5, 3.7));      // 6.2
        System.out.println(calc.add(1, 2, 3));       // 6

        // --- Polymorphism with interfaces ---
        System.out.println("\n=== Polymorphism with Interfaces ===");

        List<SoundMaker> soundMakers = new ArrayList<>();
        soundMakers.add(new Dog("Rex"));
        soundMakers.add(new Cat("Luna"));
        // Anonymous implementation
        soundMakers.add(new SoundMaker() {
            @Override
            public void makeSound() {
                System.out.println("Anonymous cow says moo");
            }
        });

        for (SoundMaker sm : soundMakers) {
            sm.makeSound();
        }
    }
}
```
