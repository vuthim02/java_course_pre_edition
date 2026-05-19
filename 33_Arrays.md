# Lesson 33: Arrays

## Key Concepts
- Declaring arrays: `int[] numbers = new int[5];`
- Initializing arrays with values: `String[] fruits = {"apple", "banana"};`
- Accessing elements by index with bracket notation `numbers[0]`
- The `.length` property (not a method — no parentheses)
- Iterating over arrays with a standard `for` loop
- The enhanced for-each loop (`for (int score : scores)`)
- `Arrays.toString()` for printing array contents
- `Arrays.sort()` for sorting in ascending order
- Calculating average by casting to `double`

## Code Example

```java
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        int[] numbers = new int[5];
        numbers[0] = 10;
        numbers[1] = 20;
        numbers[2] = 30;
        numbers[3] = 40;
        numbers[4] = 50;

        System.out.println("Array elements:");
        for (int i = 0; i < numbers.length; i++) {
            System.out.println("numbers[" + i + "] = " + numbers[i]);
        }

        String[] fruits = {"apple", "banana", "orange", "grape"};
        System.out.println("\nFruits array:");
        for (int i = 0; i < fruits.length; i++) {
            System.out.println("fruits[" + i + "] = " + fruits[i]);
        }

        int[] scores = {85, 92, 78, 95, 88};
        int sum = 0;
        for (int score : scores) {
            sum += score;
        }
        double average = (double) sum / scores.length;
        System.out.println("\nScores: " + Arrays.toString(scores));
        System.out.println("Average: " + average);

        Arrays.sort(scores);
        System.out.println("Sorted: " + Arrays.toString(scores));
    }
}
```

## Explanation
1. `int[] numbers = new int[5]` creates an array of 5 integers, all initialized to 0. Each element is then assigned individually.
2. `String[] fruits = {"apple", "banana", ...}` creates and initializes an array in one step using an array literal.
3. `numbers.length` gives the number of elements (no parentheses because it's a field, not a method).
4. The for-each loop (`for (int score : scores)`) iterates over every element without needing an index variable.
5. `Arrays.toString(scores)` returns a human-readable string like `[85, 92, 78, 95, 88]`.
6. `Arrays.sort(scores)` sorts the array in place in ascending numerical order.

## Expected Output

```
Array elements:
numbers[0] = 10
numbers[1] = 20
numbers[2] = 30
numbers[3] = 40
numbers[4] = 50

Fruits array:
fruits[0] = apple
fruits[1] = banana
fruits[2] = orange
fruits[3] = grape

Scores: [85, 92, 78, 95, 88]
Average: 87.6
Sorted: [78, 85, 88, 92, 95]
```
