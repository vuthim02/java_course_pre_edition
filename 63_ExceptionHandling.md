# Lesson 63: Exception Handling

## Key Concepts
- **Exception handling** allows a program to gracefully handle runtime errors instead of crashing.
- `try` block: contains code that might throw an exception.
- `catch` block: handles a specific exception type. Multiple catch blocks can be chained.
- `finally` block: always executes, regardless of whether an exception occurred (used for cleanup).
- `throw`: manually throws an exception.
- `throws`: declares that a method might throw an exception.
- Common exceptions: `ArithmeticException`, `ArrayIndexOutOfBoundsException`, `IllegalArgumentException`.

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter numerator: ");
            int numerator = scanner.nextInt();

            System.out.print("Enter denominator: ");
            int denominator = scanner.nextInt();

            int result = numerator / denominator;
            System.out.println("Result: " + result);
        }
        catch (ArithmeticException e) {
            System.out.println("Cannot divide by zero!");
        }
        catch (Exception e) {
            System.out.println("Something went wrong: " + e.getMessage());
        }
        finally {
            System.out.println("This always executes.");
        }

        scanner.nextLine();

        try {
            System.out.print("\nEnter an index: ");
            int index = scanner.nextInt();
            int[] numbers = {10, 20, 30, 40, 50};
            System.out.println("Value at index " + index + ": " + numbers[index]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid index!");
        }

        try {
            validateAge(15);
        } catch (Exception e) {
            System.out.println("Validation error: " + e.getMessage());
        }

        scanner.close();
    }

    static void validateAge(int age) throws IllegalArgumentException {
        if (age < 18) {
            throw new IllegalArgumentException("Must be 18 or older.");
        }
        System.out.println("Age validated.");
    }
}
```

## Explanation
1. **First try-catch-finally**: Attempts division. If denominator is 0, `ArithmeticException` is caught. The `finally` block runs unconditionally.
2. **Second try-catch**: Reads an array index. If out of bounds, `ArrayIndexOutOfBoundsException` is caught.
3. **Third try-catch**: Calls `validateAge(15)`, which throws `IllegalArgumentException` because 15 < 18.
4. `validateAge` uses `throws` to declare the exception and `throw` to raise it manually.
5. Multiple catch blocks let you handle different exceptions with different responses.

## Expected Output

```
Enter numerator: 10
Enter denominator: 0
Cannot divide by zero!
This always executes.

Enter an index: 10
Invalid index!
Validation error: Must be 18 or older.
```
