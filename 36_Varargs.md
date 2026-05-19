# Lesson 36: Varargs (Variable-Length Arguments)

## Key Concepts
- Varargs syntax: `type... name` (three dots after the type)
- Allows a method to accept zero or more arguments of the specified type
- Inside the method, varargs is treated as an array
- A method can have only one varargs parameter, and it must be the last parameter
- Varargs makes APIs more flexible — no need for method overloading

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Sum of 1, 2, 3: " + sum(1, 2, 3));
        System.out.println("Sum of 1, 2, 3, 4, 5: " + sum(1, 2, 3, 4, 5));
        System.out.println("Sum of nothing: " + sum());

        System.out.println("\nAverage of 10, 20, 30: " + average(10, 20, 30));
        System.out.println("Average of 5, 15, 25, 35: " + average(5, 15, 25, 35));

        printStrings("Hello", "World", "Java");
        printStrings("One");
    }

    static int sum(int... numbers) {
        int total = 0;
        for (int num : numbers) {
            total += num;
        }
        return total;
    }

    static double average(int... numbers) {
        if (numbers.length == 0) return 0;
        int total = sum(numbers);
        return (double) total / numbers.length;
    }

    static void printStrings(String... strings) {
        System.out.println("\nPrinting " + strings.length + " string(s):");
        for (String s : strings) {
            System.out.println("  " + s);
        }
    }
}
```

## Explanation
1. `int... numbers` means the method can be called with any number of `int` arguments (including zero). Inside the method, `numbers` is accessed as an `int[]` array.
2. The `sum()` method works with 3 arguments, 5 arguments, or no arguments at all — the same code handles all cases.
3. `average()` calls `sum()` internally and uses `numbers.length` to compute the average, with a guard clause for the empty case.
4. `printStrings(String... strings)` shows that varargs works with any type, including `String`.
5. The `length` property is available on the varargs parameter just like a regular array.

## Expected Output

```
Sum of 1, 2, 3: 6
Sum of 1, 2, 3, 4, 5: 15
Sum of nothing: 0

Average of 10, 20, 30: 20.0
Average of 5, 15, 25, 35: 20.0

Printing 3 string(s):
  Hello
  World
  Java

Printing 1 string(s):
  One
```
