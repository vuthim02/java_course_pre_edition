# Lesson 98: HashMap

## Key Concepts
- `HashMap<K, V>` stores key-value pairs — fast lookups by key
- Keys are unique; putting a value with an existing key overwrites it
- `put(key, value)` adds or updates an entry
- `get(key)` retrieves the value; returns `null` if key not found
- `containsKey(key)` / `containsValue(value)` check existence
- `remove(key)` deletes an entry
- `replace(key, newValue)` updates a value
- `keySet()` returns all keys; `values()` returns all values
- `forEach()` with a lambda iterates over entries
- `HashMap` does not guarantee insertion order (use `LinkedHashMap` if needed)

## Code Example

```java
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        HashMap<String, String> capitals = new HashMap<>();

        capitals.put("USA", "Washington D.C.");
        capitals.put("France", "Paris");
        capitals.put("Japan", "Tokyo");
        capitals.put("UK", "London");
        capitals.put("Germany", "Berlin");

        System.out.println("=== HashMap Demo ===\n");
        System.out.println("All entries:");

        for (String country : capitals.keySet()) {
            System.out.println(country + " -> " + capitals.get(country));
        }

        System.out.println("\nCapital of France: " + capitals.get("France"));

        System.out.println("Contains key 'Italy': " + capitals.containsKey("Italy"));
        System.out.println("Contains value 'Tokyo': " + capitals.containsValue("Tokyo"));

        capitals.remove("UK");
        System.out.println("\nAfter removing UK: " + capitals.size() + " entries");
        System.out.println("Is empty: " + capitals.isEmpty());

        capitals.replace("USA", "Washington, D.C.");
        System.out.println("Updated USA: " + capitals.get("USA"));

        System.out.println("\n--- Student Grades ---");
        HashMap<String, Integer> grades = new HashMap<>();
        grades.put("Alice", 95);
        grades.put("Bob", 87);
        grades.put("Charlie", 92);
        grades.put("Diana", 78);

        int sum = 0;
        for (int grade : grades.values()) {
            sum += grade;
        }
        System.out.println("Average grade: " + (double) sum / grades.size());

        grades.forEach((name, grade) -> {
            System.out.println(name + ": " + grade);
        });
    }
}
```

## Explanation
1. `HashMap<String, String> capitals` maps country names to capital cities.
2. `keySet()` iterates over all keys, and `get(key)` retrieves each value.
3. `containsKey("Italy")` returns `false` because "Italy" was never added.
4. `remove("UK")` deletes that entry; `size()` then returns 4.
5. `replace("USA", "Washington, D.C.")` updates the value for an existing key.
6. The Student Grades section shows how to compute an average by iterating over `values()`.
7. `forEach((name, grade) -> ...)` uses a lambda that receives both key and value.

## Expected Output

```
=== HashMap Demo ===

All entries:
USA -> Washington D.C.
France -> Paris
Japan -> Tokyo
UK -> London
Germany -> Berlin

Capital of France: Paris
Contains key 'Italy': false
Contains value 'Tokyo': true

After removing UK: 4 entries
Is empty: false
Updated USA: Washington, D.C.

--- Student Grades ---
Average grade: 88.0
Alice: 95
Bob: 87
Charlie: 92
Diana: 78
```
