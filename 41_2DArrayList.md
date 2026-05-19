# Lesson 41: 2D ArrayList

## Key Concepts
- `ArrayList` of `ArrayList`s: `ArrayList<ArrayList<String>>`
- Each inner ArrayList represents a row or category
- Accessing elements: `list.get(row).get(col)`
- Nested loops to iterate over both dimensions
- Dynamically building a 2D structure by adding `new ArrayList<>()` per row in a loop
- Useful for modeling tables, categories, and grid-like data

## Code Example

```java
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArrayList<ArrayList<String>> groceryList = new ArrayList<>();

        ArrayList<String> produceList = new ArrayList<>();
        produceList.add("apple");
        produceList.add("banana");
        produceList.add("orange");

        ArrayList<String> dairyList = new ArrayList<>();
        dairyList.add("milk");
        dairyList.add("cheese");
        dairyList.add("yogurt");

        ArrayList<String> meatList = new ArrayList<>();
        meatList.add("chicken");
        meatList.add("beef");
        meatList.add("fish");

        groceryList.add(produceList);
        groceryList.add(dairyList);
        groceryList.add(meatList);

        System.out.println("2D ArrayList - Grocery List:");
        for (ArrayList<String> category : groceryList) {
            System.out.print("[");
            for (int i = 0; i < category.size(); i++) {
                System.out.print(category.get(i));
                if (i < category.size() - 1) System.out.print(", ");
            }
            System.out.println("]");
        }

        System.out.println("\nGet item [1][2]: " + groceryList.get(1).get(2));

        ArrayList<ArrayList<Integer>> grid = new ArrayList<>();
        for (int row = 0; row < 3; row++) {
            grid.add(new ArrayList<>());
            for (int col = 0; col < 3; col++) {
                grid.get(row).add((row + 1) * (col + 1));
            }
        }

        System.out.println("\nNumber grid:");
        for (ArrayList<Integer> row : grid) {
            for (int val : row) {
                System.out.printf("%4d", val);
            }
            System.out.println();
        }
    }
}
```

## Explanation
1. `ArrayList<ArrayList<String>> groceryList` declares a list that holds other lists — analogous to a 2D array but dynamic.
2. Each inner list (produce, dairy, meat) is built independently and then added to the outer list with `groceryList.add(...)`.
3. Iteration uses a for-each loop on the outer list to get each row (an `ArrayList<String>`), then a standard or for-each loop on the inner list to access elements.
4. `groceryList.get(1).get(2)` first gets the second row (index 1 = dairy), then gets the third element (index 2 = yogurt).
5. The number grid demonstrates building a 2D ArrayList dynamically: in each row iteration, a new `ArrayList<Integer>()` is created and populated in a nested column loop.

## Expected Output

```
2D ArrayList - Grocery List:
[apple, banana, orange]
[milk, cheese, yogurt]
[chicken, beef, fish]

Get item [1][2]: yogurt

Number grid:
   1   2   3
   2   4   6
   3   6   9
```
