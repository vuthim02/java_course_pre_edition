# Lesson 55: Method Overriding

## Key Concepts
- **Method overriding** occurs when a subclass provides its own implementation of a method already defined in the parent class
- The method signature (name + parameters) must be identical
- Use the `@Override` annotation to mark overridden methods (optional but recommended)
- At runtime, Java calls the method based on the **actual object type**, not the reference type (polymorphism)
- Overriding enables polymorphic behavior — treating different subclasses uniformly

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Animal animal = new Animal();
        Dog dog = new Dog();
        Cat cat = new Cat();

        System.out.println("Animal speaks:");
        animal.speak();

        System.out.println("\nDog speaks:");
        dog.speak();

        System.out.println("\nCat speaks:");
        cat.speak();

        System.out.println("\n--- Method overriding with polymorphism ---");
        Animal[] animals = {new Animal(), new Dog(), new Cat()};
        for (Animal a : animals) {
            a.speak();
        }
    }
}

class Animal {
    void speak() {
        System.out.println("Animal makes a sound");
    }
}

class Dog extends Animal {
    @Override
    void speak() {
        System.out.println("Dog barks: Woof! Woof!");
    }
}

class Cat extends Animal {
    @Override
    void speak() {
        System.out.println("Cat meows: Meow!");
    }
}
```

### Explanation
`Dog` and `Cat` each override `speak()` from `Animal`. The polymorphic loop at the end demonstrates that even though all objects are stored in an `Animal[]` array, the correct overridden method runs for each one based on its actual runtime type.

### Expected Output
```
Animal speaks:
Animal makes a sound

Dog speaks:
Dog barks: Woof! Woof!

Cat speaks:
Cat meows: Meow!

--- Method overriding with polymorphism ---
Animal makes a sound
Dog barks: Woof! Woof!
Cat meows: Meow!
```
