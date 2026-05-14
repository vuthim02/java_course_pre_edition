# 05 — Arrays

Single and multi-dimensional arrays, the `Arrays` utility class, and the relationship between varargs and arrays.

## Array Declaration, Creation, and Initialization

```java
// ArrayBasics.java — all ways to create and initialize arrays
package com.example;

public class ArrayBasics {
    public static void main(String[] args) {
        // ── Declaration + separate creation ──
        int[] numbers;                   // preferred form
        numbers = new int[5];            // all elements default to 0

        // ── Declaration + creation + inline initialization ──
        int[] primes = {2, 3, 5, 7, 11};

        // ── Declaration + creation with new + initializer ──
        int[] squares = new int[]{1, 4, 9, 16, 25};

        // ── Access and modify ──
        System.out.println("First prime: " + primes[0]);    // 2
        System.out.println("Array length: " + primes.length); // 5
        primes[0] = 99;   // overwrite
        System.out.println("After overwrite: " + primes[0]);

        // ── Quick fill using a loop ──
        int[] filled = new int[10];
        for (int i = 0; i < filled.length; i++) {
            filled[i] = i * i;
        }
        // Print using enhanced for
        System.out.print("Squares: ");
        for (int v : filled) {
            System.out.print(v + " ");
        }
        System.out.println();

        // ── of() helper methods ──
        int[] fromOf = java.util.stream.IntStream.of(10, 20, 30).toArray();
        System.out.println("From IntStream.of: " + fromOf.length);
    }
}
```

## Enhanced for Loop with Arrays

```java
// EnhancedForArray.java — for-each with arrays
package com.example;

public class EnhancedForArray {
    public static void main(String[] args) {
        String[] fruits = {"Apple", "Banana", "Cherry", "Date"};

        // Read-only iteration — cannot modify the array through the loop variable
        for (String fruit : fruits) {
            System.out.println("Fruit: " + fruit);
        }

        // Summing with enhanced for
        int[] values = {5, 10, 15, 20};
        int sum = 0;
        for (int v : values) {
            sum += v;
        }
        System.out.println("Sum: " + sum);

        // Finding max
        int max = values[0];
        for (int v : values) {
            if (v > max) max = v;
        }
        System.out.println("Max: " + max);
    }
}
```

## Multidimensional Arrays (2D, Ragged Arrays)

```java
// MultiDimArrayDemo.java — 2D arrays and jagged/ragged arrays
package com.example;

public class MultiDimArrayDemo {
    public static void main(String[] args) {
        // ── Rectangular 2D array ──
        int[][] matrix = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };

        System.out.println("matrix[1][2] = " + matrix[1][2]);   // 6

        // Print with nested loops
        for (int r = 0; r < matrix.length; r++) {
            for (int c = 0; c < matrix[r].length; c++) {
                System.out.print(matrix[r][c] + " ");
            }
            System.out.println();
        }

        // ── Ragged array (rows have different lengths) ──
        int[][] ragged = new int[4][];
        ragged[0] = new int[]{1};
        ragged[1] = new int[]{2, 3};
        ragged[2] = new int[]{4, 5, 6};
        ragged[3] = new int[]{7, 8, 9, 10};

        System.out.println("\nRagged array:");
        for (int[] row : ragged) {
            for (int val : row) {
                System.out.print(val + " ");
            }
            System.out.println();
        }

        // ── 3D array example ──
        int[][][] cube = new int[2][3][4];
        cube[0][1][2] = 42;
        System.out.println("\n3D access: cube[0][1][2] = " + cube[0][1][2]);

        // ── Enhanced for on 2D ──
        System.out.print("Using enhanced for: ");
        for (int[] row : matrix) {
            for (int val : row) {
                System.out.print(val + " ");
            }
        }
        System.out.println();
    }
}
```

## Arrays Utility Class

