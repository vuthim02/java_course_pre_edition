# Lesson 44: Slot Machine

## Key Concepts
- Building a slot machine game with balance tracking and betting
- String array of slot symbols (including emoji symbols in Java strings)
- Generating three random symbols with `Random`
- Checking match conditions: three-of-a-kind vs two-of-a-kind
- Multiplier payouts (10x for jackpot, 2x for partial match)
- Deducting bet from balance before resolving the round
- Game loop continues while balance > 0; player can quit by betting 0

## Code Example

```java
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        int balance = 100;
        String[] symbols = {"🍒", "🍋", "🔔", "⭐", "💎"};
        int bet;

        System.out.println("=== SLOT MACHINE ===");
        System.out.println("Starting balance: $" + balance);

        while (balance > 0) {
            System.out.print("\nEnter your bet (0 to quit): $");
            bet = scanner.nextInt();

            if (bet == 0) break;
            if (bet > balance) {
                System.out.println("Not enough balance!");
                continue;
            }

            balance -= bet;

            String[] result = {
                symbols[random.nextInt(symbols.length)],
                symbols[random.nextInt(symbols.length)],
                symbols[random.nextInt(symbols.length)]
            };

            System.out.println("[" + result[0] + "] [" + result[1] + "] [" + result[2] + "]");

            if (result[0].equals(result[1]) && result[1].equals(result[2])) {
                int winnings = bet * 10;
                balance += winnings;
                System.out.println("JACKPOT! You won $" + winnings);
            } else if (result[0].equals(result[1]) || result[1].equals(result[2]) || result[0].equals(result[2])) {
                int winnings = bet * 2;
                balance += winnings;
                System.out.println("You won $" + winnings);
            } else {
                System.out.println("No match. You lost $" + bet);
            }

            System.out.println("Balance: $" + balance);
        }

        System.out.println("\nFinal balance: $" + balance);
        System.out.println("Thanks for playing!");
        scanner.close();
    }
}
```

## Explanation
1. The player starts with a balance of $100. Each round they place a bet, which is deducted from the balance.
2. Three random symbols are selected from the `symbols` array using `random.nextInt(symbols.length)`.
3. Match checking:
   - **Three of a kind** (all equal): wins 10x the bet.
   - **Two of a kind** (any pair equal): wins 2x the bet.
   - **No match**: the bet is lost.
4. Winnings are added back to the balance. The balance can go up or down depending on luck.
5. The loop continues until the player types `0` to quit or the balance reaches $0.
6. The `break` statement exits the loop when `bet == 0`. The `continue` statement skips the rest of the iteration when `bet > balance`.

## Expected Output

```
=== SLOT MACHINE ===
Starting balance: $100

Enter your bet (0 to quit): $10
[🍒] [🍋] [🍒]
You won $20
Balance: $110

Enter your bet (0 to quit): $20
[🍒] [🍒] [🍒]
JACKPOT! You won $200
Balance: $290

Enter your bet (0 to quit): $10
[🍋] [🔔] [⭐]
No match. You lost $10
Balance: $280

Enter your bet (0 to quit): 0

Final balance: $280
Thanks for playing!
```
