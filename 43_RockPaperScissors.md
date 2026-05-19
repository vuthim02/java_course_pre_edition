# Lesson 43: Rock Paper Scissors Game

## Key Concepts
- Game loop that runs until a player reaches a target score (first to 3 wins)
- Random computer choice using `Random` and an array of choices
- Comparing player input against computer choice with compound conditions (`||`)
- Input validation and case-insensitive comparison with `.toLowerCase()`
- Using `continue` to skip the rest of the loop iteration on invalid input
- Tracking and displaying a running score

## Code Example

```java
import java.util.Random;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        String[] choices = {"rock", "paper", "scissors"};
        int playerScore = 0;
        int computerScore = 0;

        System.out.println("=== ROCK PAPER SCISSORS ===");
        System.out.println("First to 3 wins!");

        while (playerScore < 3 && computerScore < 3) {
            System.out.print("\nEnter rock, paper, or scissors: ");
            String playerChoice = scanner.nextLine().toLowerCase();

            int computerIndex = random.nextInt(3);
            String computerChoice = choices[computerIndex];

            System.out.println("Computer chose: " + computerChoice);

            if (playerChoice.equals(computerChoice)) {
                System.out.println("It's a tie!");
            } else if ((playerChoice.equals("rock") && computerChoice.equals("scissors")) ||
                       (playerChoice.equals("paper") && computerChoice.equals("rock")) ||
                       (playerChoice.equals("scissors") && computerChoice.equals("paper"))) {
                System.out.println("You win this round!");
                playerScore++;
            } else if (playerChoice.equals("rock") || playerChoice.equals("paper") || playerChoice.equals("scissors")) {
                System.out.println("Computer wins this round!");
                computerScore++;
            } else {
                System.out.println("Invalid choice!");
                continue;
            }

            System.out.println("Score - You: " + playerScore + " Computer: " + computerScore);
        }

        if (playerScore == 3) {
            System.out.println("\nCongratulations! You won the game!");
        } else {
            System.out.println("\nComputer wins the game!");
        }

        scanner.close();
    }
}
```

## Explanation
1. The game continues (`while`) until either the player or computer reaches 3 points.
2. The computer picks a random index (0-2) to select from the `choices` array.
3. `.toLowerCase()` makes the player's input case-insensitive.
4. Win conditions are checked with compound logical OR (`||`): rock beats scissors, paper beats rock, scissors beats paper.
5. If the player's choice is valid but didn't win and wasn't a tie, the computer wins. An extra `else if` validates the input — if it's none of the three valid options, `continue` skips to the next iteration without updating the score.
6. After the loop, the winner is announced based on who reached 3 first.

## Expected Output

```
=== ROCK PAPER SCISSORS ===
First to 3 wins!

Enter rock, paper, or scissors: rock
Computer chose: scissors
You win this round!
Score - You: 1 Computer: 0

Enter rock, paper, or scissors: paper
Computer chose: rock
You win this round!
Score - You: 2 Computer: 0

Enter rock, paper, or scissors: rock
Computer chose: rock
It's a tie!
Score - You: 2 Computer: 0

Enter rock, paper, or scissors: scissors
Computer chose: paper
You win this round!
Score - You: 3 Computer: 0

Congratulations! You won the game!
```
