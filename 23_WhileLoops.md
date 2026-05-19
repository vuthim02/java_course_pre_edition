# Lesson 23: While Loops

## Key Concepts
- `while` loop — runs *while* a condition is true (may run 0 times)
- `do-while` loop — always runs the body *at least once*
- Loop counter variables
- Sentinel-controlled loops (exit when a sentinel value is entered)

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int count = 1;
        while (count <= 5) {
            System.out.println("Count: " + count);
            count++;
        }

        System.out.println("\nDo-while loop:");
        int num;
        do {
            System.out.print("Enter a number (0 to exit): ");
            num = scanner.nextInt();
            if (num != 0) {
                System.out.println("You entered: " + num);
            }
        } while (num != 0);

        System.out.println("Goodbye!");

        scanner.close();
    }
}
```

## Explanation
1. **`while` loop**: counts from 1 to 5. The condition is checked *before* each iteration.
2. **`do-while` loop**: prompts the user repeatedly until they enter `0`. The condition is checked *after* the body, so it always runs at least once.
3. The sentinel value `0` signals the loop to stop.

## Expected Output

```
Count: 1
Count: 2
Count: 3
Count: 4
Count: 5

Do-while loop:
Enter a number (0 to exit): 3
You entered: 3
Enter a number (0 to exit): 7
You entered: 7
Enter a number (0 to exit): 0
Goodbye!
```
