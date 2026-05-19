# Lesson 21: Calculator Program

## Key Concepts
- Building a simple calculator with `switch`
- Using `char` input from the user
- `scanner.next().charAt(0)` to read a single character
- Handling division by zero with `yield` and `Double.NaN`
- `Double.isNaN()` to check for invalid results

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== CALCULATOR ===");
        System.out.print("Enter first number: ");
        double num1 = scanner.nextDouble();

        System.out.print("Enter operator (+, -, *, /): ");
        char op = scanner.next().charAt(0);

        System.out.print("Enter second number: ");
        double num2 = scanner.nextDouble();

        double result = switch (op) {
            case '+' -> num1 + num2;
            case '-' -> num1 - num2;
            case '*' -> num1 * num2;
            case '/' -> {
                if (num2 == 0) yield Double.NaN;
                yield num1 / num2;
            }
            default -> Double.NaN;
        };

        if (Double.isNaN(result)) {
            System.out.println("Invalid operation.");
        } else {
            System.out.printf("%.2f %c %.2f = %.2f%n", num1, op, num2, result);
        }

        scanner.close();
    }
}
```

## Explanation
1. The user enters two numbers and an operator.
2. An enhanced `switch` expression chooses the operation.
3. Division by zero is guarded: if `num2 == 0`, `Double.NaN` (Not-a-Number) is yielded.
4. An invalid operator also yields `Double.NaN`.
5. `Double.isNaN(result)` checks whether the result is valid before printing.

## Expected Output

```
=== CALCULATOR ===
Enter first number: 10
Enter operator (+, -, *, /): /
Enter second number: 3
10.00 / 3.00 = 3.33
```
