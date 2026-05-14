# 03 — Operators & Expressions

Arithmetic, relational, logical, bitwise, ternary, `instanceof`, and operator precedence.

## Arithmetic Operators

```java
// ArithmeticDemo.java — + - * / % on integers and floats
package com.example;

public class ArithmeticDemo {
    public static void main(String[] args) {
        int a = 17, b = 5;

        System.out.println("a = " + a + ", b = " + b);
        System.out.println("a + b  = " + (a + b));   // 22
        System.out.println("a - b  = " + (a - b));   // 12
        System.out.println("a * b  = " + (a * b));   // 85
        System.out.println("a / b  = " + (a / b));   // 3  (integer division truncates)
        System.out.println("a % b  = " + (a % b));   // 2  (modulo / remainder)

        // Integer division with doubles
        double x = 17.0, y = 5.0;
        System.out.println("x / y  = " + (x / y));   // 3.4

        // Unary operators
        int c = 10;
        System.out.println("++c    = " + (++c));     // 11 (pre-increment)
        System.out.println("c++    = " + (c++));     // 11 (post-increment, then c=12)
        System.out.println("--c    = " + (--c));     // 11
        System.out.println("c--    = " + (c--));     // 11 (post-decrement, then c=10)
        System.out.println("-c     = " + (-c));      // -10
        System.out.println("+c     = " + (+c));      // 10 (no-op, included for symmetry)
    }
}
```

## Relational / Comparison Operators

```java
// RelationalDemo.java — comparing values, result is always boolean
package com.example;

public class RelationalDemo {
    public static void main(String[] args) {
        int a = 10, b = 20;

        System.out.println("a = " + a + ", b = " + b);
        System.out.println("a == b : " + (a == b));   // false
        System.out.println("a != b : " + (a != b));   // true
        System.out.println("a <  b : " + (a < b));    // true
        System.out.println("a >  b : " + (a > b));    // false
        System.out.println("a <= b : " + (a <= b));   // true
        System.out.println("a >= b : " + (a >= b));   // false

        // Comparing floating-point — beware of precision!
        double d1 = 0.1 + 0.2;
        double d2 = 0.3;
        System.out.println("0.1+0.2 == 0.3? " + (d1 == d2));  // false!
        System.out.println("d1 = " + d1 + ", d2 = " + d2);    // 0.30000000000000004
        // Use a tolerance for float comparisons
        double epsilon = 1e-10;
        System.out.println("Math.abs(d1-d2) < eps? " + (Math.abs(d1 - d2) < epsilon));
    }
}
```

## Logical Operators — Short-Circuit vs Non-Short-Circuit

```java
// LogicalDemo.java — && || !  vs  & | (bitwise used as logical)
package com.example;

public class LogicalDemo {
    static boolean falseSideEffect() {
        System.out.println("   falseSideEffect() called");
        return false;
    }
    static boolean trueSideEffect() {
        System.out.println("   trueSideEffect() called");
        return true;
    }

    public static void main(String[] args) {
        boolean a = true, b = false;

        // Standard logical operators
        System.out.println("a && b : " + (a && b));   // false
        System.out.println("a || b : " + (a || b));   // true
        System.out.println("!a     : " + (!a));       // false
        System.out.println("!b     : " + (!b));       // true

        // ── Short-circuit (&&, ||) ──
        // If the left side determines the result, the right side is NEVER evaluated.
        System.out.println("\nShort-circuit &&:");
        System.out.println("   Result: " + (falseSideEffect() && trueSideEffect()));
        // falseSideEffect printed, but trueSideEffect was NOT called

        System.out.println("\nShort-circuit ||:");
        System.out.println("   Result: " + (trueSideEffect() || falseSideEffect()));
        // trueSideEffect printed, falseSideEffect was NOT called

        // ── Non-short-circuit (&, |) ──
        // BOTH sides are ALWAYS evaluated, even if the result is already known.
        System.out.println("\nNon-short-circuit &:");
        System.out.println("   Result: " + (falseSideEffect() & trueSideEffect()));
        // BOTH methods are called

        System.out.println("\nNon-short-circuit |:");
        System.out.println("   Result: " + (trueSideEffect() | falseSideEffect()));
        // BOTH methods are called

        // XOR (no short-circuit version)
        System.out.println("\na ^ b : " + (a ^ b));   // true (different → true)
        System.out.println("a ^ a : " + (a ^ a));     // false (same → false)
    }
}
```

## Bitwise Operators

```java
// BitwiseDemo.java — & | ^ ~ << >> >>>
package com.example;

public class BitwiseDemo {
    public static void main(String[] args) {
        int a = 0b1100;   // 12 in binary
        int b = 0b1010;   // 10 in binary

        System.out.println("a       = " + toPaddedBin(a) + "  (" + a + ")");
        System.out.println("b       = " + toPaddedBin(b) + "  (" + b + ")");
        System.out.println("a & b   = " + toPaddedBin(a & b) + "  (" + (a & b) + ")");
        // AND: 1 only where both are 1
        System.out.println("a | b   = " + toPaddedBin(a | b) + "  (" + (a | b) + ")");
        // OR:  1 where at least one is 1
        System.out.println("a ^ b   = " + toPaddedBin(a ^ b) + "  (" + (a ^ b) + ")");
        // XOR: 1 where bits differ
        System.out.println("~a      = " + toPaddedBin(~a) + "  (" + (~a) + ")");
        // NOT: flip all bits (including sign)

        // Shift operators
        int c = 0b0001_0000;   // 16
        System.out.println("\nc       = " + toPaddedBin(c) + "  (" + c + ")");
        System.out.println("c << 2  = " + toPaddedBin(c << 2) + "  (" + (c << 2) + ")");
        // Left shift: multiply by 2^2 = 64
        System.out.println("c >> 2  = " + toPaddedBin(c >> 2) + "  (" + (c >> 2) + ")");
        // Right shift (sign-extending): divide by 2^2 = 4
        System.out.println("c >>> 2 = " + toPaddedBin(c >>> 2) + "  (" + (c >>> 2) + ")");
        // Unsigned right shift: always fills with 0 (positive result)

        // Negative number shifts
        int neg = -16;
        System.out.println("\nneg     = " + Integer.toBinaryString(neg) + "  (" + neg + ")");
        System.out.println("neg >> 2  = " + Integer.toBinaryString(neg >> 2) + "  (" + (neg >> 2) + ")");
        // Signed right shift keeps the sign bit (fills with 1s for negatives)
        System.out.println("neg >>> 2 = " + Integer.toBinaryString(neg >>> 2) + "  (" + (neg >>> 2) + ")");
        // Unsigned right shift fills with 0 → large positive
    }

    static String toPaddedBin(int n) {
        return String.format("%8s", Integer.toBinaryString(n)).replace(' ', '0');
    }
}
```

