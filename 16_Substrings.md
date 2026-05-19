# Lesson 16: Substrings

## Key Concepts
- The `substring()` method extracts a portion of a string
- `substring(int beginIndex)` — returns from `beginIndex` to the end
- `substring(int beginIndex, int endIndex)` — returns from `beginIndex` to `endIndex - 1`
- `indexOf()` finds the position of a character or substring
- `contains()` checks if a substring exists in a string

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your email: ");
        String email = scanner.nextLine();

        if (email.contains("@")) {
            String username = email.substring(0, email.indexOf("@"));
            String domain = email.substring(email.indexOf("@") + 1);

            System.out.println("Username: " + username);
            System.out.println("Domain: " + domain);
        } else {
            System.out.println("Invalid email address.");
        }

        String text = "Hello, World!";
        System.out.println("\nsubstring(7): " + text.substring(7));
        System.out.println("substring(0, 5): " + text.substring(0, 5));

        scanner.close();
    }
}
```

## Explanation
1. The user enters an email address.
2. `contains("@")` checks if the email is valid.
3. `indexOf("@")` finds the position of `@`.
4. `substring(0, indexOf("@"))` extracts the username (characters before `@`).
5. `substring(indexOf("@") + 1)` extracts the domain (everything after `@`).
6. Two standalone substring examples show the two overloaded versions.

## Expected Output

```
Enter your email: john@example.com
Username: john
Domain: example.com

substring(7): World!
substring(0, 5): Hello
```
