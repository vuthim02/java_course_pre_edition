# Lesson 34: User Input Array

## Key Concepts
- Creating an array whose size is determined by user input at runtime
- Using `scanner.nextLine()` after `scanner.nextInt()` to consume the leftover newline
- Filling an array using a `for` loop
- Processing array data (sum, average)
- Converting an array to a string with `java.util.Arrays.toString()`

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("How many items do you want to enter? ");
        int size = scanner.nextInt();
        scanner.nextLine();

        String[] items = new String[size];

        for (int i = 0; i < items.length; i++) {
            System.out.print("Enter item " + (i + 1) + ": ");
            items[i] = scanner.nextLine();
        }

        System.out.println("\nYou entered:");
        for (int i = 0; i < items.length; i++) {
            System.out.println((i + 1) + ". " + items[i]);
        }

        int[] numbers = new int[size];
        int sum = 0;
        System.out.println("\nNow enter " + size + " numbers:");
        for (int i = 0; i < numbers.length; i++) {
            System.out.print("Number " + (i + 1) + ": ");
            numbers[i] = scanner.nextInt();
            sum += numbers[i];
        }

        System.out.println("\nNumbers: " + java.util.Arrays.toString(numbers));
        System.out.println("Sum: " + sum);
        System.out.println("Average: " + (double) sum / numbers.length);

        scanner.close();
    }
}
```

## Explanation
1. `scanner.nextInt()` reads the integer but leaves the newline character in the buffer. The `scanner.nextLine()` call immediately after consumes that leftover newline, so subsequent `nextLine()` calls work correctly.
2. A `String[]` array of user-specified size is created and each slot is filled by prompting the user.
3. A second `int[]` array of the same size is filled with numbers whose sum is accumulated during input.
4. The average is computed by casting `sum` to `double` before division to avoid integer truncation.

## Expected Output

```
How many items do you want to enter? 3
Enter item 1: apple
Enter item 2: banana
Enter item 3: orange

You entered:
1. apple
2. banana
3. orange

Now enter 3 numbers:
Number 1: 10
Number 2: 20
Number 3: 30

Numbers: [10, 20, 30]
Sum: 60
Average: 20.0
```
