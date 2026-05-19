# Lesson 26: Break and Continue

## Key Concepts
- `break` — exits a loop immediately
- `continue` — skips the rest of the current iteration and moves to the next
- Labeled `break` — exits an outer loop from inside a nested loop
- Sentinel-controlled `while (true)` loops

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Break example - enter names (type 'quit' to stop):");
        while (true) {
            System.out.print("Enter name: ");
            String name = scanner.nextLine();
            if (name.equalsIgnoreCase("quit")) {
                break;
            }
            System.out.println("Hello " + name + "!");
        }

        System.out.println("\nContinue example - skip even numbers:");
        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) {
                continue;
            }
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("\nBreak with label - nested loops:");
        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (i == 2 && j == 2) {
                    break outer;
                }
                System.out.println("i=" + i + ", j=" + j);
            }
        }
        System.out.println("Exited outer loop.");

        scanner.close();
    }
}
```

## Explanation
1. **`break`** in `while (true)`: the loop runs forever until `"quit"` is entered, which triggers `break`.
2. **`continue`**: when `i` is even, `continue` skips the `print` and goes to the next iteration — only odd numbers are printed.
3. **Labeled `break`**: the label `outer:` marks the outer loop. `break outer` breaks out of *both* loops when `i == 2 && j == 2`.

## Expected Output

```
Break example - enter names (type 'quit' to stop):
Enter name: Alice
Hello Alice!
Enter name: Bob
Hello Bob!
Enter name: quit

Continue example - skip even numbers:
1 3 5 7 9 

Break with label - nested loops:
i=1, j=1
i=1, j=2
i=1, j=3
i=2, j=1
Exited outer loop.
```
