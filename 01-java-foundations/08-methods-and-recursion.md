# Java Foundations — Lesson 8: Methods & Recursion

## What is a Method?

A method is a **named block of reusable code** — like a recipe:

```
RECIPE: makeCoffee()
    1. Boil water
    2. Add coffee grounds
    3. Pour water over grounds
    4. Serve

You can CALL this recipe anytime you need coffee
without re-explaining the steps.
```

## Method Structure

```java
 public static int add(int a, int b) {
// ↑            ↑  ↑       ↑
//modifier return  method  parameters
    //     type     name
    return a + b;
    // ↑
    // return value
}
```

## Types of Methods

### 1. Takes input, returns output

```java
public static int square(int x) {
    return x * x;
}

int result = square(5);  // result = 25
```

### 2. Takes input, returns nothing (void)

```java
public static void printGreeting(String name) {
    System.out.println("Hello, " + name + "!");
    // No return statement needed for void
}

printGreeting("Alice");  // Prints: Hello, Alice!
```

### 3. Takes nothing, returns output

```java
public static String getWelcomeMessage() {
    return "Welcome to Java!";
}

String msg = getWelcomeMessage();
```

### 4. Takes nothing, returns nothing

```java
public static void printSeparator() {
    System.out.println("====================");
}

printSeparator();
```

## Parameters vs Arguments

```java
public static void greet(String name, int age) {
    //                   ↑              ↑
    //              PARAMETERS (the names used in the method)
    System.out.println("Hi " + name + ", you are " + age);
}

greet("Alice", 30);
//    ↑        ↑
// ARGUMENTS (the actual values passed)
```

**Parameters** = variables in the method definition
**Arguments** = values you pass when calling

## Method Overloading

Multiple methods with the **same name** but **different parameters**:

```java
public static int add(int a, int b) {
    return a + b;
}

public static double add(double a, double b) {
    return a + b;
}

public static int add(int a, int b, int c) {
    return a + b + c;
}

// Usage:
add(5, 10);           // calls int version → 15
add(5.5, 2.5);        // calls double version → 8.0
add(1, 2, 3);         // calls 3-param version → 6
```

The compiler knows which method to call based on the **arguments you pass** (number and types).

## Pass by Value

**Java ALWAYS passes by value** — it copies the value to the parameter:

```java
public static void changeValue(int x) {
    x = 100;  // Only changes the LOCAL copy
}

int num = 5;
changeValue(num);
System.out.println(num);  // STILL 5! The original is unchanged
```

BUT — with objects, the "value" is a **reference**:

```java
public static void changeFirst(int[] arr) {
    arr[0] = 999;  // Modifies the actual array object
}

int[] data = {1, 2, 3};
changeFirst(data);
System.out.println(data[0]);  // 999 — the array was modified
```

Mental model: You can't change WHICH box someone gives you, but you can change what's INSIDE the box.

## Method Scope

Variables declared inside a method are LOCAL — they don't exist outside:

```java
public static void myMethod() {
    int localVar = 10;  // local to this method
    // localVar accessible here
}

// localVar NOT accessible here — it's out of scope
```

## The Call Stack

When method A calls method B, Java keeps track using a **stack**:

```java
public static void main(String[] args) {
    int x = 5;
    int y = square(x);  // (1) main calls square
    System.out.println(y);
}

public static int square(int n) {
    return multiply(n, n);  // (2) square calls multiply
}

public static int multiply(int a, int b) {
    return a * b;  // (3) multiply executes, returns to square
}
```

```
CALL STACK DURING EXECUTION:

Step 1              Step 2              Step 3              Step 4
┌──────────┐       ┌──────────┐       ┌──────────┐       ┌──────────┐
│          │       │multiply  │       │          │       │          │
│          │       │ a=5,b=5  │       │          │       │          │
│          │       ├──────────┤       ├──────────┤       ├──────────┤
│  main()  │       │  square  │       │  square  │       │  main()  │
│  x=5     │       │  n=5     │       │  n=5     │       │  x=5     │
│          │       ├──────────┤       ├──────────┤       │  y=25    │
│          │       │  main()  │       │  main()  │       │          │
│          │       │  x=5     │       │  x=5     │       │          │
└──────────┘       └──────────┘       └──────────┘       └──────────┘
   main()           main() calls      multiply runs,     square returns
   calls            square, square    returns to         25 to main,
   square()         calls multiply    square, square     stack unrolled
                                     pops multiply
```

## Recursion

A method that **calls itself**:

```java
public static int factorial(int n) {
    if (n <= 1) {      // BASE CASE — stops recursion
        return 1;
    }
    return n * factorial(n - 1);  // RECURSIVE CASE — calls itself
}

// factorial(5) = 5 * factorial(4)
//             = 5 * 4 * factorial(3)
//             = 5 * 4 * 3 * factorial(2)
//             = 5 * 4 * 3 * 2 * factorial(1)
//             = 5 * 4 * 3 * 2 * 1
//             = 120
```

### Every Recursive Method Needs Two Things:

1. **Base Case** — When to stop (otherwise: infinite recursion → StackOverflowError)
2. **Recursive Case** — Call itself with a smaller/simpler input

### Visualizing Recursion

```
factorial(5)
  │
  ├── 5 * factorial(4)
  │        │
  │        ├── 4 * factorial(3)
  │        │        │
  │        │        ├── 3 * factorial(2)
  │        │        │        │
  │        │        │        ├── 2 * factorial(1)
  │        │        │        │        │
  │        │        │        │        └── 1 (BASE CASE!)
  │        │        │        │
  │        │        │        └── 2 * 1 = 2
  │        │        │
  │        │        └── 3 * 2 = 6
  │        │
  │        └── 4 * 6 = 24
  │
  └── 5 * 24 = 120
```

### More Recursion Examples

```java
// Fibonacci sequence
public static int fibonacci(int n) {
    if (n <= 1) {
        return n;
    }
    return fibonacci(n - 1) + fibonacci(n - 2);
}
// fibonacci(6) = 8  (0, 1, 1, 2, 3, 5, 8)

// Sum of array elements
public static int sumArray(int[] arr, int index) {
    if (index >= arr.length) {
        return 0;  // base case
    }
    return arr[index] + sumArray(arr, index + 1);
}

// Palindrome checker
public static boolean isPalindrome(String s) {
    if (s.length() <= 1) {
        return true;  // base case
    }
    if (s.charAt(0) != s.charAt(s.length() - 1)) {
        return false;  // mismatch found
    }
    return isPalindrome(s.substring(1, s.length() - 1));
}
```

### Recursion vs Iteration

| Aspect | Recursion | Iteration (loops) |
|--------|-----------|-------------------|
| **Code** | Often shorter, more readable | Slightly more verbose |
| **Performance** | Slower (method call overhead) | Faster |
| **Memory** | Uses stack (limited!) | Uses heap (abundant) |
| **When to use** | Tree/Graph traversal, divide & conquer, backtracking | Simple repetitions, performance-critical |
| **Stack limit** | ~10,000 calls before StackOverflowError | No limit (infinite possible) |

---

### Exercises

1. Write a method `isEven(int n)` that returns `true` if the number is even.
2. Write an overloaded `max` method: one for 2 ints, one for 3 ints, one for an array.
3. Write a recursive method `power(base, exp)` that computes `base^exp`.
4. Write a recursive method `countDown(n)` that prints n, n-1, ..., 1.
5. **Recursion Challenge:** Solve the **Tower of Hanoi** problem recursively.
