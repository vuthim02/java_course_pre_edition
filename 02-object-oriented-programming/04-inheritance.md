# OOP — Lesson 4: Inheritance

## What is Inheritance?

**Inheritance** = a class can "inherit" fields and methods from a parent class. The child class REUSES and EXTENDS the parent.

```
┌─────────────────────────────────────────────────────┐
│                    THE ANIMAL KINGDOM                  │
│                                                       │
│  ┌─────────────────────────────┐                     │
│  │         Animal              │  ← Parent class     │
│  │  - name: String             │                     │
│  │  + eat()                    │                     │
│  │  + sleep()                  │                     │
│  └──────────┬──────────────────┘                     │
│             │                                         │
│    ┌────────┼────────┐                               │
│    │        │        │                               │
│    ▼        ▼        ▼                               │
│  ┌────┐  ┌────┐  ┌────┐                             │
│  │Dog │  │Cat │  │Bird│   ← Child classes           │
│  │    │  │    │  │    │                              │
│  │+   │  │+   │  │+   │     (INHERIT eat + sleep,   │
│  │bark│  │meow│  │fly │      ADD their own methods) │
│  └────┘  └────┘  └────┘                             │
└─────────────────────────────────────────────────────┘
```

## Basic Inheritance Syntax

```java
// Parent class (superclass, base class)
public class Animal {
    protected String name;

    public Animal(String name) {
        this.name = name;
    }

    public void eat() {
        System.out.println(name + " is eating.");
    }

    public void sleep() {
        System.out.println(name + " is sleeping.");
    }
}

// Child class (subclass, derived class)
public class Dog extends Animal {  // ← 'extends' is the keyword
    public Dog(String name) {
        super(name);  // ← MUST call parent constructor first!
    }

    // NEW method — Dog-specific
    public void bark() {
        System.out.println(name + " says Woof!");
    }
}
```

```java
// Usage
Dog dog = new Dog("Rex");
dog.eat();      // Inherited from Animal
dog.sleep();    // Inherited from Animal
dog.bark();     // Defined in Dog

Animal animal = new Dog("Buddy");  // Polymorphism (next lesson)
```

## The `super` Keyword

`super` refers to the **parent class**:

```java
public class Dog extends Animal {
    public Dog(String name) {
        super(name);  // Call Animal's constructor FIRST
        // If you don't call super(), Java inserts super() (no-arg)
        // But if Animal has no no-arg constructor → COMPILE ERROR!
    }

    @Override
    public void eat() {
        System.out.println(name + " sniffs the food...");
        super.eat();  // Call the parent's eat() method too
        System.out.println(name + " wags tail.");
    }
}
```

**Constructor rule:** The first line of ANY constructor must be `super()` or `this()`. If neither is written, Java inserts `super()` automatically.

## Method Overriding

A child class can **override** (replace) a parent method:

```java
public class Animal {
    public void makeSound() {
        System.out.println("Some generic animal sound");
    }
}

public class Dog extends Animal {
    @Override  // ← Annotation: tells compiler "I intend to override"
    public void makeSound() {
        System.out.println("Woof! Woof!");
    }
}

public class Cat extends Animal {
    @Override
    public void makeSound() {
        System.out.println("Meow!");
    }
}
```

```java
Animal[] animals = {new Dog(), new Cat(), new Animal()};
for (Animal a : animals) {
    a.makeSound();
}
// Output:
// Woof! Woof!
// Meow!
// Some generic animal sound
```

### Override Rules

1. Same method signature (name + parameters)
2. Return type must be the same OR a subtype (covariant return)
3. Can't be more restrictive (e.g., can't make public → private)
4. Can be less restrictive (e.g., protected → public)
5. Can't override `final` methods
6. Can't override `static` methods (that's hiding, not overriding)
7. @Override annotation is optional but STRONGLY recommended

## The `Object` Class — The Root of Everything

EVERY class in Java extends `Object` implicitly:

```java
// These are the SAME:
public class MyClass extends Object { ... }
public class MyClass { ... }  // Implicitly extends Object
```

Useful methods inherited from Object:

```java
public class Person {
    private String name;
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Override toString() — what to print for this object
    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + "}";
    }

    // Override equals() — when are two objects "equal"?
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return age == person.age && Objects.equals(name, person.name);
    }

    // Override hashCode() — if you override equals(), override hashCode()!
    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

## Inheritance Hierarchies

Java supports single inheritance (one parent), but chains can be deep:

```java
public class LivingThing { ... }
public class Animal extends LivingThing { ... }
public class Mammal extends Animal { ... }
public class Dog extends Mammal { ... }
public class Poodle extends Dog { ... }
```

```
LivingThing → Animal → Mammal → Dog → Poodle
     ↑                                        ↑
  Most general                           Most specific
```

## What is NOT Inherited

- **Private fields/methods** — not visible to child classes
- **Constructors** — not inherited (must use `super()` to call)
- **Static methods** — technically inherited, but it's "hiding" not overriding

## Composition Over Inheritance

**Favor composition (has-a) over inheritance (is-a):**

```java
// INHERITANCE (is-a): Dog IS-AN Animal
// Good for: "X is a type of Y"

// COMPOSITION (has-a): Car HAS-AN Engine
// Good for: "X has a Y"
public class Car {
    private Engine engine;  // Car HAS-AN Engine
    private List<Wheel> wheels;

    public void start() {
        engine.ignite();  // Delegate to Engine
    }
}
```

**Rule of thumb:** "Is-a" → inheritance. "Has-a" → composition.

---

### Exercises

1. Create a class hierarchy: `Vehicle` → `Car` → `ElectricCar`. Add appropriate fields and methods.
2. Override `toString()`, `equals()`, and `hashCode()` for a `Book` class.
3. Create an `Employee` parent class and `Manager`, `Developer`, `Intern` subclasses with different `calculateBonus()` implementations.
4. Demonstrate the difference between method overriding and method overloading with examples.
5. **Challenge:** Create a shape hierarchy: `Shape` → `Circle`, `Rectangle`, `Triangle` with an overridden `area()` method.
