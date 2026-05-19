# Lesson 101: Lambda Expressions

## Key Concepts
- Lambda expressions provide a concise way to implement functional interfaces (interfaces with a single abstract method)
- Syntax: `(parameters) -> expression` or `(parameters) -> { statements }`
- Lambdas can replace anonymous classes for single-method interfaces
- They enable functional-style programming: sorting, filtering, iterating
- The `@FunctionalInterface` annotation (not shown) can enforce single-method contracts
- Type inference works — parameter types are often optional

## Code Example

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Lambda Expressions Demo ===\n");

        Greeting greeting = () -> System.out.println("Hello from lambda!");
        greeting.greet();

        MathOperation add = (a, b) -> a + b;
        MathOperation subtract = (a, b) -> a - b;
        MathOperation multiply = (a, b) -> a * b;
        MathOperation divide = (a, b) -> b != 0 ? a / b : 0;

        System.out.println("10 + 5 = " + operate(10, 5, add));
        System.out.println("10 - 5 = " + operate(10, 5, subtract));
        System.out.println("10 * 5 = " + operate(10, 5, multiply));
        System.out.println("10 / 5 = " + operate(10, 5, divide));

        System.out.println("\n--- Sorting with lambdas ---");
        List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob", "David"));

        Collections.sort(names);
        System.out.println("Natural order: " + names);

        Collections.sort(names, (a, b) -> b.compareTo(a));
        System.out.println("Reverse order: " + names);

        Collections.sort(names, (a, b) -> Integer.compare(a.length(), b.length()));
        System.out.println("By length: " + names);

        System.out.println("\n--- Filtering with lambdas ---");
        List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        List<Integer> evens = filter(numbers, n -> n % 2 == 0);
        System.out.println("Even numbers: " + evens);

        List<Integer> odds = filter(numbers, n -> n % 2 != 0);
        System.out.println("Odd numbers: " + odds);

        List<Integer> big = filter(numbers, n -> n > 5);
        System.out.println("Numbers > 5: " + big);

        System.out.println("\n--- ForEach with lambdas ---");
        names.forEach(name -> System.out.println("Hello, " + name + "!"));
    }

    static int operate(int a, int b, MathOperation op) {
        return op.operate(a, b);
    }

    static List<Integer> filter(List<Integer> list, Predicate condition) {
        List<Integer> result = new ArrayList<>();
        for (Integer n : list) {
            if (condition.test(n)) {
                result.add(n);
            }
        }
        return result;
    }
}

interface Greeting {
    void greet();
}

interface MathOperation {
    int operate(int a, int b);
}

interface Predicate {
    boolean test(int n);
}
```

## Explanation
1. `() -> System.out.println("Hello from lambda!")` — a lambda with no parameters implementing `Greeting`.
2. `(a, b) -> a + b` — a lambda with two parameters implementing `MathOperation`. The body is a single expression; no `return` keyword needed.
3. `operate(10, 5, add)` — passing a lambda as a behavior argument (strategy pattern).
4. `Collections.sort(names, (a, b) -> b.compareTo(a))` — using a lambda as a `Comparator` to sort in reverse order.
5. The `filter` method takes a `Predicate` lambda — each call passes different logic for filtering.
6. `names.forEach(name -> ...)` — Java's built-in `Iterable.forEach()` accepts a `Consumer` lambda.
7. The interfaces `Greeting`, `MathOperation`, and `Predicate` are all functional interfaces (single abstract method).

## Expected Output

```
=== Lambda Expressions Demo ===

Hello from lambda!
10 + 5 = 15
10 - 5 = 5
10 * 5 = 50
10 / 5 = 2

--- Sorting with lambdas ---
Natural order: [Alice, Bob, Charlie, David]
Reverse order: [David, Charlie, Bob, Alice]
By length: [Bob, Alice, David, Charlie]

--- Filtering with lambdas ---
Even numbers: [2, 4, 6, 8, 10]
Odd numbers: [1, 3, 5, 7, 9]
Numbers > 5: [6, 7, 8, 9, 10]

--- ForEach with lambdas ---
Hello, Alice!
Hello, Bob!
Hello, Charlie!
Hello, David!
```
