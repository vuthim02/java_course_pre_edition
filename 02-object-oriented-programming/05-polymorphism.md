# OOP — Lesson 5: Polymorphism

## What is Polymorphism?

**Polymorphism** = "many forms." The ability for the SAME code to work with objects of DIFFERENT types.

```
polymorphism = poly (many) + morph (form)

Same method call → Different behavior depending on the object type.
```

## The Two Types of Polymorphism

### 1. Compile-Time Polymorphism (Method Overloading)

Same method name, different parameters — resolved at compile time:

```java
public class Calculator {
    public int add(int a, int b) { return a + b; }
    public double add(double a, double b) { return a + b; }
    public int add(int a, int b, int c) { return a + b + c; }
}
// Compiler knows which version to call based on arguments
```

### 2. Runtime Polymorphism (Method Overriding)

The JVM decides at RUNTIME which method to call, based on the **actual object type**:

```java
Animal a1 = new Dog();   // Reference type: Animal, Object type: Dog
Animal a2 = new Cat();   // Reference type: Animal, Object type: Cat

a1.makeSound();  // JVM checks: "Is a1 actually a Dog?" → calls Dog.makeSound()
a2.makeSound();  // JVM checks: "Is a2 actually a Cat?" → calls Cat.makeSound()
```

## The Power of Polymorphism

### Example: A Zoo Feeding System

```java
public class Zoo {
    public static void feed(Animal animal) {
        // This ONE method works for ANY Animal subtype!
        System.out.println("Feeding " + animal.getName() + "...");
        animal.eat();  // Calls the CORRECT eat() for this animal type
    }

    public static void main(String[] args) {
        feed(new Dog("Rex"));
        feed(new Cat("Whiskers"));
        feed(new Bird("Tweety"));
        // If you add a Fish class, feed() still works!
    }
}
```

**Without polymorphism:** You'd need separate methods:
```java
// Without polymorphism:
public void feedDog(Dog d) { ... }
public void feedCat(Cat c) { ... }
public void feedBird(Bird b) { ... }
// Every new animal type = new method!
```

### Polymorphic Collections

```java
List<Animal> animals = new ArrayList<>();
animals.add(new Dog("Rex"));
animals.add(new Cat("Whiskers"));
animals.add(new Bird("Tweety"));

// ONE loop handles ALL types!
for (Animal a : animals) {
    a.makeSound();  // Polymorphic call!
}
```

## Upcasting and Downcasting

### Upcasting (Implicit, Always Safe)

```java
Dog dog = new Dog();
Animal animal = dog;  // Upcast: Dog → Animal (ALWAYS safe)
```

```
Dog is-a Animal, so this always works.
The reference is "narrower" (can only access Animal methods),
but the object is still a Dog.
```

### Downcasting (Explicit, Can Fail)

```java
Animal animal = new Dog();
Dog dog = (Dog) animal;      // Downcast: Animal → Dog (works)
Cat cat = (Cat) animal;      // ClassCastException! animal is a Dog, not a Cat!
```

**Always check with instanceof:**

```java
if (animal instanceof Dog) {
    Dog dog = (Dog) animal;
    dog.bark();
} else if (animal instanceof Cat) {
    Cat cat = (Cat) animal;
    cat.meow();
}

// Java 16+ pattern matching:
if (animal instanceof Dog dog) {
    dog.bark();  // Pattern variable — already cast!
}
if (animal instanceof Cat cat) {
    cat.meow();
}
```

---

### Exercises

1. Create a `PaymentMethod` interface with `void pay(double amount)`. Implement `CreditCard`, `PayPal`, `Bitcoin`. Then write a method `processPayment(PaymentMethod pm, double amount)` that works for all implementations.
2. Create an array of `Shape` objects (Circle, Rectangle, Triangle). Loop through and call `area()` on each.
3. Demonstrate upcasting and downcasting with instanceof checks. Show a ClassCastException.
4. **Challenge:** Create an `Employee` base class with `calculateBonus()`. Create a `Manager`, `Developer`, `SalesPerson` subclass that each override `calculateBonus()` differently. Store them in a single list and calculate total bonuses.
