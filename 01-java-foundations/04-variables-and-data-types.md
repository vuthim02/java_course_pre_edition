# Java Foundations — Lesson 4: Variables & Data Types

## Variables: The Named Boxes

Think of a variable as a **labeled box**:
- The **label** = variable name
- The **box size/shape** = data type (what kind of thing fits)
- The **contents** = the value

```java
int age = 25;
//  ↑    ↑    ↑
// type name value
```

## Primitive Data Types — The 8 Basics

Java has exactly 8 primitive types. Everything else is an object.

```
┌─────────────────────────────────────────────────────────────┐
│               THE 8 PRIMITIVE TYPES                         │
├───────┬──────────┬──────────┬───────────────────────────────┤
│ TYPE  │  SIZE    │  DEFAULT │           RANGE               │
├───────┼──────────┼──────────┼───────────────────────────────┤
│ byte  │  1 byte  │    0     │ -128 to 127                   │
│ short │  2 bytes │    0     │ -32,768 to 32,767             │
│ int   │  4 bytes │    0     │ -2^31 to 2^31-1 (~±2.1B)    │
│ long  │  8 bytes │    0L    │ -2^63 to 2^63-1              │
│ float │  4 bytes │   0.0f   │ ±3.4E-38 to ±3.4E+38         │
│ double│  8 bytes │   0.0d   │ ±1.7E-308 to ±1.7E+308       │
│ char  │  2 bytes │ '\u0000' │ 0 to 65,535 (Unicode)        │
│ boolean│ 1 bit*    │  false  │ true or false                │
└───────┴──────────┴──────────┴───────────────────────────────┘
*boolean size is JVM-dependent, typically 1 byte
```

### Integer Types — Visual Guide

```
Memory Layout:

byte (1 byte)     [  ▒▒▒▒▒▒▒▒  ] → -128 to 127

short (2 bytes)   [  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ] → -32,768 to 32,767

int (4 bytes)     [  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ] → -2.1B to 2.1B

long (8 bytes)    [  ▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒  ]
                  → truly huge numbers
```

**When to use what:**
- **int** — 99% of the time for whole numbers
- **long** — when numbers exceed 2 billion, or timestamps
- **byte/short** — saving memory in large arrays (rare)
- **double** — 99% of decimal numbers
- **float** — saving memory in massive arrays (games, graphics)
- **char** — a single Unicode character (rare; String is usually better)
- **boolean** — true/false flags

## Variable Declaration & Initialization

```java
// Declaration (creates the box — empty)
int age;

// Initialization (puts something in the box)
age = 25;

// Declaration + Initialization (one step)
int score = 100;

// Multiple declarations
int x, y, z;

// Multiple with initialization
int a = 1, b = 2, c = 3;
```

## Naming Conventions (Follow These!)

```
✅ GOOD
int numberOfStudents;
double taxRate;
String firstName;
boolean isActive;

❌ BAD
int NumberOfStudents;    // Should start lowercase
double taxrate;          // Missing camelCase
String first_name;       // Snake_case not Java style
int 123abc;              // Can't start with digit
int class;               // Reserved keyword!
```

**Rules:**
- Must start with a letter, `$`, or `_`
- After first character: letters, digits, `$`, `_`
- Case-sensitive: `age` ≠ `Age`
- Can't use **reserved keywords**: `class`, `public`, `static`, `void`, `if`, `else`, `for`, `while`, etc.
- **camelCase** by convention

## Type Casting

### Widening (Implicit — automatic, safe)

```java
int myInt = 100;
long myLong = myInt;      // int → long (automatic, no data loss)
double myDouble = myInt;  // int → double (automatic)
```

```
int [   100    ] → long [    100     ]  (just adds zeros)
int [   100    ] → double [ 100.0     ]  (adds decimal)
```

### Narrowing (Explicit — you must cast, possible data loss)

