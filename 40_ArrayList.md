# Lesson 40: ArrayList

## Key Concepts
- `ArrayList` is a resizable-array implementation from `java.util`
- Declaring: `ArrayList<String> names = new ArrayList<>();`
- Common methods: `add()`, `get()`, `set()`, `remove()`, `size()`, `contains()`, `indexOf()`, `clear()`, `isEmpty()`
- `Collections.sort()` and `Collections.reverse()` for utility operations
- ArrayList only works with objects (use wrapper classes for primitives, e.g., `ArrayList<Integer>`)
- Type safety with generics (the `<String>` syntax)

## Code Example

```java
import java.util.ArrayList;
import java.util.Collections;

public class Main {
    public static void main(String[] args) {
        ArrayList<String> names = new ArrayList<>();

        names.add("Alice");
        names.add("Bob");
        names.add("Charlie");
        names.add("Diana");

        System.out.println("ArrayList: " + names);
        System.out.println("Size: " + names.size());
        System.out.println("Element at index 2: " + names.get(2));

        names.remove("Bob");
        System.out.println("After removing Bob: " + names);

        names.set(1, "Bernard");
        System.out.println("After setting index 1: " + names);

        System.out.println("Contains 'Alice': " + names.contains("Alice"));
        System.out.println("Index of 'Alice': " + names.indexOf("Alice"));

        Collections.sort(names);
        System.out.println("Sorted: " + names);

        Collections.reverse(names);
        System.out.println("Reversed: " + names);

        names.clear();
        System.out.println("After clear: " + names);
        System.out.println("Is empty: " + names.isEmpty());

        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        numbers.add(40);
        numbers.add(50);

        int sum = 0;
        for (int num : numbers) {
            sum += num;
        }
        System.out.println("\nNumbers: " + numbers);
        System.out.println("Sum: " + sum);
    }
}
```

## Explanation
1. `ArrayList<String> names = new ArrayList<>()` creates a list that can only hold `String` objects. The diamond operator `<>` infers the type.
2. `add()` appends to the end. `get(index)` retrieves an element. `set(index, value)` replaces an element.
3. `remove(Object)` removes the first occurrence of the specified element. `size()` returns the number of elements.
4. `contains()` checks if an element exists. `indexOf()` returns its position or -1 if not found.
5. `Collections.sort(names)` sorts the list in place. `Collections.reverse(names)` reverses the order.
6. `clear()` removes all elements. `isEmpty()` returns true if the list has no elements.
7. For primitives, use the wrapper class: `ArrayList<Integer>` — autoboxing handles conversion automatically.
8. The for-each loop works with ArrayList just like arrays.

## Expected Output

```
ArrayList: [Alice, Bob, Charlie, Diana]
Size: 4
Element at index 2: Charlie
After removing Bob: [Alice, Charlie, Diana]
After setting index 1: [Alice, Bernard, Diana]
Contains 'Alice': true
Index of 'Alice': 0
Sorted: [Alice, Bernard, Diana]
Reversed: [Diana, Bernard, Alice]
After clear: []
Is empty: true

Numbers: [10, 20, 30, 40, 50]
Sum: 150
```