## Ternary Operator (`? :`)

```java
// TernaryDemo.java — compact if-else expressions, including nested
package com.example;

public class TernaryDemo {
    public static void main(String[] args) {
        int score = 85;

        // Basic ternary: condition ? valueIfTrue : valueIfFalse
        String grade = score >= 60 ? "Pass" : "Fail";
        System.out.println("Score " + score + " → " + grade);

        // Nested ternary: use parentheses for clarity
        String letter;
        letter = score >= 90 ? "A"
               : score >= 80 ? "B"
               : score >= 70 ? "C"
               : score >= 60 ? "D"
               : "F";
        System.out.println("Letter grade: " + letter);

        // Ternary for side effects (works, but an if-else is usually clearer)
        int x = 10, y = 20;
        int max = (x > y) ? x : y;
        System.out.println("Max of " + x + " and " + y + " is " + max);

        // Ternary with method calls — both branches must be expressions
        String result = (x % 2 == 0) ? "even" : "odd";
        System.out.println(x + " is " + result);
    }
}
```

## `instanceof` Operator

```java
// InstanceofDemo.java — checks whether an object is an instance of a given type
package com.example;

sealed interface Animal permits Dog, Cat {}
final class Dog implements Animal {}
final class Cat implements Animal {}

public class InstanceofDemo {
    public static void main(String[] args) {
        Animal pet = new Dog();

        // Traditional instanceof
        if (pet instanceof Dog) {
            System.out.println("pet is a Dog");
        }

        // Pattern matching for instanceof — Java 16+
        // Declares a variable directly in the condition
        if (pet instanceof Dog d) {
            System.out.println("pet is a Dog, name it " + d.getClass().getSimpleName());
        }

        // Works with any type
        Object obj = "Hello, Java!";
        if (obj instanceof String s && s.length() > 5) {
            System.out.println("Long string: " + s.toUpperCase());
        }

        // null is NEVER an instanceof anything
        Object nothing = null;
        System.out.println("null instanceof String? " + (nothing instanceof String));  // false
    }
}
```

## Operator Precedence

```java
// PrecedenceDemo.java — demonstrates that precedence matters!
package com.example;

public class PrecedenceDemo {
    public static void main(String[] args) {
        // Without parentheses, the expression follows Java's precedence table.
        // From highest to lowest (simplified):
        //   () [] .        → grouping, array index, member access
        //   ++ -- + - ~ !  → unary
        //   * / %          → multiplicative
        //   + -            → additive
        //   << >> >>>      → shift
        //   < <= > >= instanceof
        //   == !=
        //   &
        //   ^
        //   |
        //   &&
        //   ||
        //   ?:             → ternary
        //   = += -= etc.   → assignment

        int result;

        // Example 1: multiplication before addition
        result = 10 + 2 * 5;          // 10 + (2*5) = 20, NOT (10+2)*5 = 60
        System.out.println("10 + 2 * 5   = " + result);

        // Example 2: use parens to override
        result = (10 + 2) * 5;         // 60
        System.out.println("(10 + 2) * 5 = " + result);

        // Example 3: mixed operators
        boolean boolResult = 3 + 4 * 2 > 10 && (5 + 1) % 3 == 0;
        // Step-by-step:
        //   4 * 2 = 8
        //   3 + 8 = 11
        //   5 + 1 = 6
        //   6 % 3 = 0
        //   11 > 10 → true
        //   0 == 0 → true
        //   true && true → true
        System.out.println("Complex expression: " + boolResult);

        // Example 4: assignment is right-associative
        int a, b, c;
        a = b = c = 5;   // means a = (b = (c = 5))
        System.out.println("a=" + a + " b=" + b + " c=" + c);
    }
}
```

## Operator Precedence Table (high → low)

| Level | Operators                           | Associativity |
|-------|--------------------------------------|---------------|
| 1     | `()` `[]` `.`                       | left→right    |
| 2     | `++` `--` `+` `-` `~` `!` (unary)  | right→left    |
| 3     | `*` `/` `%`                         | left→right    |
| 4     | `+` `-`                             | left→right    |
| 5     | `<<` `>>` `>>>`                     | left→right    |
| 6     | `<` `<=` `>` `>=` `instanceof`      | left→right    |
| 7     | `==` `!=`                           | left→right    |
| 8     | `&`                                 | left→right    |
| 9     | `^`                                 | left→right    |
| 10    | `|`                                 | left→right    |
| 11    | `&&`                                | left→right    |
| 12    | `||`                                | left→right    |
| 13    | `?:`                                | right→left    |
| 14    | `=` `+=` `-=` etc.                  | right→left    |
