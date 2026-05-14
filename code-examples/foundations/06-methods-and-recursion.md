# 06 — Methods & Recursion

Method declaration and parts, overloading, varargs, pass-by-value, recursion (factorial, fibonacci, Tower of Hanoi), tail recursion, and stack overflows.

## Method Declaration — The 6 Parts

```java
// MethodParts.java — anatomy of a method declaration
package com.example;

public class MethodParts {

    // ┌── Access modifier (public / protected / private / package-private)
    // │   ┌── Optional modifier (static, final, abstract, synchronized, native)
    // │   │   ┌── Return type (void, int, String, etc.)
    // │   │   │        ┌── Method name
    // │   │   │        │      ┌── Parameter list (zero or more)
    // │   │   │        │      │
    public static int add(int a, int b) {
        //                      │        │
        //                      └────────┘
        //                     Method body
        return a + b;   // return statement — must match return type
    }

    // No parameters, returns void
    public void sayHello() {
        System.out.println("Hello!");
    }

    // Private method — only accessible within this class
    private static String greeting(String name) {
        return "Hi, " + name + "!";
    }

    public static void main(String[] args) {
        System.out.println("add(3, 5) = " + add(3, 5));

        new MethodParts().sayHello();

        System.out.println(greeting("Alice"));
    }
}
```

## Method Overloading

```java
// OverloadingDemo.java — same name, different parameters
package com.example;

public class OverloadingDemo {

    // Multiple methods with the same name but different parameter lists
    static int max(int a, int b) {
        System.out.println("  → max(int, int)");
        return a > b ? a : b;
    }

    static double max(double a, double b) {
        System.out.println("  → max(double, double)");
        return a > b ? a : b;
    }

    static int max(int a, int b, int c) {
        System.out.println("  → max(int, int, int)");
        return max(max(a, b), c);
    }

    // Overloading with different types
    static String format(String value) {
        return "String: " + value;
    }

    static String format(int value) {
        return "int: " + value;
    }

    static String format(double value) {
        return "double: " + value;
    }

    public static void main(String[] args) {
        System.out.println("max(3, 7)        = " + max(3, 7));
        System.out.println("max(3.5, 2.1)    = " + max(3.5, 2.1));
        System.out.println("max(1, 9, 5)     = " + max(1, 9, 5));

        System.out.println(format(42));
        System.out.println(format(3.14));
        System.out.println(format("Java"));

        // Overload resolution: the compiler picks the most specific match
    }
}
```

## Varargs (`...`)

```java
// VarargsMethods.java — variable-length argument lists
package com.example;

public class VarargsMethods {

    // Varargs must be the LAST parameter
    static double average(String label, double... values) {
        if (values.length == 0) return 0;
        double sum = 0;
        for (double v : values) sum += v;
        double avg = sum / values.length;
        System.out.println(label + ": " + avg);
        return avg;
    }

    // Concatenate an arbitrary number of strings
    static String join(String delimiter, String... parts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (i > 0) sb.append(delimiter);
            sb.append(parts[i]);
        }
        return sb.toString();
    }

    // Overloading with varargs — be careful of ambiguity!
    static void print(int... nums) {
        System.out.println("int... called");
    }

    // static void print(int a, int b) { ... }   // more specific → wins if 2 args

    public static void main(String[] args) {
        average("Class A", 85, 90, 78, 92);
        average("Class B", 100);
        average("Empty", /* no args */);

        System.out.println(join("-", "2024", "12", "25"));

        // Varargs with array argument
        average("Array", new double[]{1.0, 2.0, 3.0});
    }
}
```

## Pass-by-Value Demonstration

```java
// PassByValueDemo.java — Java is ALWAYS pass-by-value
package com.example;

import java.util.Arrays;

class MutableObject {
    int value;
    MutableObject(int v) { this.value = v; }
}

public class PassByValueDemo {

    // Primitive parameter — changing it has NO effect on the caller
    static void modifyPrimitive(int x) {
        x = 999;   // only changes the local copy
    }

    // Reference parameter — changing THE REFERENCE has no effect on caller
    static void reassignReference(MutableObject obj) {
        obj = new MutableObject(777);   // local variable now points elsewhere
    }

    // Reference parameter — mutating THE OBJECT's state DOES affect the caller
    static void mutateObject(MutableObject obj) {
        obj.value = 500;   // modifies the same object the caller points to
    }

    // Array reference — modifying array contents affects caller
    static void modifyArray(int[] arr) {
        arr[0] = 42;   // changes the array that the caller sees
        arr = new int[]{1, 2, 3};  // reassignment has NO effect on caller
        arr[0] = 99;
    }

    public static void main(String[] args) {
        // ── Primitive ──
        int a = 10;
        modifyPrimitive(a);
        System.out.println("After modifyPrimitive: a = " + a);   // still 10

        // ── Object reference ──
        MutableObject obj = new MutableObject(100);
        reassignReference(obj);
        System.out.println("After reassignReference: obj.value = " + obj.value);  // still 100

        mutateObject(obj);
        System.out.println("After mutateObject: obj.value = " + obj.value);  // 500

        // ── Array ──
        int[] arr = {1, 2, 3};
        modifyArray(arr);
        System.out.println("After modifyArray: arr[0] = " + arr[0]);  // 42 (not 99!)
        // The reassignment inside modifyArray had no effect on the caller.
    }
}
```

