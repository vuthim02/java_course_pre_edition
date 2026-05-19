# Lesson 28: Methods

## Key Concepts
- Methods break code into reusable, named blocks
- `static` methods belong to the class (no object needed)
- Methods can accept parameters and return values
- `void` methods perform an action without returning a value
- Recursion: a method calling itself (factorial example)

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your name: ");
        String name = scanner.nextLine();
        greet(name);

        System.out.print("\nEnter first number: ");
        int a = scanner.nextInt();
        System.out.print("Enter second number: ");
        int b = scanner.nextInt();

        int sum = add(a, b);
        System.out.println("Sum: " + sum);

        int difference = subtract(a, b);
        System.out.println("Difference: " + difference);

        System.out.println("\nFactorial of " + a + ": " + factorial(a));

        scanner.close();
    }

    static void greet(String name) {
        System.out.println("Hello " + name + "! Welcome to Java methods.");
    }

    static int add(int x, int y) {
        return x + y;
    }

    static int subtract(int x, int y) {
        return x - y;
    }

    static int factorial(int n) {
        if (n <= 1) return 1;
        return n * factorial(n - 1);
    }
}
```

## Explanation
1. **`greet(String name)`** — a `void` method that prints a message. Takes a parameter, returns nothing.
2. **`add(int x, int y)`** — returns the sum of two integers.
3. **`subtract(int x, int y)`** — returns the difference of two integers.
4. **`factorial(int n)`** — a recursive method. `factorial(5) = 5 * 4 * 3 * 2 * 1`. The base case `n <= 1` stops the recursion.
5. Methods are called from `main` using their name and passing arguments.

## Expected Output

```
Enter your name: Alice
Hello Alice! Welcome to Java methods.

Enter first number: 7
Enter second number: 4
Sum: 11
Difference: 3

Factorial of 7: 5040
```
