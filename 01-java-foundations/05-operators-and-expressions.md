# Java Foundations — Lesson 5: Operators & Expressions

## What is an Operator?

An **operator** is a symbol that performs an operation on one or more **operands** (values):

```java
int sum = 5 + 3;
//         ↑   ↑
//       operand operator operand
```

## Arithmetic Operators

```java
int a = 10, b = 3;

int sum = a + b;      // 13  (addition)
int diff = a - b;     // 7   (subtraction)
int product = a * b;  // 30  (multiplication)
int quotient = a / b; // 3   (division — truncates for ints!)
int remainder = a % b;// 1   (modulo — remainder of division)
```

### Integer Division — A Common Pitfall

```java
int result = 10 / 3;   // = 3, NOT 3.333!
// Both operands are int → result is int (truncated)

double correct = 10.0 / 3;  // = 3.333... (one operand is double)
double alsoCorrect = (double) 10 / 3;  // cast makes it double
```

## Compound Assignment Operators

```java
int x = 10;
x += 5;   // x = x + 5  → 15
x -= 3;   // x = x - 3  → 12
x *= 2;   // x = x * 2  → 24
x /= 4;   // x = x / 4  → 6
x %= 5;   // x = x % 5  → 1
```

## Increment & Decrement

```java
int count = 5;

// POSTFIX: returns current value, THEN increments
int result = count++;   // result = 5, count = 6

// PREFIX: increments first, THEN returns new value
int result2 = ++count;  // count = 7, result2 = 7
```

```
count = 5

result = count++;   →   result = 5, count = 6
                 ↑
          "Use 5, then add 1"

result = ++count;   →   count = 6, result = 6
              ↑
       "Add 1, then use 6"
```

## Relational (Comparison) Operators

```java
int a = 10, b = 20;

boolean eq = (a == b);    // false (equal to)
boolean ne = (a != b);    // true  (not equal to)
boolean lt = (a < b);     // true  (less than)
boolean gt = (a > b);     // false (greater than)
boolean le = (a <= 10);   // true  (less than or equal)
boolean ge = (b >= 20);   // true  (greater than or equal)
```

## Logical Operators

```java
boolean sunny = true;
boolean warm = true;
boolean weekend = false;

// AND (&&) — ALL must be true
boolean goToBeach = sunny && warm;         // true
boolean goWithFriend = sunny && weekend;   // false

// OR (||) — AT LEAST ONE must be true
boolean stayHome = !sunny || !warm;        // false
boolean mightGo = sunny || weekend;        // true

// NOT (!) — inverts
boolean rain = !sunny;                     // false
```

### Short-Circuit Evaluation

Java evaluates `&&` and `||` **lazily** — it stops as soon as the result is known:

```java
// Short-circuit AND: if first is false, second is NEVER evaluated
String name = null;
boolean valid = name != null && name.length() > 0;  // SAFE: name.length() never called

// Short-circuit OR: if first is true, second is NEVER evaluated
boolean quick = true || expensiveOperation();  // expensiveOperation() NEVER called
```

### Bitwise Operators (Advanced)

**&, |, ^, ~, <<, >>, >>>** — operate on individual bits.

```java
int flags = 0b1100;   // 12 in binary
int mask = 0b1010;    // 10 in binary

int and = flags & mask;   // 0b1000 (8)  — both bits 1
int or  = flags | mask;   // 0b1110 (14) — either bit 1
int xor = flags ^ mask;   // 0b0110 (6)  — bits differ
int not = ~flags;         // ...11110011 (inverts all bits)

int left = flags << 2;    // 0b110000 (48) — shift left
int right = flags >> 2;   // 0b0011 (3) — shift right (sign preserved)
```

**Real-world use:** Permission flags, graphics, low-level protocols.

## The String Concatenation Operator (+)

```java
String greeting = "Hello, " + "World!";  // "Hello, World!"
String message = "Count: " + 42;          // "Count: 42" (int auto-converted)
String complex = "Result: " + (5 + 3);    // "Result: 8" (parentheses matter!)
```

**Gotcha:** `+` is left-associative for strings vs numbers:

```java
System.out.println(1 + 2 + " = 3");    // "3 = 3"  (1+2=3 first, then concat)
System.out.println("1 + 2 = " + 1 + 2); // "1 + 2 = 12" (concat takes over!)
System.out.println("1 + 2 = " + (1 + 2)); // "1 + 2 = 3" (parentheses fix it)
```

## Operator Precedence

```java
int result = 5 + 3 * 2;    // = 11 (multiplication first, THEN addition)
int withParens = (5 + 3) * 2;  // = 16 (parentheses override)
```

**PEMDAS-style precedence (highest to lowest):**

| Level | Operators | Associativity |
|-------|-----------|---------------|
| 1 (highest) | `()` `[]` `.` | left→right |
| 2 | `++` `--` `!` `~` `+` `-` (unary) | right→left |
| 3 | `*` `/` `%` | left→right |
| 4 | `+` `-` | left→right |
| 5 | `<<` `>>` `>>>` | left→right |
| 6 | `<` `>` `<=` `>=` `instanceof` | left→right |
| 7 | `==` `!=` | left→right |
| 8 | `&` | left→right |
| 9 | `^` | left→right |
| 10 | `|` | left→right |
| 11 | `&&` | left→right |
| 12 | `||` | left→right |
| 13 | `=` `+=` `-=` `*=` etc. (assignment) | right→left |

**Rule of thumb:** When in doubt, **use parentheses**. Code readability > showing off.

## Type Promotion in Expressions

Java automatically promotes types in expressions to avoid data loss:

```java
byte a = 10, b = 20;
int sum = a + b;  // bytes are PROMOTED to int before addition!

// Promotion rules:
// 1. If either operand is double → other promoted to double
// 2. Else if either is float → other promoted to float
// 3. Else if either is long → other promoted to long
// 4. Else → both promoted to int (even byte/short!)
```

```java
short s = 5;
short result = s + 10;  // COMPILE ERROR! s + 10 is int, can't assign to short
short fixed = (short)(s + 10);  // OK with explicit cast
```

## The Ternary Operator — `? :`

A compact if-else expression:

```java
int age = 20;
String status = age >= 18 ? "Adult" : "Minor";
//                    ^      ^          ^
//              condition   true       false
```

This is equivalent to:
```java
String status;
if (age >= 18) {
    status = "Adult";
} else {
    status = "Minor";
}
```

## `instanceof` Operator

Checks if an object is a specific type:

```java
String text = "hello";
boolean isString = text instanceof String;       // true
boolean isObject = text instanceof Object;       // true (everything extends Object)
boolean isInteger = text instanceof Integer;     // false

// Pattern matching with instanceof (Java 16+):
if (text instanceof String s) {
    System.out.println(s.toUpperCase());  // s is already cast!
}
```

---

### Exercises

1. Write a program that takes two integers and prints: sum, difference, product, quotient, remainder.
2. Demonstrate integer division truncation vs floating-point division.
3. Show the difference between `x++` and `++x` in a program.
4. Use short-circuit evaluation to safely check if a String is null AND has length > 0.
5. Write a temperature converter: Fahrenheit → Celsius. Use the formula: `C = (F - 32) * 5/9`. Why does `5/9` give 0? Fix it!
