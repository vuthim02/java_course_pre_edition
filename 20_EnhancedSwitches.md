# Lesson 20: Enhanced Switches

## Key Concepts
- Java 14+ enhanced `switch` expressions
- Arrow syntax `->` instead of colons and breaks
- Multiple cases on one line: `case "a", "b" ->`
- `yield` returns a value from a switch block
- Switches can be used as expressions (assigned to variables)

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter a day of the week: ");
        String day = scanner.nextLine().toLowerCase();

        String result = switch (day) {
            case "monday", "tuesday", "wednesday", "thursday", "friday" -> "Weekday";
            case "saturday", "sunday" -> "Weekend";
            default -> "Invalid day";
        };
        System.out.println(day + " is a " + result);

        System.out.print("\nEnter a number (1-3): ");
        int num = scanner.nextInt();

        String word = switch (num) {
            case 1 -> { yield "One"; }
            case 2 -> { yield "Two"; }
            case 3 -> { yield "Three"; }
            default -> "Unknown";
        };
        System.out.println("You entered: " + word);

        scanner.close();
    }
}
```

## Explanation
1. The `switch` expression returns a value directly assigned to a variable.
2. Arrow `->` replaces the traditional colon + `break` pattern — no fall-through.
3. Multiple values per case are comma-separated.
4. `yield` is used inside a block `{ }` when the case body needs multiple statements.
5. `default` handles all unmatched input.

## Expected Output

```
Enter a day of the week: Saturday
saturday is a Weekend

Enter a number (1-3): 2
You entered: Two
```
