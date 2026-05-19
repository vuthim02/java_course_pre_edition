# Lesson 38: For-Each Loop

## Key Concepts
- The enhanced for-each loop syntax: `for (Type variable : collection)`
- Automatically iterates over every element without an index counter
- Works with both arrays and `ArrayList`
- Cannot modify the array/collection structure during iteration
- No access to the current index — use a standard `for` loop when index is needed
- Cleaner, more readable code when you just need to read/process each element

## Code Example

```java
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        String[] animals = {"dog", "cat", "bird", "fish"};

        System.out.println("Array with for-each:");
        for (String animal : animals) {
            System.out.println("Animal: " + animal);
        }

        ArrayList<String> fruits = new ArrayList<>();
        fruits.add("apple");
        fruits.add("banana");
        fruits.add("orange");
        fruits.add("grape");

        System.out.println("\nArrayList with for-each:");
        for (String fruit : fruits) {
            System.out.println("Fruit: " + fruit);
        }

        int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        System.out.println("\nSum of 1-10: " + sum);

        char[] letters = {'J', 'A', 'V', 'A'};
        System.out.print("\nLetters: ");
        for (char c : letters) {
            System.out.print(c);
        }
        System.out.println();
    }
}
```

## Explanation
1. The for-each loop reads as "for each `String animal` in `animals`". The loop variable takes on the value of each element in sequence.
2. It works identically for arrays and `ArrayList` — the same syntax applies.
3. Accumulating a sum is clean with for-each because we don't need an index; we just add each number.
4. For-each works with any element type, including `char` primitives.
5. **Limitation**: You cannot modify the collection (add/remove elements) while iterating with for-each. Use a standard `for` loop with an index if you need to modify the collection or access positions.

## Expected Output

```
Array with for-each:
Animal: dog
Animal: cat
Animal: bird
Animal: fish

ArrayList with for-each:
Fruit: apple
Fruit: banana
Fruit: orange
Fruit: grape

Sum of 1-10: 55

Letters: JAVA
```
