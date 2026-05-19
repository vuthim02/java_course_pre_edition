# Lesson 99: HashSet

## Key Concepts
- `HashSet<E>` stores unique elements — duplicates are silently ignored
- `add(e)`, `remove(e)`, `contains(e)`, `size()`, `isEmpty()`
- No guaranteed order (use `LinkedHashSet` for insertion order)
- Mathematical set operations:
  - `addAll(other)` — union (elements in either set)
  - `retainAll(other)` — intersection (elements in both sets)
  - `removeAll(other)` — difference (elements in first but not second)

## Code Example

```java
import java.util.HashSet;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== HashSet Demo ===\n");

        HashSet<String> fruits = new HashSet<>();

        fruits.add("apple");
        fruits.add("banana");
        fruits.add("orange");
        fruits.add("apple");
        fruits.add("grape");
        fruits.add("banana");

        System.out.println("Fruits set: " + fruits);
        System.out.println("Size: " + fruits.size());
        System.out.println("Contains 'apple': " + fruits.contains("apple"));

        fruits.remove("grape");
        System.out.println("After removing grape: " + fruits);

        System.out.println("\nIterating:");
        for (String fruit : fruits) {
            System.out.println("  " + fruit);
        }

        HashSet<Integer> numbers1 = new HashSet<>();
        HashSet<Integer> numbers2 = new HashSet<>();

        for (int i = 1; i <= 10; i++) numbers1.add(i);
        for (int i = 5; i <= 15; i++) numbers2.add(i);

        System.out.println("\nSet 1 (1-10): " + numbers1);
        System.out.println("Set 2 (5-15): " + numbers2);

        Set<Integer> union = new HashSet<>(numbers1);
        union.addAll(numbers2);
        System.out.println("Union: " + union);

        Set<Integer> intersection = new HashSet<>(numbers1);
        intersection.retainAll(numbers2);
        System.out.println("Intersection: " + intersection);

        Set<Integer> difference = new HashSet<>(numbers1);
        difference.removeAll(numbers2);
        System.out.println("Difference (1-4): " + difference);
    }
}
```

## Explanation
1. Duplicate entries (`"apple"`, `"banana"`) are added twice but only stored once. The size is 4 (not 6).
2. `remove("grape")` deletes an element; `contains("apple")` checks membership.
3. Iterating over a HashSet does not guarantee order.
4. **Union** — `addAll(numbers2)` adds all elements from numbers2 to a copy of numbers1, producing `{1..15}`.
5. **Intersection** — `retainAll(numbers2)` keeps only elements present in both sets: `{5,6,7,8,9,10}`.
6. **Difference** — `removeAll(numbers2)` removes any element also in numbers2, leaving `{1,2,3,4}`.

## Expected Output

```
=== HashSet Demo ===

Fruits set: [orange, banana, apple, grape]
Size: 4
Contains 'apple': true
After removing grape: [orange, banana, apple]

Iterating:
  orange
  banana
  apple

Set 1 (1-10): [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
Set 2 (5-15): [5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
Union: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15]
Intersection: [5, 6, 7, 8, 9, 10]
Difference (1-4): [1, 2, 3, 4]
```
