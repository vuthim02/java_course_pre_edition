# Java Foundations — Lesson 7: Arrays

## What is an Array?

An array is a **fixed-size container** that holds multiple values of the SAME type, arranged in sequence.

```
int[] scores = new int[5];

Memory layout:
  ┌──────┬──────┬──────┬──────┬──────┐
  │ [0]  │ [1]  │ [2]  │ [3]  │ [4]  │
  │  0   │  0   │  0   │  0   │  0   │
  └──────┴──────┴──────┴──────┴──────┘
index 0  index 1  index 2  index 3  index 4
```

**Mental model:** An array is a **row of numbered boxes**. Each box holds one value. Boxes are numbered starting at 0 (zero-indexed).

## Declaring and Creating Arrays

```java
// Method 1: Declare + allocate (default values)
int[] numbers = new int[5];        // [0, 0, 0, 0, 0]
String[] names = new String[3];    // [null, null, null]
boolean[] flags = new boolean[2];  // [false, false]

// Method 2: Declare + initialize with values
int[] scores = {95, 87, 73, 91, 88};
String[] fruits = {"apple", "banana", "cherry"};

// Method 3: Declare + new + values
int[] ages = new int[]{25, 30, 35, 40};

// Method 4: Declare first, allocate later
int[] data;
data = new int[10];
```

## Accessing Array Elements

```java
int[] scores = {95, 87, 73, 91, 88};

// Read an element
int first = scores[0];   // 95
int third = scores[2];   // 73

// Write an element
scores[1] = 99;          // Change 87 → 99
// scores is now [95, 99, 73, 91, 88]

// The last element
int last = scores[scores.length - 1];  // 88
```

## The Length of an Array

```java
int[] arr = {10, 20, 30, 40, 50};
System.out.println(arr.length);  // 5 (NOT a method — it's a field!)
```

**Important:** `length` is a **field**, not a method. No parentheses!
- `arr.length` ✅ — array length
- `arr.length()` ❌ — compile error
- `"hello".length()` ✅ — String's method

## Iterating Over Arrays

### For loop with index

```java
int[] scores = {95, 87, 73, 91, 88};

for (int i = 0; i < scores.length; i++) {
    System.out.println("Score " + i + ": " + scores[i]);
}
```

### Enhanced for-each loop

```java
for (int score : scores) {
    System.out.println("Score: " + score);
}
// Simpler, but you DON'T have the index
```

### When to use which:
- **For loop** — Need index to modify elements or know position
- **For-each** — Just reading values in order

## Common Array Operations

```java
// Sum all elements
int[] numbers = {5, 10, 15, 20, 25};
int sum = 0;
for (int num : numbers) {
    sum += num;
}
System.out.println("Sum: " + sum);  // 75

// Find maximum
int max = numbers[0];
for (int num : numbers) {
    if (num > max) {
        max = num;
    }
}
System.out.println("Max: " + max);  // 25

// Find average
double average = (double) sum / numbers.length;
```

## Multidimensional Arrays

### 2D Array (like a grid/table/matrix)

```java
// Create a 3×4 grid
int[][] grid = new int[3][4];

grid[0][0] = 1;    // First row, first column
grid[2][3] = 42;   // Last row, last column

// Initialize with values
int[][] matrix = {
    {1, 2, 3, 4},
    {5, 6, 7, 8},
    {9, 10, 11, 12}
};
```

```
Visual representation:
       Col 0  Col 1  Col 2  Col 3
Row 0: [ 1 ]  [ 2 ]  [ 3 ]  [ 4 ]
Row 1: [ 5 ]  [ 6 ]  [ 7 ]  [ 8 ]
Row 2: [ 9 ]  [10 ]  [11 ]  [12 ]

Access: matrix[row][column]
matrix[0][0] = 1
matrix[2][3] = 12
```

### Nested loops for 2D arrays

```java
for (int row = 0; row < matrix.length; row++) {
    for (int col = 0; col < matrix[row].length; col++) {
        System.out.print(matrix[row][col] + "\t");
    }
    System.out.println();  // new line after each row
}
```

### Ragged Arrays (Rows of different lengths)

```java
int[][] triangle = new int[5][];
for (int i = 0; i < triangle.length; i++) {
    triangle[i] = new int[i + 1];
    for (int j = 0; j < triangle[i].length; j++) {
        triangle[i][j] = i + j;
    }
}

/*
triangle:
[0]
[0, 1]
[0, 1, 2]
[0, 1, 2, 3]
[0, 1, 2, 3, 4]
*/
```

## Arrays Utility Class (`java.util.Arrays`)

```java
import java.util.Arrays;

int[] arr = {5, 2, 8, 1, 9};

// Sort
Arrays.sort(arr);                    // [1, 2, 5, 8, 9]

// Search (BINARY search — array MUST be sorted first!)
int index = Arrays.binarySearch(arr, 8);  // 3

// Fill
Arrays.fill(arr, 0);                 // [0, 0, 0, 0, 0]

// Copy
int[] copy = Arrays.copyOf(arr, arr.length);
int[] partial = Arrays.copyOfRange(arr, 1, 4);

// Compare
int[] a = {1, 2, 3};
int[] b = {1, 2, 3};
boolean eq = Arrays.equals(a, b);    // true

// Print (toString for 1D, deepToString for 2D)
System.out.println(Arrays.toString(arr));        // [0, 0, 0, 0, 0]
int[][] matrix = {{1,2},{3,4}};
System.out.println(Arrays.deepToString(matrix)); // [[1,2],[3,4]]

// Parallel prefix (Java 8+)
Arrays.parallelPrefix(arr, (x, y) -> x + y);  // cumulative sum
```

## Array Reference Behavior

**Arrays are objects** in Java. Variables hold **references** to the array, not the array itself:

```java
int[] a = {1, 2, 3};
int[] b = a;        // b points to SAME array as a!

b[0] = 999;         // Modifies the same array!
System.out.println(a[0]);  // 999 ← a changed too!

// To copy VALUES (separate copy):
int[] c = Arrays.copyOf(a, a.length);
c[0] = 0;
System.out.println(a[0]);  // 999 ← unchanged
System.out.println(c[0]);  // 0
```

## Common Array Pitfalls

### IndexOutOfBoundsException

```java
int[] arr = {10, 20, 30};
System.out.println(arr[0]);  // OK  — 10
System.out.println(arr[2]);  // OK  — 30
System.out.println(arr[3]);  // BUG! — IndexOutOfBoundsException (max index is 2)
```

### Uninitialized Array Elements

```java
int[] arr = new int[3];
System.out.println(arr[0]);  // 0 (default for int)
// For objects: null (default for references)
```

### Varargs — Methods with Variable Arguments

```java
public static int sum(int... numbers) {
    int total = 0;
    for (int n : numbers) {
        total += n;
    }
    return total;
}

// Called with any number of arguments:
sum(1, 2);            // 3
sum(1, 2, 3, 4, 5);  // 15
sum();                // 0 (zero arguments is valid!)
```

---

### Exercises

1. Create an array of 10 integers, fill it with the numbers 1-10, then print them in reverse.
2. Find the **second largest** element in an array of integers.
3. Create a 2D array representing a 4×4 identity matrix and print it.
4. Write a method that takes an int array and returns the reversed array (without modifying the original).
5. Find all duplicate values in an array.
