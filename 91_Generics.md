# Lesson 91: Generics

## Key Concepts
- Generics enable type-safe code by parameterizing types with `<T>`
- Generic classes can hold any type without casting
- Generic methods work with any type while maintaining type safety
- Bounded type parameters (`T extends Comparable<T>`) restrict types to those with specific capabilities
- Raw types (without generics) are allowed but lose type safety

## Code Example

```java
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> intBox = new Box<>(42);
        Box<Double> doubleBox = new Box<>(3.14);

        System.out.println("String box: " + stringBox.getContents());
        System.out.println("Int box: " + intBox.getContents());
        System.out.println("Double box: " + doubleBox.getContents());

        Integer[] intArray = {1, 2, 3, 4, 5};
        String[] strArray = {"A", "B", "C", "D"};

        System.out.println("\nInteger array:");
        printArray(intArray);

        System.out.println("String array:");
        printArray(strArray);

        System.out.println("\nMax of 10, 20, 5: " + findMax(10, 20, 5));
        System.out.println("Max of 3.14, 2.71, 1.41: " + findMax(3.14, 2.71, 1.41));
    }

    static <T> void printArray(T[] array) {
        for (T element : array) {
            System.out.print(element + " ");
        }
        System.out.println();
    }

    static <T extends Comparable<T>> T findMax(T a, T b, T c) {
        T max = a;
        if (b.compareTo(max) > 0) max = b;
        if (c.compareTo(max) > 0) max = c;
        return max;
    }
}

class Box<T> {
    private T contents;

    Box(T contents) {
        this.contents = contents;
    }

    T getContents() {
        return contents;
    }

    void setContents(T contents) {
        this.contents = contents;
    }
}
```

## Explanation
1. `Box<T>` is a generic class — `T` is a type parameter that gets replaced with a real type at compile time (e.g., `Box<String>`, `Box<Integer>`).
2. The diamond operator `<>` lets Java infer the type from the declaration.
3. `printArray(T[])` is a generic method — it works with any array type.
4. `findMax(T a, T b, T c)` uses a bounded type parameter `T extends Comparable<T>`, meaning `T` must implement `Comparable` so `compareTo()` is available.
5. Without the bound, the compiler would not know `compareTo()` exists on `T`.

## Expected Output

```
String box: Hello
Int box: 42
Double box: 3.14

Integer array:
1 2 3 4 5
String array:
A B C D

Max of 10, 20, 5: 20
Max of 3.14, 2.71, 1.41: 3.14
```