```java
double myDouble = 100.75;
int myInt = (int) myDouble;   // (int) is the CAST — 100.75 → 100 (truncated!)
// myInt is now 100 — the .75 is GONE

long bigNumber = 1_000_000_000_000L;
int smaller = (int) bigNumber;  // Data may be truncated!
```

```
double [ 100.75 ] → (int) → int [ 100 ]  ← DECIMAL LOST!

long [ 1,000,000,000,000 ] → (int) → int [ ??? ]  ← DATA LOST (overflow!)
```

### Overflow — The Silent Killer

```java
int max = Integer.MAX_VALUE;  // 2,147,483,647
int overflow = max + 1;       // -2,147,483,648 (wraps around!)
```

```
    2,147,483,647
    +          1
    ──────────────
    -2,147,483,648  ← WRAPS around like an odometer
```

**Never assume arithmetic won't overflow! Use `Math.addExact()` for safety:**
```java
try {
    int safe = Math.addExact(max, 1);  // Throws ArithmeticException
} catch (ArithmeticException e) {
    System.err.println("Overflow!");
}
```

## Literals — Writing Values in Code

```java
// Integer literals
int dec = 42;              // Decimal (base 10)
int hex = 0x2A;            // Hexadecimal (base 16)
int bin = 0b101010;        // Binary (base 2)
int oct = 052;             // Octal (base 8) — avoid, confusing!

// Underscores for readability (Java 7+)
int million = 1_000_000;
long creditCard = 1234_5678_9012_3456L;
double pi = 3.141_592_653;

// Floating point
float f = 3.14f;           // f/F suffix required for float
double d = 3.14;           // double is the default
double scientific = 1.5e10; // 1.5 × 10^10

// Char literals
char letter = 'A';
char unicode = '\u0041';   // 'A' in Unicode
char tab = '\t';           // Escape sequence
char newline = '\n';

// Boolean
boolean isJavaFun = true;
boolean isHard = false;
```

## Default Values

In Java, **local variables** have NO default — must initialize before use:

```java
public void method() {
    int x;          // NO default value
    System.out.println(x);  // COMPILE ERROR: variable might not have been initialized
    x = 5;          // Must initialize first
}
```

But **class fields** DO have defaults:

```java
public class Person {
    int age;        // defaults to 0
    double salary;  // defaults to 0.0
    boolean active; // defaults to false
    String name;    // defaults to null (objects)
}
```

## Wrapper Classes — Primitives as Objects

Each primitive has a corresponding **wrapper class**:

| Primitive | Wrapper | Example |
|-----------|---------|---------|
| `byte` | `Byte` | `Byte b = 5;` |
| `short` | `Short` | `Short s = 100;` |
| `int` | `Integer` | `Integer i = 42;` |
| `long` | `Long` | `Long l = 1L;` |
| `float` | `Float` | `Float f = 3.14f;` |
| `double` | `Double` | `Double d = 3.14;` |
| `char` | `Character` | `Character c = 'A';` |
| `boolean` | `Boolean` | `Boolean b = true;` |

### Autoboxing & Unboxing

```java
// Autoboxing: int → Integer automatically
Integer boxed = 42;  // Java auto-creates Integer.valueOf(42)

// Unboxing: Integer → int automatically
int unboxed = boxed; // Java auto-calls boxed.intValue()

// In collections:
List<Integer> numbers = new ArrayList<>();
numbers.add(5);      // Autoboxing: int → Integer
int value = numbers.get(0);  // Unboxing: Integer → int
```

---

### Exercises

1. Declare variables for: your name, age, height, student status, and favorite letter. Print them all.
2. Create an `int` with value `Integer.MAX_VALUE`. Add 1 to it. What happens? Now use `Math.addExact()`.
3. Convert a `double` to `int` using a cast. What happens to the decimal part?
4. Write a program that uses binary (`0b`), hex (`0x`), and underscore literals. Print the results.
5. Experiment with overflow: `int x = 2_000_000_000; int y = x * 2;` What is the result?
