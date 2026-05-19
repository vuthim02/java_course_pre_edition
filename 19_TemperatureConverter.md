# Lesson 19: Temperature Converter

## Key Concepts
- Celsius to Fahrenheit: `(C × 9/5) + 32`
- Fahrenheit to Celsius: `(F − 32) × 5/9`
- Menu-driven program with `if-else`
- `printf()` with `%d` and `%f` format specifiers

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== TEMPERATURE CONVERTER ===");
        System.out.println("1. Celsius to Fahrenheit");
        System.out.println("2. Fahrenheit to Celsius");
        System.out.print("Choose option: ");
        int choice = scanner.nextInt();

        System.out.print("Enter temperature: ");
        double temp = scanner.nextDouble();

        if (choice == 1) {
            double f = (temp * 9 / 5) + 32;
            System.out.printf("%.1f°C = %.1f°F%n", temp, f);
        } else if (choice == 2) {
            double c = (temp - 32) * 5 / 9;
            System.out.printf("%.1f°F = %.1f°C%n", temp, c);
        } else {
            System.out.println("Invalid choice.");
        }

        scanner.close();
    }
}
```

## Explanation
1. User selects conversion direction (1 or 2).
2. They enter the temperature value.
3. The appropriate formula is applied.
4. `printf` displays the result with one decimal place and degree symbols.
5. Invalid menu choices are caught by the `else` branch.

## Expected Output

```
=== TEMPERATURE CONVERTER ===
1. Celsius to Fahrenheit
2. Fahrenheit to Celsius
Choose option: 1
Enter temperature: 25
25.0°C = 77.0°F
```
