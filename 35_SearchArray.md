# Lesson 35: Search Array

## Key Concepts
- Linear search algorithm — checking each element one by one
- Using `.equals()` for string comparison vs `==` for primitives
- `.toLowerCase()` for case-insensitive comparison
- Returning `-1` as a sentinel value meaning "not found"
- Breaking out of a loop early with `break` when a match is found
- Encapsulating search logic in a reusable method (`linearSearch`)

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        String[] fruits = {"apple", "banana", "orange", "grape", "mango", "kiwi"};

        System.out.print("Enter a fruit to search: ");
        String target = scanner.nextLine().toLowerCase();

        boolean found = false;
        int index = -1;

        for (int i = 0; i < fruits.length; i++) {
            if (fruits[i].equals(target)) {
                found = true;
                index = i;
                break;
            }
        }

        if (found) {
            System.out.println(target + " found at index " + index);
        } else {
            System.out.println(target + " not found in the array.");
        }

        int[] numbers = {10, 25, 37, 42, 55, 68, 73, 89};
        System.out.print("\nEnter a number to search: ");
        int numTarget = scanner.nextInt();

        int numIndex = linearSearch(numbers, numTarget);
        if (numIndex != -1) {
            System.out.println(numTarget + " found at index " + numIndex);
        } else {
            System.out.println(numTarget + " not found.");
        }

        scanner.close();
    }

    static int linearSearch(int[] arr, int target) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
```

## Explanation
1. For string arrays, `.equals()` is used because strings are objects; `==` compares references, not content. `.toLowerCase()` converts the user's input to lowercase so the search is case-insensitive.
2. A `boolean found` flag and `int index` track whether and where the target was found. The `break` statement exits the loop as soon as a match is found.
3. The `linearSearch` method accepts an `int[]` array and a target value, iterates through the array, and returns the index of the match or `-1` if not found. This makes the search logic reusable.

## Expected Output

```
Enter a fruit to search: mango
mango found at index 4

Enter a number to search: 42
42 found at index 3
```
