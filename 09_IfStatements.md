# Lesson 9: If Statements

## Key Concepts
- **`if`**, **`else if`**, and **`else`** for conditional logic
- Comparison operators: `>=`, `==`, `!=`, etc.
- Checking string equality with `.equals()` (not `==`)
- Checking for empty strings with `.equals("")`

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your age: ");
        int age = scanner.nextInt();

        if (age >= 18) {
            System.out.println("You are an adult!");
        } else if (age >= 13) {
            System.out.println("You are a teenager.");
        } else {
            System.out.println("You are a child.");
        }

        scanner.nextLine();
        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine();

        if (name.equals("")) {
            System.out.println("You didn't enter your name!");
        } else {
            System.out.println("Hello " + name + "!");
        }

        scanner.close();
    }
}
```

### Explanation
- `if (condition)` — runs the block if condition is `true`
- `else if` — checked only if the previous `if` was `false`
- `else` — runs if none of the above conditions were true
- **Always use `.equals()`** to compare `String` values, never `==`
- The extra `scanner.nextLine()` after `nextInt()` consumes the leftover newline so the next `nextLine()` works correctly

## Expected Output

```
Enter your age: 16
You are a teenager.

Enter your name: Alex
Hello Alex!
```

```
Enter your age: 7

Enter your name: 
You didn't enter your name!
```
