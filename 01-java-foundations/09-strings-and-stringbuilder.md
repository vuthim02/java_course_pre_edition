# Java Foundations — Lesson 9: Strings & StringBuilder

## Strings: The Most Important Class

`String` is not a primitive — it's a **class** (object type). But Java gives it special treatment because strings are used everywhere.

```java
String greeting = "Hello";  // String literal (special syntax)
String name = new String("Alice");  // Also works, but DON'T do this
```

## String Immutability

**Strings are IMMUTABLE** — once created, they can NEVER be changed:

```java
String s = "Hello";
s.toUpperCase();    // Returns a NEW string "HELLO", original unchanged
System.out.println(s);  // Still "Hello"!

// To "change" a string, you must reassign:
s = s.toUpperCase();  // Now s = "HELLO" (old "Hello" can be garbage collected)
```

```
Memory:
Before:        After:
s ──▶ "Hello"   s ──▶ "HELLO" (new object)
                     "Hello" (old, waiting for GC)
```

**Why immutable?**
1. **Thread safety** — can be shared across threads safely
2. **String pool** — can reuse identical strings
3. **Security** — can't modify strings used for passwords, class names
4. **Caching** — hash code can be cached (improves HashMap performance)

## The String Pool

Java optimizes memory by storing string literals in a **pool**:

```java
String a = "Hello";
String b = "Hello";
String c = new String("Hello");

System.out.println(a == b);       // TRUE  — same object in pool
System.out.println(a == c);       // FALSE — c is a NEW object on heap
System.out.println(a.equals(c));  // TRUE  — same value (ALWAYS use equals()!)
```

```
String Pool (in heap):     Heap (outside pool):
┌──────────────────┐      ┌──────────────────┐
│     "Hello"      │      │   "Hello" (new)  │
│       ↑  ↑       │      │       ↑          │
│       a  b       │      │       c          │
└──────────────────┘      └──────────────────┘
```

**Always compare strings with `.equals()`, NOT `==`!**

## Essential String Methods

```java
String s = "  Hello, World!  ";

// Length
s.length();                 // 17 (including spaces)

// Character access
s.charAt(0);                // ' ' (space at index 0)
s.charAt(7);                // 'W'

// Searching
s.indexOf('W');            // 8
s.indexOf("World");        // 8
s.lastIndexOf('o');        // 9
s.contains("Hello");       // true
s.startsWith("  He");      // true
s.endsWith("!  ");         // true

// Extracting
s.substring(2, 7);         // "Hello" (start=2, end=7 — exclusive)
s.substring(8);            // "World!  " (from index 8 to end)

// Case conversion
s.toUpperCase();            // "  HELLO, WORLD!  "
s.toLowerCase();            // "  hello, world!  "

// Trimming
s.trim();                   // "Hello, World!" (removes leading/trailing spaces)
s.strip();                  // "Hello, World!" (Java 11+ — handles Unicode)
s.stripLeading();           // "Hello, World!  " (removes leading only)
s.stripTrailing();          // "  Hello, World!" (removes trailing only)

// Replacement
s.replace('o', '0');        // "  Hell0, W0rld!  "
s.replace("World", "Java"); // "  Hello, Java!  "
s.replaceAll("\\s+", "");  // "Hello,World!" (regex — removes all spaces)

// Splitting
String[] parts = s.trim().split(", ");
// parts[0] = "Hello"
// parts[1] = "World!"

// Joining (Java 8+)
String joined = String.join("-", "2024", "12", "25");
// "2024-12-25"

// Formatting
String formatted = String.format("Name: %s, Age: %d", "Alice", 30);
// "Name: Alice, Age: 30"

// Conversion
String.valueOf(42);             // "42"
String.valueOf(3.14);           // "3.14"
String.valueOf(true);           // "true"
Integer.parseInt("42");         // 42 (int)
Double.parseDouble("3.14");     // 3.14
Boolean.parseBoolean("true");   // true

// Checking
s.isEmpty();       // false
"".isEmpty();      // true
s.isBlank();       // false (Java 11+ — also checks whitespace)
"   ".isBlank();   // true

// Equality
"Hello".equals("hello");           // false (case-sensitive)
"Hello".equalsIgnoreCase("hello"); // true

// Comparison
"apple".compareTo("banana");    // negative (apple < banana)
"banana".compareTo("apple");    // positive
"apple".compareTo("apple");     // 0
```

