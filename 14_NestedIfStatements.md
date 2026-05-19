# Lesson 14: Nested If Statements

## Key Concepts
- Placing `if` statements inside other `if` statements (nesting)
- `equalsIgnoreCase()` — case-insensitive string comparison
- Building a movie theater access system with multiple rules
- Handling multiple user inputs in a single session

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== MOVIE THEATER ===");
        System.out.print("Enter your age: ");
        int age = scanner.nextInt();

        System.out.print("Do you have a VIP pass? (yes/no): ");
        scanner.nextLine();
        String vip = scanner.nextLine();

        if (age >= 18) {
            if (vip.equalsIgnoreCase("yes")) {
                System.out.println("You can watch any movie (VIP).");
            } else {
                System.out.println("You can watch regular movies.");
                System.out.print("Would you like a large popcorn? (yes/no): ");
                String popcorn = scanner.nextLine();
                if (popcorn.equalsIgnoreCase("yes")) {
                    System.out.println("Large popcorn added - $8.99");
                }
            }
        } else if (age >= 13) {
            System.out.println("You can watch PG-13 movies.");
        } else {
            System.out.println("You can watch G or PG movies.");
            System.out.print("Do you have a guardian? (yes/no): ");
            String guardian = scanner.nextLine();
            if (guardian.equalsIgnoreCase("yes")) {
                System.out.println("You can watch PG movies with guardian.");
            }
        }

        scanner.close();
    }
}
```

### Explanation
- The outer `if` checks age brackets (adult / teen / child)
- Inside each branch, further `if` statements check additional conditions:
  - **Adults**: VIP check → extra popcorn upsell for non-VIP
  - **Teens**: simple message, no further conditions
  - **Children**: guardian check to allow PG movies
- `equalsIgnoreCase("yes")` accepts "yes", "YES", "Yes", etc.
- The `scanner.nextLine()` after `nextInt()` consumes the leftover newline

## Expected Output

```
=== MOVIE THEATER ===
Enter your age: 20
Do you have a VIP pass? (yes/no): no
You can watch regular movies.
Would you like a large popcorn? (yes/no): yes
Large popcorn added - $8.99
```

```
=== MOVIE THEATER ===
Enter your age: 8
Do you have a VIP pass? (yes/no): no
You can watch G or PG movies.
Do you have a guardian? (yes/no): yes
You can watch PG movies with guardian.
```