```java
// ArraysUtilDemo.java — java.util.Arrays methods
package com.example;

import java.util.Arrays;

public class ArraysUtilDemo {
    public static void main(String[] args) {
        // ── sort ──
        int[] unsorted = {9, 3, 7, 1, 5};
        Arrays.sort(unsorted);
        System.out.println("Sorted: " + Arrays.toString(unsorted));

        // ── binarySearch (array must be sorted) ──
        int idx = Arrays.binarySearch(unsorted, 5);
        System.out.println("Index of 5: " + idx);

        idx = Arrays.binarySearch(unsorted, 99);
        System.out.println("Index of 99 (not found): " + idx);  // negative

        // ── fill ──
        int[] filled = new int[5];
        Arrays.fill(filled, 7);
        System.out.println("Filled with 7: " + Arrays.toString(filled));

        // ── copyOf ──
        int[] original = {1, 2, 3};
        int[] copy = Arrays.copyOf(original, 5);  // extra slots are 0
        System.out.println("Copied (len 5): " + Arrays.toString(copy));

        int[] truncated = Arrays.copyOf(original, 2);
        System.out.println("Truncated (len 2): " + Arrays.toString(truncated));

        // ── copyOfRange ──
        int[] range = Arrays.copyOfRange(original, 1, 3);  // from index 1 to 2
        System.out.println("Range [1..3): " + Arrays.toString(range));

        // ── equals ──
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};
        int[] c = {3, 2, 1};
        System.out.println("a.equals(b): " + Arrays.equals(a, b));  // true
        System.out.println("a.equals(c): " + Arrays.equals(a, c));  // false (different order)

        // ── toString ──
        int[] data = {10, 20, 30};
        System.out.println("toString: " + Arrays.toString(data));

        // ── deepToString (for nested arrays) ──
        int[][] grid = {{1, 2}, {3, 4}};
        System.out.println("deepToString: " + Arrays.deepToString(grid));

        // ── stream (Java 8+) ──
        int[] nums = {4, 8, 15, 16, 23, 42};
        long sum = Arrays.stream(nums).sum();
        double avg = Arrays.stream(nums).average().orElse(0);
        System.out.println("Stream sum: " + sum + ", avg: " + avg);

        // ── parallelPrefix (Java 8+) ──
        int[] prefix = {1, 2, 3, 4};
        Arrays.parallelPrefix(prefix, (x, y) -> x * y);
        System.out.println("Cumulative product: " + Arrays.toString(prefix));
        // prefix becomes [1, 1*2=2, 1*2*3=6, 1*2*3*4=24]
    }
}
```

## Varargs and Arrays

```java
// VarargsDemo.java — variable-length argument list is syntactic sugar for arrays
package com.example;

public class VarargsDemo {

    // Varargs — the compiler transforms this into an array parameter
    static int sum(int... numbers) {
        // Inside the method, `numbers` is just an int[]
        int total = 0;
        for (int n : numbers) {
            total += n;
        }
        return total;
    }

    // Varargs with other parameters — varargs must be LAST
    static String format(String prefix, String... items) {
        StringJoiner sj = new StringJoiner(", ", prefix + "[", "]");
        for (String item : items) {
            sj.add(item);
        }
        return sj.toString();
    }

    // You can call varargs with an explicit array
    static void printAll(Object... args) {
        for (Object arg : args) {
            System.out.print(arg + " ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        // Calling varargs with individual arguments
        System.out.println("sum(1, 2, 3) = " + sum(1, 2, 3));
        System.out.println("sum()       = " + sum());        // empty is OK
        System.out.println("sum(100)    = " + sum(100));

        // Varargs with other params
        System.out.println(format("Fruits", "Apple", "Banana", "Cherry"));

        // Passing an array directly to a varargs method
        int[] arrayArg = {10, 20, 30};
        System.out.println("sum(arrayArg) = " + sum(arrayArg));   // same as passing elements

        // Object... allows mixing types
        printAll("Hello", 42, 3.14, true);

        // ── Varargs is really just an array at runtime ──
        // These two calls are identical after compilation:
        System.out.println("sum(new int[]{1,2,3}) = " + sum(new int[]{1, 2, 3}));
        System.out.println("sum(1,2,3)            = " + sum(1, 2, 3));
        // The compiler turns the second call into the first one automatically.
    }
}

// Helper — not in java.util, a minimal one for the example
class StringJoiner {
    private String prefix, delimiter, suffix;
    private StringBuilder sb = new StringBuilder();
    private boolean first = true;

    StringJoiner(String delimiter) { this("", delimiter, ""); }

    StringJoiner(String prefix, String delimiter, String suffix) {
        this.prefix = prefix;
        this.delimiter = delimiter;
        this.suffix = suffix;
    }

    void add(String s) {
        if (!first) sb.append(delimiter);
        sb.append(s);
        first = false;
    }

    public String toString() { return prefix + sb.toString() + suffix; }
}
```
