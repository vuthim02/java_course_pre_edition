# Lesson 51: The `static` Keyword

## Key Concepts
- `static` members belong to the **class itself**, not to individual instances
- All instances share the same static variable
- Static methods can be called without creating an object (`ClassName.methodName()`)
- Static methods **cannot** access non-static (instance) fields directly
- Common uses: counters, utility methods, constants
- `static final` together creates a constant (unchangeable class-level value)

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Friend friend1 = new Friend("Alice");
        Friend friend2 = new Friend("Bob");
        Friend friend3 = new Friend("Charlie");

        System.out.println("Total friends: " + Friend.numberOfFriends);

        Friend.displayFriends();

        System.out.println("\nMath utility:");
        System.out.println("PI: " + MathUtility.PI);
        System.out.println("Add: " + MathUtility.add(5, 3));
        System.out.println("Multiply: " + MathUtility.multiply(4, 7));
    }
}

class Friend {
    String name;
    static int numberOfFriends = 0;

    Friend(String name) {
        this.name = name;
        numberOfFriends++;
    }

    static void displayFriends() {
        System.out.println("You have " + numberOfFriends + " friends total.");
    }
}

class MathUtility {
    static final double PI = 3.14159;

    static int add(int a, int b) {
        return a + b;
    }

    static int multiply(int a, int b) {
        return a * b;
    }
}
```

### Explanation
`numberOfFriends` is `static` — every `Friend` constructor increments it, and it is accessed via `Friend.numberOfFriends`. `MathUtility` groups related constants and utility methods that can be used without instantiation.

### Expected Output
```
Total friends: 3
You have 3 friends total.

Math utility:
PI: 3.14159
Add: 8
Multiply: 28
```
