# Lesson 6: Mad Libs Game

## Key Concepts
- Taking multiple user inputs to build a fun story
- Reinforcing `Scanner` and `nextLine()`
- String concatenation to construct sentences dynamically

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter an adjective: ");
        String adjective1 = scanner.nextLine();

        System.out.print("Enter a noun: ");
        String noun1 = scanner.nextLine();

        System.out.print("Enter a verb: ");
        String verb1 = scanner.nextLine();

        System.out.print("Enter an adverb: ");
        String adverb1 = scanner.nextLine();

        System.out.print("Enter an adjective: ");
        String adjective2 = scanner.nextLine();

        System.out.print("Enter a noun: ");
        String noun2 = scanner.nextLine();

        System.out.println("\n=== MAD LIB ===");
        System.out.println("Today I went to a " + adjective1 + " " + noun1 + ".");
        System.out.println("I decided to " + verb1 + " " + adverb1 + ".");
        System.out.println("Then I saw a " + adjective2 + " " + noun2 + ".");
        System.out.println("What a great day!");

        scanner.close();
    }
}
```

### Explanation
- The program asks the user for 6 words: two adjectives, two nouns, one verb, and one adverb
- Each input is stored in a separate `String` variable
- The words are inserted into a short story template using `+` concatenation
- Mad Libs is a great way to practice variables, input, and output

## Expected Output

```
Enter an adjective: scary
Enter a noun: castle
Enter a verb: run
Enter an adverb: quickly
Enter an adjective: big
Enter a noun: dragon

=== MAD LIB ===
Today I went to a scary castle.
I decided to run quickly.
Then I saw a big dragon.
What a great day!
```
