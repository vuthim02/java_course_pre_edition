# OOP — Lesson 1: Classes & Objects

## The Big Idea

**Object-Oriented Programming** models real-world things as objects with:
- **State** (data/fields) — "what it HAS"
- **Behavior** (methods) — "what it DOES"

```
Real World:              Java Code:
┌─────────────────┐      ┌──────────────────────────────┐
│     DOG         │      │ public class Dog {            │
│                 │      │     String name;  // state     │
│  Name: "Rex"    │      │     int age;                  │
│  Age: 3         │      │                              │
│  Breed: "Husky" │      │     void bark() { ... } // behavior│
│                 │      │     void eat() { ... }       │
│  Can bark       │      │ }                            │
│  Can eat        │      └──────────────────────────────┘
└─────────────────┘
```

## Classes vs Objects

A **class** is a **blueprint**. An **object** is the actual thing built from the blueprint.

```
CLASS: "House Blueprint"    OBJECT: "My actual house at 123 Main St"
┌────────────────────┐      ┌────────────────────┐
│ House              │      │ myHouse            │
│ - address: String  │      │ address: "123 Main"│
│ - rooms: int       │ ──▶  │ rooms: 3           │
│ + paint()          │      │ (can be painted)   │
│ + clean()          │      │ (can be cleaned)   │
└────────────────────┘      └────────────────────┘
```

**Analogy:** A class is the recipe for a cake. An object is the actual cake you bake and eat.

## Defining a Class

```java
public class Dog {
    // Fields (state) — what a Dog HAS
    String name;
    String breed;
    int age;

    // Methods (behavior) — what a Dog DOES
    void bark() {
        System.out.println("Woof! Woof!");
    }

    void eat() {
        System.out.println(name + " is eating.");
    }
}
```

## Creating Objects (Instantiation)

Use the `new` keyword to create an object from a class:

```java
public class Main {
    public static void main(String[] args) {
        // Create Dog objects
        Dog dog1 = new Dog();  // Instantiation
        Dog dog2 = new Dog();

        // Set state
        dog1.name = "Rex";
        dog1.age = 3;
        dog1.breed = "Husky";

        dog2.name = "Bella";
        dog2.age = 5;
        dog2.breed = "Labrador";

        // Call behavior
        dog1.bark();  // Woof! Woof!
        dog2.eat();   // Bella is eating.
    }
}
```

## Memory View: Objects on the Heap

```
Stack:                     Heap:
┌─────────────┐           ┌───────────────────────┐
│ dog1        │──────▶    │ Dog object #1         │
│ (reference) │           │ name: "Rex"           │
├─────────────┤           │ breed: "Husky"        │
│ dog2        │──────▶    │ age: 3                │
│ (reference) │           └───────────────────────┘
└─────────────┘           ┌───────────────────────┐
                          │ Dog object #2         │
                          │ name: "Bella"         │
                          │ breed: "Labrador"     │
                          │ age: 5                │
                          └───────────────────────┘
```

The **reference** (`dog1`, `dog2`) lives on the stack. The **actual object** lives on the heap.

## Constructors

A **constructor** is a special method that runs when an object is created. It initializes the object.

```java
public class Dog {
    String name;
    String breed;
    int age;

    // Constructor — same name as class, no return type
    public Dog(String name, String breed, int age) {
        this.name = name;   // this.name = THE FIELD
        this.breed = breed; // name = THE PARAMETER
        this.age = age;
    }

    void bark() {
        System.out.println(name + " says Woof!");
    }
}
```

```java
// Usage — now we MUST pass the constructor arguments
Dog dog1 = new Dog("Rex", "Husky", 3);
Dog dog2 = new Dog("Bella", "Labrador", 5);
```

### Constructor Overloading

```java
public class Dog {
    String name;
    String breed;
    int age;

    // Full constructor
    public Dog(String name, String breed, int age) {
        this.name = name;
        this.breed = breed;
        this.age = age;
    }

    // Constructor with defaults
    public Dog(String name) {
        this(name, "Unknown", 0);  // Calls the other constructor!
    }

    // No-arg constructor
    public Dog() {
        this("Unknown", "Unknown", 0);
    }
}
```

### Default Constructor

If you don't define ANY constructor, Java provides a **default no-arg constructor**:

```java
public class SimpleDog {
    String name;
    int age;
    // Java auto-creates: public SimpleDog() { }
}

SimpleDog d = new SimpleDog();  // Works — uses default constructor
```

But if you define ANY constructor, the default disappears:

```java
public class Dog {
    public Dog(String name) { ... }
    // No default constructor anymore!
}

Dog d = new Dog();  // COMPILE ERROR! No no-arg constructor
```

## The `this` Keyword

`this` refers to the **current object** — the one whose method is running:

```java
public class Person {
    String name;

    public Person(String name) {
        this.name = name;  // this.name = FIELD, name = PARAMETER
    }

    public void introduce() {
        System.out.println("Hi, I'm " + this.name);
        // 'this' is optional here — no ambiguity
    }

    public Person withName(String name) {
        this.name = name;
        return this;  // Return the current object for chaining
    }
}
```

## The `new` Keyword — What Actually Happens

```
Dog dog = new Dog("Rex", "Husky", 3);

Step 1: new         → Allocate memory on heap for a Dog object
Step 2: Dog(...)    → Call the constructor
Step 3: this.name="Rex" → Initialize fields
Step 4: Return reference → Assign reference to 'dog'
```

---

### Exercises

1. Create a `Car` class with fields: make, model, year. Add methods: `start()`, `stop()`, `honk()`.
2. Create a `Student` class with: name, id, grades (array). Add a method to calculate average grade.
3. Create a `Rectangle` class with width, height. Add methods: `getArea()`, `getPerimeter()`, `isSquare()`.
4. Create a `Book` class. Add a method `isLongBook()` that returns true if pages > 300.
5. **Challenge:** Create a `TicTacToe` class that manages the board state, has a method to make a move, and checks for a winner.
