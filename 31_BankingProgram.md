# Lesson 31: Banking Program

## Key Concepts
- Building a complete interactive console application
- Using `static` variables shared across methods
- `switch` expression with arrow syntax (`->`)
- `printf()` for formatted output (`%n` for newline, `%.2f` for 2 decimal places)
- Input validation (checking for positive amounts, sufficient balance)
- Separating logic into methods for modular code
- `Scanner.nextDouble()` for reading decimal numbers

## Code Example

```java
import java.util.Scanner;

public class Main {
    static double balance = 0;
    static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        boolean running = true;

        while (running) {
            System.out.println("\n=== BANKING PROGRAM ===");
            System.out.println("1. Check Balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Exit");
            System.out.print("Choose option: ");

            int choice = scanner.nextInt();

            switch (choice) {
                case 1 -> checkBalance();
                case 2 -> deposit();
                case 3 -> withdraw();
                case 4 -> {
                    System.out.println("Thank you for banking with us!");
                    running = false;
                }
                default -> System.out.println("Invalid option.");
            }
        }
        scanner.close();
    }

    static void checkBalance() {
        System.out.printf("Current balance: $%.2f%n", balance);
    }

    static void deposit() {
        System.out.print("Enter deposit amount: $");
        double amount = scanner.nextDouble();
        if (amount > 0) {
            balance += amount;
            System.out.printf("Deposited $%.2f%n", amount);
        } else {
            System.out.println("Invalid amount.");
        }
    }

    static void withdraw() {
        System.out.print("Enter withdraw amount: $");
        double amount = scanner.nextDouble();
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            System.out.printf("Withdrew $%.2f%n", amount);
        } else if (amount > balance) {
            System.out.println("Insufficient funds!");
        } else {
            System.out.println("Invalid amount.");
        }
    }
}
```

## Explanation
1. `balance` and `scanner` are declared `static` so all methods can access them.
2. The `while` loop keeps the program running until the user chooses to exit.
3. A `switch` expression maps menu choices to method calls using arrow syntax.
4. `checkBalance()` prints the current balance formatted to 2 decimal places.
5. `deposit()` validates that the amount is positive before adding to balance.
6. `withdraw()` checks that the amount is positive AND does not exceed the current balance.
7. `scanner.close()` is called when the program ends to release resources.

## Expected Output

```
=== BANKING PROGRAM ===
1. Check Balance
2. Deposit
3. Withdraw
4. Exit
Choose option: 2
Enter deposit amount: $100
Deposited $100.00

=== BANKING PROGRAM ===
1. Check Balance
2. Deposit
3. Withdraw
4. Exit
Choose option: 1
Current balance: $100.00

=== BANKING PROGRAM ===
1. Check Balance
2. Deposit
3. Withdraw
4. Exit
Choose option: 3
Enter withdraw amount: $30
Withdrew $30.00

=== BANKING PROGRAM ===
1. Check Balance
2. Deposit
3. Withdraw
4. Exit
Choose option: 4
Thank you for banking with us!
```
