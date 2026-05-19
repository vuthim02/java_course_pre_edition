# Lesson 32: Dice Roller Program

## Key Concepts
- Using `Random` class to generate random numbers
- `random.nextInt(n)` generates a number from 0 to n-1; add 1 to get 1 to n
- Reading multiple user inputs with `Scanner`
- Using a `for` loop to roll multiple dice
- Accumulating a total with `+=`
- Computing an average with `(double)` cast

## Code Example

```java
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        System.out.print("Enter number of dice: ");
        int numDice = scanner.nextInt();

        System.out.print("Enter number of sides per die: ");
        int numSides = scanner.nextInt();

        System.out.println("\n=== DICE RESULTS ===");
        int total = 0;
        for (int i = 1; i <= numDice; i++) {
            int roll = random.nextInt(numSides) + 1;
            System.out.println("Die " + i + ": " + roll);
            total += roll;
        }

        System.out.println("Total: " + total);
        System.out.println("Average: " + (double) total / numDice);

        scanner.close();
    }
}
```

## Explanation
1. `Random` generates pseudo-random numbers. `nextInt(numSides)` returns 0 to `numSides - 1`. Adding 1 shifts the range to 1 to `numSides`.
2. The user specifies how many dice to roll and how many sides each die has.
3. The `for` loop runs once per die, generating a random roll and accumulating the total.
4. The average is computed by casting `total` to `double` before dividing — otherwise integer division would truncate the result.

## Expected Output

```
Enter number of dice: 3
Enter number of sides per die: 6

=== DICE RESULTS ===
Die 1: 4
Die 2: 2
Die 3: 6
Total: 12
Average: 4.0
```