## Recursion — Factorial, Fibonacci, Tower of Hanoi

```java
// RecursionDemo.java — classic recursive algorithms
package com.example;

public class RecursionDemo {

    // ── Factorial (n!) ──
    // Base case: 0! = 1
    // Recursive: n! = n * (n-1)!
    static long factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        if (n <= 1) return 1;                    // base case
        return n * factorial(n - 1);              // recursive case
    }

    // ── Fibonacci (naive recursive — inefficient) ──
    // Base cases: fib(0) = 0, fib(1) = 1
    static long fib(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        if (n <= 1) return n;                     // base case
        return fib(n - 1) + fib(n - 2);            // recursive case (exponential!)
    }

    // ── Fibonacci with memoization (top-down DP) ──
    static long fibMemo(int n, long[] memo) {
        if (n <= 1) return n;
        if (memo[n] != 0) return memo[n];          // already computed
        memo[n] = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
        return memo[n];
    }

    static long fibMemo(int n) {
        return fibMemo(n, new long[n + 1]);
    }

    // ── Tower of Hanoi ──
    // Move n disks from source to target using auxiliary peg
    static void hanoi(int n, char source, char target, char auxiliary) {
        if (n == 1) {
            System.out.println("  Move disk 1 from " + source + " → " + target);
            return;
        }
        hanoi(n - 1, source, auxiliary, target);
        System.out.println("  Move disk " + n + " from " + source + " → " + target);
        hanoi(n - 1, auxiliary, target, source);
    }

    public static void main(String[] args) {
        System.out.println("--- Factorial ---");
        for (int i = 0; i <= 10; i++) {
            System.out.println(i + "! = " + factorial(i));
        }

        System.out.println("\n--- Fibonacci ---");
        System.out.println("fib(10) = " + fib(10));          // 55
        System.out.println("fib(45) = " + fibMemo(45));      // 1,134,903,170 (fast)

        System.out.println("\n--- Tower of Hanoi (3 disks) ---");
        hanoi(3, 'A', 'C', 'B');
    }
}
```

## Tail Recursion vs Regular Recursion

```java
// TailRecursionDemo.java — comparing tail-recursive vs non-tail-recursive
package com.example;

public class TailRecursionDemo {

    // ── Regular recursion (NOT tail-recursive) ──
    // After the recursive call returns, we still have to multiply by n.
    // The JVM must keep the call stack frame alive to do that work.
    static long factorialRegular(int n) {
        if (n <= 1) return 1;
        return n * factorialRegular(n - 1);  // not in tail position (multiplication happens after)
    }

    // ── Tail-recursive factorial ──
    // The recursive call is the LAST operation — nothing happens after it.
    // Java does NOT optimize tail calls, but the structure is still instructive.
    static long factorialTail(int n) {
        return factorialTailHelper(n, 1);
    }

    private static long factorialTailHelper(int n, long accumulator) {
        if (n <= 1) return accumulator;
        // The recursive call IS in tail position — nothing happens after it.
        return factorialTailHelper(n - 1, n * accumulator);
    }

    // ── Visual difference ──
    // Regular:   fact(5) → 5 * fact(4) → 5 * (4 * fact(3)) → ...
    //   JVM stack must keep all pending frames.
    //
    // Tail:      factTail(5, 1) → factTail(4, 5) → factTail(3, 20) → ...
    //   Each call replaces the previous frame (IF tail-call optimized — Java doesn't do this).

    public static void main(String[] args) {
        System.out.println("factorialRegular(10) = " + factorialRegular(10));
        System.out.println("factorialTail(10)    = " + factorialTail(10));
    }
}
```

## Stack Overflow Demonstration

```java
// StackOverflowDemo.java — deliberately overflow the call stack
package com.example;

public class StackOverflowDemo {

    static int depth = 0;

    // Recursively call without a base case (or with too-deep recursion)
    static void infiniteRecursion() {
        depth++;
        infiniteRecursion();   // never returns → StackOverflowError
    }

    // Even with a base case, you can still overflow if N is too large
    static long factorialOverflow(int n) {
        if (n <= 1) return 1;
        return n * factorialOverflow(n - 1);
    }

    public static void main(String[] args) {
        // ── Test 1: infinite recursion ──
        try {
            infiniteRecursion();
        } catch (StackOverflowError e) {
            System.out.println("Stack overflow after " + depth + " recursive calls");
            // Typical depth is a few thousand (depends on JVM stack size)
        }

        // ── Test 2: factorial with N too large ──
        depth = 0;
        try {
            factorialOverflow(100_000);   // way too deep!
        } catch (StackOverflowError e) {
            System.out.println("Stack overflow in deep factorial");
        }

        // ── How to avoid: use iteration or increase stack size ──
        // Iterative factorial (no recursion, no overflow):
        long result = 1;
        for (int i = 2; i <= 20_000; i++) {
            result *= i;    // this will overflow long arithmetic, but won't stack overflow
        }
        System.out.println("Iterative product (will wrap): " + result);

        // To increase stack size at JVM startup:
        //   java -Xss10m StackOverflowDemo
        // This sets each thread's stack to 10 MB (default is ~1 MB on most systems).
    }
}
```