## StringBuilder — When You Need Mutable Strings

For heavy string manipulation, `StringBuilder` is MUCH faster than `String`:

```java
// SLOW: creates 1000 intermediate String objects
String result = "";
for (int i = 0; i < 1000; i++) {
    result += i + ",";  // Each += creates a NEW String!
}

// FAST: single StringBuilder, no intermediate objects
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(i).append(",");
}
String fastResult = sb.toString();  // Convert to String when done
```

### Why StringBuilder is faster:

```
String concatenation (+=):
"a" + "b" → "ab" + "c" → "abc" + "d" → "abcd"
 Creates 4 String objects!

StringBuilder:
sb.append("a").append("b").append("c").append("d")
 Modifies ONE mutable buffer — creates 1 String at the end
```

### StringBuilder Methods

```java
StringBuilder sb = new StringBuilder();

// Append (add to end)
sb.append("Hello");
sb.append(' ');
sb.append("World");

// Insert
sb.insert(5, ",");       // "Hello, World"

// Replace
sb.replace(7, 12, "Java");  // "Hello, Java"

// Delete
sb.delete(5, 6);          // "Hello Java" (removes the comma)

// Reverse
sb.reverse();              // "avaJ olleH"

// Convert to String
String result = sb.toString();

// Chaining (most methods return the StringBuilder)
sb.append("!")
  .insert(0, "Hi! ")
  .reverse();

// Capacity
StringBuilder sb2 = new StringBuilder(10000); // Pre-allocate capacity for performance
```

### StringBuilder vs StringBuffer

| Feature | StringBuilder | StringBuffer |
|---------|---------------|--------------|
| **Thread safety** | NOT thread-safe | Thread-safe (synchronized) |
| **Speed** | Faster | Slower (locking overhead) |
| **Use when** | Single-threaded | Multiple threads access same buffer |

**Default choice:** Always use `StringBuilder` unless sharing across threads.

## Text Blocks (Java 13+, Preview → Final in Java 15)

Multi-line strings without escape sequences:

```java
// OLD WAY:
String json = "{\n" +
              "  \"name\": \"Alice\",\n" +
              "  \"age\": 30,\n" +
              "  \"city\": \"New York\"\n" +
              "}";

// NEW WAY (Text Block):
String json = """
    {
      "name": "Alice",
      "age": 30,
      "city": "New York"
    }
    """;

// HTML:
String html = """
    <html>
        <body>
            <h1>Hello, World!</h1>
        </body>
    </html>
    """;
```

## String Templates (Java 21+ Preview → Java 22+)

```java
String name = "Alice";
int age = 30;

// Old way:
String message = "Hello, " + name + "! You are " + age + " years old.";

// With String templates (preview):
String message = STR."Hello, \{name}! You are \{age} years old.";
```

## Common String Pitfalls

### 1. Comparing with `==`

```java
String a = "hello";
String b = new String("hello");
System.out.println(a == b);  // FALSE! Never use == with strings!
```

### 2. Concatenation in loops

```java
// BAD: O(n²) time!
String s = "";
for (int i = 0; i < 10000; i++) {
    s += i;  // Creates 10000 String objects!
}

// GOOD: O(n) time!
StringBuilder sb = new StringBuilder(50000);
for (int i = 0; i < 10000; i++) {
    sb.append(i);
}
```

### 3. Forgetting trim()

```java
String input = getUserInput();  // Might be "  Alice  "
if (input.equals("Alice")) {    // FALSE because of spaces!
    // ...
}
if (input.trim().equals("Alice")) {  // TRUE
    // ...
}
```

---

### Exercises

1. Write a method `reverse(String s)` that returns the reversed string WITHOUT using StringBuilder.reverse().
2. Write a method `countVowels(String s)` that counts vowels (a, e, i, o, u).
3. Write a method `isPalindrome(String s)` that checks if a string reads the same forward/backward (ignoring case, spaces, and punctuation).
4. Use StringBuilder to build a multiplication table string (1-10) and print it.
5. Write a program that converts "snake_case" to "camelCase".
