# Lesson 24: Number Guessing Game

## Key Concepts
- `Random` class for generating random numbers
- `random.nextInt(100) + 1` generates a number from 1 to 100
- `while` loop with a boolean flag (`won`)
- Tracking attempt count
- Comparing user input to a secret value

## Code Example

```java
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        int secretNumber = random.nextInt(100) + 1;
        int guess;
        int attempts = 0;
        boolean won = false;

        System.out.println("=== NUMBER GUESSING GAME ===");
        System.out.println("Guess a number between 1 and 100!");

        while (!won) {
            System.out.print("Enter your guess: ");
            guess = scanner.nextInt();
            attempts++;

            if (guess < secretNumber) {
                System.out.println("Too low! Try again.");
            } else if (guess > secretNumber) {
                System.out.println("Too high! Try again.");
            } else {
                System.out.println("Correct! The number was " + secretNumber);
                System.out.println("You got it in " + attempts + " attempts!");
                won = true;
            }
        }

        scanner.close();
    }
}
```

## Explanation
1. `Random random = new Random()` creates a random number generator.
2. `random.nextInt(100) + 1` picks a number between 1 and 100.
3. The `while` loop continues until `won` becomes `true`.
4. Each guess increments the attempt counter.
5. Hints ("Too low" / "Too high") guide the player.
6. When guessed correctly, `won = true` exits the loop.

## Expected Output

```
=== NUMBER GUESSING GAME ===
Guess a number between 1 and 100!
Enter your guess: 50
Too low! Try again.
Enter your guess: 75
Too high! Try again.
Enter your guess: 62
Correct! The number was 62
You got it in 3 attempts!
```
