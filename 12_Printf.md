# Lesson 12: Printf

## Key Concepts
- `System.out.printf()` — formatted output (like C's `printf`)
- **Format specifiers**: `%s` (String), `%d` (int), `%f` (double), `%n` (newline)
- Width and precision: `%10s`, `%-10s`, `%.2f`, `%,.2f`
- `%05d` — zero-padded numbers
- `%X` — hexadecimal, `%o` — octal

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        String name = "Bro";
        int age = 25;
        double salary = 12345.6789;

        System.out.printf("Hello %s!%n", name);
        System.out.printf("You are %d years old.%n", age);
        System.out.printf("You earn $%,.2f per year.%n", salary);

        System.out.println("\nFormatting Examples:");
        System.out.printf("%-10s %-10s %-10s%n", "Name", "Age", "Salary");
        System.out.printf("%-10s %-10d %-10.2f%n", "Alice", 30, 50000.0);
        System.out.printf("%-10s %-10d %-10.2f%n", "Bob", 25, 60000.5);
        System.out.printf("%-10s %-10d %-10.2f%n", "Charlie", 35, 75000.99);

        System.out.println("\nNumber formats:");
        System.out.printf("Padded: '%05d'%n", 42);
        System.out.printf("Left:   '%-5d'%n", 42);
        System.out.printf("Right:  '%5d'%n", 42);
        System.out.printf("Hex:    %X%n", 255);
        System.out.printf("Octal:  %o%n", 255);
    }
}
```

### Explanation
- `%s` — placeholder for a `String`
- `%d` — placeholder for an `int`
- `%f` — placeholder for a `double` (`.2f` = 2 decimal places)
- `%,.2f` — comma as thousands separator + 2 decimal places
- `%-10s` — left-aligned in a 10-character wide field
- `%10s` — right-aligned in a 10-character wide field
- `%05d` — pad with leading zeros to width 5
- `%n` — platform-independent newline
- `%X` — uppercase hex, `%o` — octal

## Expected Output

```
Hello Bro!
You are 25 years old.
You earn $12,345.68 per year.

Formatting Examples:
Name       Age        Salary     
Alice      30         50000.00   
Bob        25         60000.50   
Charlie    35         75000.99   

Number formats:
Padded: '00042'
Left:   '42   '
Right:  '   42'
Hex:    FF
Octal:  377
```
