# Lesson 18: Ternary Operator

## Key Concepts
- Shorthand for `if-else`: `condition ? valueIfTrue : valueIfFalse`
- Can be chained for multiple conditions
- Used to assign values concisely
- Also called the *conditional operator*

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter your score: ");
        int score = scanner.nextInt();

        String grade = (score >= 60) ? "Pass" : "Fail";
        System.out.println("Result: " + grade);

        String message = (score >= 90) ? "Excellent!" :
                         (score >= 80) ? "Good job!" :
                         (score >= 70) ? "Not bad." :
                         (score >= 60) ? "Barely passed." : "Better luck next time.";
        System.out.println("Feedback: " + message);

        System.out.print("\nEnter a number: ");
        int number = scanner.nextInt();
        String parity = (number % 2 == 0) ? "even" : "odd";
        System.out.println(number + " is " + parity + ".");

        scanner.close();
    }
}
```

## Explanation
1. `(score >= 60) ? "Pass" : "Fail"` — if true, returns `"Pass"`; otherwise `"Fail"`.
2. Chained ternaries check score thresholds in order (90, 80, 70, 60).
3. `(number % 2 == 0) ? "even" : "odd"` checks parity.
4. Ternaries make simple conditional assignments cleaner than full `if-else` blocks.

## Expected Output

```
Enter your score: 85
Result: Pass
Feedback: Good job!

Enter a number: 7
7 is odd.
```
