# 02 — Variables & Types

All 8 primitives, the `var` keyword, type conversion, casting, overflow, wrapper classes, autoboxing, and default values.

## The 8 Primitive Types

```java
// Primitives.java — every primitive type in Java
package com.example;

public class Primitives {
    public static void main(String[] args) {
        // Integers
        byte   b = 127;              //  8-bit,  -128 to 127
        short  s = 32_767;           // 16-bit,  -32_768 to 32_767
        int    i = 2_147_483_647;    // 32-bit,  ≈ ±2.1 billion
        long   l = 9_223_372_036_854_775_807L; // 64-bit, L suffix required

        // Floating-point
        float  f = 3.141592f;        // 32-bit IEEE 754, f/F suffix required
        double d = 3.141592653589793; // 64-bit IEEE 754 (default for decimals)

        // Other
        char   c = 'A';              // 16-bit Unicode character (0–65_535)
        boolean bool = true;         // true or false

        System.out.printf("byte=%d, short=%d, int=%d, long=%d%n", b, s, i, l);
        System.out.printf("float=%.4f, double=%.15f%n", f, d);
        System.out.printf("char=%c, boolean=%b%n", c, bool);
    }
}
```

## The `var` Keyword (Local Variable Type Inference)

```java
// VarDemo.java — Java 10+ local variable type inference
package com.example;

import java.util.List;

public class VarDemo {
    public static void main(String[] args) {
        // var infers the compile-time type from the initializer
        var name = "Alice";              // inferred as String
        var count = 42;                  // inferred as int
        var pi = 3.14159;                // inferred as double
        var list = List.of(1, 2, 3);     // inferred as List<Integer>

        // Useful for complex generic types
        var map = java.util.Map.of("key", new int[]{1, 2, 3});

        System.out.println(name + " " + count + " " + pi);
        System.out.println(list);
        System.out.println(map);

        // Restrictions:
        //   var x;             // ❌ must have initializer
        //   var y = null;      // ❌ cannot infer null
        //   var z = (Runnable) () -> {}; // ✅ with explicit cast
        //   var is NOT allowed for fields, method params, or return types
    }
}
```

## Widening (Implicit) vs Narrowing (Explicit) Casting

```java
// CastingDemo.java — implicit and explicit type conversion
package com.example;

public class CastingDemo {
    public static void main(String[] args) {
        // ── Widening (implicit): smaller → larger, no data loss ──
        int    i = 100;
        long   l = i;          // int → long: automatic
        float  f = l;          // long → float: automatic (possible precision loss)
        double d = f;          // float → double: automatic

        System.out.printf("Widening: %d → %d → %f → %f%n", i, l, f, d);

        // ── Narrowing (explicit): larger → smaller, may lose data ──
        double pi = 3.141592653589793;
        float  piFloat = (float) pi;     // loses precision
        int    piInt   = (int) pi;       // truncates fractional part → 3
        byte   piByte  = (byte) piInt;   // 3 fits in byte, fine

        System.out.printf("Narrowing: %f → %f → %d → %d%n",
                          pi, piFloat, piInt, piByte);

        // Overflow with narrowing
        int big = 300;
        byte small = (byte) big;         // 300 - 256 = 44 (overflow!)
        System.out.printf("(byte)300 = %d (overflow)%n", small);
    }
}
```

## Integer Overflow

```java
// OverflowDemo.java — what happens when you exceed the range
package com.example;

public class OverflowDemo {
    public static void main(String[] args) {
        // Max int value
        int max = Integer.MAX_VALUE;   // 2_147_483_647
        System.out.println("Max int:        " + max);
        System.out.println("Max int + 1:    " + (max + 1));   // wraps to -2_147_483_648
        System.out.println("Max int + 2:    " + (max + 2));   // -2_147_483_647

        // Real-world: the classic overflow bug
        int total = 1_500_000_000;
        int count = 2_000_000_000;
        int avg   = (total + count) / 2;   // overflows!  total+count exceeds int range
        System.out.println("Buggy average:  " + avg);

        // Fix: use long
        long correctAvg = ((long) total + count) / 2;
        System.out.println("Correct average:" + correctAvg);

        // Wraparound in a byte
        byte b = 127;
        b++;       // 127 → -128
        System.out.println("byte 127++ = " + b);
    }
}
```

## Wrapper Classes & Autoboxing / Unboxing

```java
// AutoboxingDemo.java — primitives ↔ wrapper objects automatically
package com.example;

public class AutoboxingDemo {
    public static void main(String[] args) {
        // ── Autoboxing: primitive → wrapper (automatic) ──
        Integer wrapped = 42;               // int → Integer
        Long    longW   = 123L;             // long → Long
        Double  dbl     = 3.14;             // double → Double
        Boolean bool    = true;             // boolean → Boolean

        // ── Unboxing: wrapper → primitive (automatic) ──
        int primitive = wrapped;            // Integer → int
        double d      = dbl + 1.0;          // Double → double in expression

        System.out.println("Autoboxed: " + wrapped + " " + longW);
        System.out.println("Unboxed:   " + primitive + " " + d);

        // ── Common utility methods on wrapper classes ──
        System.out.println("Max int:  " + Integer.MAX_VALUE);
        System.out.println("Min int:  " + Integer.MIN_VALUE);
        System.out.println("Bits in int: " + Integer.SIZE);

        int parsed = Integer.parseInt("255");        // String → int
        System.out.println("Parsed:   " + parsed);

        String bin = Integer.toBinaryString(255);    // int → binary string
        System.out.println("255 in binary: " + bin);

        // ── null danger with unboxing ──
        Integer nullable = null;
        // int n = nullable;   // ❌ NullPointerException at runtime!
    }
}
```

## Default Values (for fields, NOT locals)

```java
// DefaultValues.java — JVM assigns defaults to fields, not local variables
package com.example;

public class DefaultValues {
    // Instance fields — always get a default value
    byte    b;
    short   s;
    int     i;
    long    l;
    float   f;
    double  d;
    char    c;
    boolean bool;
    String  str;    // reference type → null

    public void printDefaults() {
        System.out.println("byte    → " + b);     // 0
        System.out.println("short   → " + s);     // 0
        System.out.println("int     → " + i);     // 0
        System.out.println("long    → " + l);     // 0
        System.out.println("float   → " + f);     // 0.0
        System.out.println("double  → " + d);     // 0.0
        System.out.println("char    → '" + c + "' (unicode 0)");
        System.out.println("boolean → " + bool);  // false
        System.out.println("String  → " + str);   // null
    }

    public static void main(String[] args) {
        new DefaultValues().printDefaults();

        // Local variables MUST be initialized before use:
        // int x;           // OK to declare
        // System.out.println(x);  // ❌ compiler error — might not be initialized
    }
}
```

## Type Summary Table

| Type    | Size    | Range                          | Default  | Wrapper   |
|---------|---------|--------------------------------|----------|-----------|
| byte    | 8 bit   | -128 … 127                     | 0        | Byte      |
| short   | 16 bit  | -32_768 … 32_767               | 0        | Short     |
| int     | 32 bit  | -2^31 … 2^31-1                 | 0        | Integer   |
| long    | 64 bit  | -2^63 … 2^63-1                 | 0L       | Long      |
| float   | 32 bit  | ±3.4E-38 … ±3.4E+38            | 0.0f     | Float     |
| double  | 64 bit  | ±1.7E-308 … ±1.7E+308          | 0.0d     | Double    |
| char    | 16 bit  | 0 … 65_535 (Unicode)           | '\u0000' | Character |
| boolean | ~1 bit  | true / false                   | false    | Boolean   |
