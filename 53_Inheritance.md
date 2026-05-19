# Lesson 53: Inheritance

## Key Concepts
- **Inheritance** allows a class to acquire the fields and methods of another class
- Use the `extends` keyword to inherit from a parent (super) class
- The subclass inherits all non-private members from the parent
- The subclass can **override** inherited methods or add new ones
- `super()` calls the parent class constructor
- Java supports **single inheritance** — a class can only extend one parent
- Inheritance models an "is-a" relationship (e.g., a Dog is an Animal)

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Dog dog = new Dog("Buddy", "Golden Retriever");
        Cat cat = new Cat("Whiskers", "Black");

        System.out.println("=== DOG ===");
        dog.speak();
        dog.eat();
        dog.fetch();

        System.out.println("\n=== CAT ===");
        cat.speak();
        cat.eat();
        cat.scratch();

        System.out.println("\nDog name: " + dog.name);
        System.out.println("Cat name: " + cat.name);
    }
}

class Animal {
    String name;

    Animal(String name) {
        this.name = name;
    }

    void eat() {
        System.out.println(name + " is eating.");
    }

    void speak() {
        System.out.println(name + " makes a sound.");
    }
}

class Dog extends Animal {
    String breed;

    Dog(String name, String breed) {
        super(name);
        this.breed = breed;
    }

    @Override
    void speak() {
        System.out.println(name + " barks: Woof! Woof!");
    }

    void fetch() {
        System.out.println(name + " fetches the ball.");
    }
}

class Cat extends Animal {
    String color;

    Cat(String name, String color) {
        super(name);
        this.color = color;
    }

    @Override
    void speak() {
        System.out.println(name + " meows: Meow!");
    }

    void scratch() {
        System.out.println(name + " scratches the furniture.");
    }
}
```

### Explanation
`Dog` and `Cat` extend `Animal`, inheriting the `name` field and the `eat()` method. Both override `speak()` to provide their own behavior. Each subclass adds a unique method (`fetch()` for Dog, `scratch()` for Cat) and its own field (`breed`, `color`). The `super(name)` call in each constructor initializes the inherited `name` field.

### Expected Output
```
=== DOG ===
Buddy barks: Woof! Woof!
Buddy is eating.
Buddy fetches the ball.

=== CAT ===
Whiskers meows: Meow!
Whiskers is eating.
Whiskers scratches the furniture.

Dog name: Buddy
Cat name: Whiskers
```
