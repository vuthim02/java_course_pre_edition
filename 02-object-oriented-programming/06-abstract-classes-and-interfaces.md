# OOP — Lesson 6: Abstract Classes & Interfaces

## Abstract Classes

An **abstract class** is a class that CANNOT be instantiated. It serves as a base for subclasses.

```
┌────────────────────────────────┐
│        Abstract Shape          │  ← CANNOT do: new Shape()
│  abstract double area()        │
│  abstract double perimeter()   │  ← Must be overridden
│  void display() { ... }        │  ← Fully implemented (inherited)
└────────────┬───────────────────┘
             │
     ┌───────┼───────┐
     │       │       │
     ▼       ▼       ▼
  Circle  Rect    Triangle       ← CAN do: new Circle(), new Rect()
```

```java
public abstract class Shape {
    protected String color;

    public Shape(String color) {
        this.color = color;
    }

    // Abstract method — NO body, must be overridden
    public abstract double getArea();

    // Abstract method — NO body, must be overridden
    public abstract double getPerimeter();

    // Concrete method — fully implemented, inherited as-is
    public void display() {
        System.out.println("Shape area: " + getArea());
        System.out.println("Shape perimeter: " + getPerimeter());
    }
}

public class Circle extends Shape {
    private double radius;

    public Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }

    @Override
    public double getPerimeter() {
        return 2 * Math.PI * radius;
    }
}
```

### Rules for Abstract Classes

1. Cannot instantiate: `new Shape()` ❌
2. Can have constructors (called via `super()` from subclass)
3. Can have fields, concrete methods, static methods
4. If a class has ANY abstract method, the class MUST be abstract
5. A subclass must implement ALL abstract methods OR be abstract itself

## Interfaces

An **interface** is a **contract** — it specifies WHAT a class must do, but not HOW.

```java
// Interface = 100% abstract contract
public interface Drawable {
    void draw();  // implicitly public abstract
}

public interface Resizable {
    void resize(double factor);  // implicitly public abstract
}
```

### Implementing Interfaces

```java
// A class can implement MULTIPLE interfaces
public class Circle implements Drawable, Resizable {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.println("Drawing a circle with radius " + radius);
    }

    @Override
    public void resize(double factor) {
        radius *= factor;
    }
}
```

### Interface Features (Java 8+)

```java
public interface Vehicle {
    // Abstract method (must be implemented)
    void start();

    // Default method (Java 8+) — has a body
    default void honk() {
        System.out.println("Beep beep!");
    }

    // Static method (Java 8+) — called via interface name
    static boolean isValidSpeed(int speed) {
        return speed >= 0 && speed <= 300;
    }

    // Private method (Java 9+) — helper for default methods
    private void log(String message) {
        System.out.println("[Vehicle] " + message);
    }
}
```

### Multiple Inheritance of Type

Java doesn't allow multiple CLASS inheritance, but DOES allow multiple INTERFACE implementation:

```java
public interface Swimmer {
    void swim();
}

public interface Flyer {
    void fly();
}

public class Duck implements Swimmer, Flyer {
    @Override
    public void swim() { System.out.println("Duck swimming"); }

    @Override
    public void fly() { System.out.println("Duck flying"); }
}
```

### The Diamond Problem (Why No Multiple Class Inheritance?)

```
        Animal
       /      \
    Mammal    Bird     ← If both override makeSound()
       \      /
        Bat              ← Which makeSound() to use?
        [DIAMOND PROBLEM]
```

Interfaces solve this: if two interfaces have the same default method, the implementing class must override it:

```java
public interface A {
    default void doSomething() { System.out.println("A"); }
}

public interface B {
    default void doSomething() { System.out.println("B"); }
}

public class MyClass implements A, B {
    // MUST override to resolve conflict!
    @Override
    public void doSomething() {
        A.super.doSomething();  // Choose one
        B.super.doSomething();  // Or both
        System.out.println("MyClass");  // Or custom
    }
}
```

## Abstract Class vs Interface — When to Use What

| Feature | Abstract Class | Interface |
|---------|---------------|-----------|
| **Purpose** | Base class with shared state/behavior | Contract/capability |
| **Fields** | Any fields (instance, static) | Only `public static final` |
| **Constructors** | Yes | No |
| **Methods** | Abstract + concrete | Abstract + default + static + private |
| **Multiple inheritance** | Single class extends | Multiple interfaces can implement |
| **When to use** | "Is-a" relationship with shared code | "Can-do" capability |

```java
// USE ABSTRACT CLASS WHEN:
// - Classes share state (fields)
// - Classes share implementation (concrete methods)
// - "Is-a" relationship with common base

public abstract class Employee {
    protected String name;
    protected double baseSalary;

    public abstract double calculatePay();  // Varies by type

    public void displayInfo() {  // Shared implementation
        System.out.println(name + ": $" + calculatePay());
    }
}

// USE INTERFACE WHEN:
// - Unrelated classes need same capability
// - You need multiple inheritance of type
// - You're defining a capability/behavior

public interface Payable {
    double calculatePay();
}

public interface Taxable {
    double calculateTax();
}
```

---

### Exercises

1. Create an abstract `BankAccount` class with abstract `withdraw()` and concrete `deposit()`. Create `SavingsAccount` and `CheckingAccount` subclasses.
2. Create `Playable` interface with `void play()`, `void pause()`, `void stop()`. Implement in `MusicPlayer` and `VideoPlayer`.
3. Create a `Sortable` interface with `int compare(Sortable other)`. Implement in `Book` and `Student` classes.
4. Demonstrate default method resolution when a class implements two interfaces with the same default method.
5. Create a class that extends an abstract class AND implements an interface simultaneously.
