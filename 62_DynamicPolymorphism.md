# Lesson 62: Dynamic Polymorphism

## Key Concepts
- Dynamic polymorphism resolves method calls at runtime rather than compile time.
- The actual type of the object determines which method is invoked.
- A superclass reference variable can hold any subclass object.
- The `switch` expression (Java 14+) can assign different subclass instances based on user input.
- This allows the program's behavior to change dynamically based on runtime conditions.

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Choose an animal:");
        System.out.println("1. Dog");
        System.out.println("2. Cat");
        System.out.println("3. Cow");
        System.out.print("Enter choice: ");
        int choice = scanner.nextInt();

        Animal animal;

        animal = switch (choice) {
            case 1 -> new Dog();
            case 2 -> new Cat();
            case 3 -> new Cow();
            default -> null;
        };

        if (animal != null) {
            System.out.print("The animal says: ");
            animal.speak();
        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
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
        System.out.println("Woof! Woof!");
    }
}

class Cat extends Animal {
    @Override
    void speak() {
        System.out.println("Meow!");
    }
}

class Cow extends Animal {
    @Override
    void speak() {
        System.out.println("Moo!");
    }
}
```

## Explanation
1. The program prints a menu and reads the user's choice with `Scanner`.
2. A `switch` expression assigns a different subclass instance to the `Animal` reference variable based on input.
   - Choice 1 → `new Dog()`, choice 2 → `new Cat()`, choice 3 → `new Cow()`.
3. If the choice is valid (`animal != null`), `animal.speak()` is called.
4. At runtime, Java determines which `speak()` override to execute depending on the actual object type.
5. This is dynamic (runtime) polymorphism — the same line of code (`animal.speak()`) produces different behavior.

## Expected Output (example with choice 1)

```
Choose an animal:
1. Dog
2. Cat
3. Cow
Enter choice: 1
The animal says: Woof! Woof!
```
