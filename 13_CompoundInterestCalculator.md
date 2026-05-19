# Lesson 13: Compound Interest Calculator

## Key Concepts
- Building a practical financial calculator
- The compound interest formula: `A = P(1 + r/n)^(nt)`
- Using `Math.pow()` for exponentiation
- Combining `Scanner` with `printf` for formatted monetary output

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== COMPOUND INTEREST CALCULATOR ===");

        System.out.print("Enter principal amount: $");
        double principal = scanner.nextDouble();

        System.out.print("Enter annual interest rate (%%): ");
        double rate = scanner.nextDouble() / 100;

        System.out.print("Enter time in years: ");
        int years = scanner.nextInt();

        System.out.print("Enter compounding per year: ");
        int n = scanner.nextInt();

        double amount = principal * Math.pow(1 + (rate / n), n * years);

        System.out.printf("\nAfter %d years, you will have $%,.2f%n", years, amount);
        System.out.printf("Interest earned: $%,.2f%n", amount - principal);

        scanner.close();
    }
}
```

### Explanation
- **Principal** — the initial amount invested
- **Rate** — entered as a percentage (e.g., 5), then divided by 100 to get 0.05
- **Years** — the time period
- **Compounding per year (n)** — e.g., 12 for monthly, 1 for yearly
- Formula: `amount = principal * (1 + rate/n)^(n * years)`
- `Math.pow(base, exponent)` computes the exponentiation
- `printf` with `%,.2f` formats the result as currency with commas
- Interest earned = final amount − principal

## Expected Output

```
=== COMPOUND INTEREST CALCULATOR ===
Enter principal amount: $1000
Enter annual interest rate (%): 5
Enter time in years: 10
Enter compounding per year: 12

After 10 years, you will have $1,647.01
Interest earned: $647.01
```
