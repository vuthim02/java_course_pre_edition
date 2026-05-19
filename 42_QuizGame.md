# Lesson 42: Quiz Game

## Key Concepts
- Parallel arrays: a question array, a 2D options array, and an answer key array all aligned by index
- 2D String array for multiple-choice options for each question
- Tracking a score with an accumulator variable
- Calculating a percentage with `(double)` cast and `%%` in `printf` to print a literal percent sign
- Using a for-each loop to print options within each question
- Comparing user input against the answer key

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] questions = {
            "What is the capital of France?",
            "Which planet is known as the Red Planet?",
            "What is 2 + 2?",
            "Who wrote 'Romeo and Juliet'?",
            "What is the largest ocean?"
        };

        String[][] options = {
            {"1. London", "2. Paris", "3. Berlin", "4. Madrid"},
            {"1. Venus", "2. Jupiter", "3. Mars", "4. Saturn"},
            {"1. 3", "2. 4", "3. 5", "4. 22"},
            {"1. Dickens", "2. Shakespeare", "3. Austen", "4. Hemingway"},
            {"1. Atlantic", "2. Indian", "3. Arctic", "4. Pacific"}
        };

        int[] answers = {2, 3, 2, 2, 4};
        int score = 0;

        System.out.println("=== QUIZ GAME ===\n");

        for (int i = 0; i < questions.length; i++) {
            System.out.println("Question " + (i + 1) + ": " + questions[i]);
            for (String option : options[i]) {
                System.out.println(option);
            }
            System.out.print("Your answer: ");
            int answer = scanner.nextInt();

            if (answer == answers[i]) {
                System.out.println("Correct!\n");
                score++;
            } else {
                System.out.println("Wrong! The answer was " + answers[i] + "\n");
            }
        }

        System.out.println("=== FINAL SCORE ===");
        System.out.println("You got " + score + " out of " + questions.length + " correct!");
        System.out.printf("Percentage: %.1f%%%n", (double) score / questions.length * 100);

        scanner.close();
    }
}
```

## Explanation
1. Three parallel arrays store the quiz data. `questions[i]`, `options[i]`, and `answers[i]` all correspond to the same question at index `i`.
2. `options` is a 2D `String` array: `options[i]` is a 1D array of four choice strings for question `i`.
3. The outer loop iterates through each question. The inner for-each loop prints all four options for the current question.
4. The user's input (`scanner.nextInt()`) is compared to the answer key. If correct, the score is incremented.
5. At the end, the score and percentage are displayed. In `printf`, `%%` prints a literal `%` character.
6. The percentage is calculated as `(double) score / questions.length * 100` — casting `score` to `double` ensures fractional division.

## Expected Output

```
=== QUIZ GAME ===

Question 1: What is the capital of France?
1. London
2. Paris
3. Berlin
4. Madrid
Your answer: 2
Correct!

Question 2: Which planet is known as the Red Planet?
1. Venus
2. Jupiter
3. Mars
4. Saturn
Your answer: 3
Correct!

Question 3: What is 2 + 2?
1. 3
2. 4
3. 5
4. 22
Your answer: 2
Correct!

Question 4: Who wrote 'Romeo and Juliet'?
1. Dickens
2. Shakespeare
3. Austen
4. Hemingway
Your answer: 2
Correct!

Question 5: What is the largest ocean?
1. Atlantic
2. Indian
3. Arctic
4. Pacific
Your answer: 4
Correct!

=== FINAL SCORE ===
You got 5 out of 5 correct!
Percentage: 100.0%
```
