# Lesson 17: Weight Converter

## Key Concepts
- Building a menu-driven console program
- Using `if-else` for branching logic
- `printf()` for formatted numeric output
- Conversion formula: 1 kg = 2.20462 lbs

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== WEIGHT CONVERTER ===");
        System.out.println("1. Kilograms to Pounds");
        System.out.println("2. Pounds to Kilograms");
        System.out.print("Choose option (1 or 2): ");
        int option = scanner.nextInt();

        System.out.print("Enter weight: ");
        double weight = scanner.nextDouble();

        if (option == 1) {
            double pounds = weight * 2.20462;
            System.out.printf("%.2f kg = %.2f lbs%n", weight, pounds);
        } else if (option == 2) {
            double kg = weight / 2.20462;
            System.out.printf("%.2f lbs = %.2f kg%n", weight, kg);
        } else {
            System.out.println("Invalid option.");
        }

        scanner.close();
    }
}
```

## Explanation
1. The user picks conversion direction (1 or 2).
2. They enter a weight value.
3. If option 1: multiply by 2.20462 to get pounds.
4. If option 2: divide by 2.20462 to get kilograms.
5. `printf("%.2f", value)` formats the number to 2 decimal places.
6. An `else` catches invalid menu choices.

## Expected Output

```
=== WEIGHT CONVERTER ===
1. Kilograms to Pounds
2. Pounds to Kilograms
Choose option (1 or 2): 1
Enter weight: 75
75.00 kg = 165.35 lbs
```
