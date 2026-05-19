# Lesson 22: Logical Operators

## Key Concepts
- `&&` (AND) — both conditions must be true
- `||` (OR) — at least one condition must be true
- `!` (NOT) — inverts a boolean expression
- Combining logical operators for complex conditions
- `equalsIgnoreCase()` for case-insensitive string comparison

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter temperature: ");
        int temp = scanner.nextInt();

        if (temp > 30) {
            System.out.println("It's hot outside!");
        } else if (temp >= 20 && temp <= 30) {
            System.out.println("It's warm outside.");
        } else {
            System.out.println("It's cold outside.");
        }

        System.out.print("\nAre you a member? (yes/no): ");
        scanner.nextLine();
        String member = scanner.nextLine();

        System.out.print("Do you have a coupon? (yes/no): ");
        String coupon = scanner.nextLine();

        if (member.equalsIgnoreCase("yes") || coupon.equalsIgnoreCase("yes")) {
            System.out.println("You get a discount!");
        } else {
            System.out.println("No discount available.");
        }

        System.out.print("\nEnter your age: ");
        int age = scanner.nextInt();

        if (!(age >= 18)) {
            System.out.println("You must be 18+ to enter.");
        } else {
            System.out.println("Welcome!");
        }

        scanner.close();
    }
}
```

## Explanation
1. `&&` — `temp >= 20 && temp <= 30` is true only when the temperature is between 20 and 30 inclusive.
2. `||` — membership OR coupon grants a discount.
3. `!` — `!(age >= 18)` is equivalent to `age < 18` (negation).
4. `equalsIgnoreCase()` compares strings without caring about case ("yes", "YES", "Yes").

## Expected Output

```
Enter temperature: 25
It's warm outside.

Are you a member? (yes/no): no
Do you have a coupon? (yes/no): yes
You get a discount!

Enter your age: 16
You must be 18+ to enter.
```
