# Lesson 10: Random Numbers

## Key Concepts
- Importing `java.util.Random`
- Creating a `Random` object
- `nextInt()` — random integer (any range or bounded)
- `nextDouble()` — random double between 0.0 and 1.0
- `nextBoolean()` — random `true` or `false`
- Generating numbers in a specific range: `random.nextInt(max) + min`
- Using a loop to generate multiple random values

## Code Example

```java
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        Random random = new Random();

        int randomInt = random.nextInt();
        System.out.println("Random int: " + randomInt);

        int diceRoll = random.nextInt(6) + 1;
        System.out.println("Dice roll (1-6): " + diceRoll);

        double randomDouble = random.nextDouble();
        System.out.println("Random double (0-1): " + randomDouble);

        boolean randomBoolean = random.nextBoolean();
        System.out.println("Random boolean: " + randomBoolean);

        int range = random.nextInt(50) + 50;
        System.out.println("Random in range (50-99): " + range);

        System.out.println("\n5 random dice rolls:");
        for (int i = 0; i < 5; i++) {
            System.out.print((random.nextInt(6) + 1) + " ");
        }
        System.out.println();
    }
}
```

### Explanation
- `nextInt()` without arguments returns any `int` (including negatives)
- `nextInt(6)` returns 0–5; adding 1 shifts it to 1–6 (simulates a dice roll)
- `nextDouble()` returns a value in [0.0, 1.0)
- `nextBoolean()` returns `true` or `false` randomly
- To get a range like 50–99: `nextInt(50) + 50` (returns 0–49, then adds 50)
- The `for` loop generates 5 dice rolls in a single line

## Expected Output

```
Random int: -1423096041
Dice roll (1-6): 4
Random double (0-1): 0.7328912345678901
Random boolean: true
Random in range (50-99): 73

5 random dice rolls:
3 6 1 5 2 
```
*(Actual output will vary each run — these are random!)*
