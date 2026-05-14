# 07 — Strings

`String`, `StringBuilder`, the String pool, text blocks, and all the essential methods.

## String Immutability

```java
// ImmutabilityDemo.java — once created, a String cannot change
package com.example;

public class ImmutabilityDemo {
    public static void main(String[] args) {
        String s = "Hello";
        System.out.println("Original: " + s + "   (identity: " + System.identityHashCode(s) + ")");

        // toUpperCase() returns a NEW string; the original is unchanged
        String upper = s.toUpperCase();
        System.out.println("After toUpperCase(): " + s + " (still lowercase!)");
        System.out.println("Returned string: " + upper);

        // concat also returns a new string
        String s2 = s.concat(" World");
        System.out.println("After concat: " + s + " (unchanged)");
        System.out.println("Returned: " + s2);

        // Reassignment just points the reference elsewhere
        s = s + "!";
        System.out.println("After reassignment: " + s + "   (new identity: " + System.identityHashCode(s) + ")");

        // Why immutability matters:
        //   - Thread safety (no concurrent modification)
        //   - String pool can safely share references
        //   - Security (passwords, class names, etc.)
        //   - Hashcode caching (String caches its hashCode after first computation)
    }
}
```

## String Comparison: `==` vs `equals()`

```java
// StringComparison.java — the critical difference
package com.example;

public class StringComparison {
    public static void main(String[] args) {
        String a = "hello";                       // string literal → String pool
        String b = "hello";                       // same literal → same pool reference
        String c = new String("hello");           // forced new object on heap
        String d = "he" + "llo";                  // compile-time constant → pool
        String e = ("hel" + "lo").intern();        // explicitly interned

        System.out.println("a = " + a);
        System.out.println("b = " + b);
        System.out.println("c = " + c);
        System.out.println("d = " + d);
        System.out.println("e = " + e);

        // == compares REFERENCE identity (are they the SAME object?)
        System.out.println("\n--- == (reference equality) ---");
        System.out.println("a == b: " + (a == b));   // true  (same pool literal)
        System.out.println("a == c: " + (a == c));   // false (pool vs new heap object)
        System.out.println("a == d: " + (a == d));   // true  (compile-time constant)
        System.out.println("a == e: " + (a == e));   // true  (interned)

        // equals() compares VALUE (character by character)
        System.out.println("\n--- equals() (value equality) ---");
        System.out.println("a.equals(b): " + a.equals(b));  // true
        System.out.println("a.equals(c): " + a.equals(c));  // true
        System.out.println("a.equals(d): " + a.equals(d));  // true
        System.out.println("a.equals(e): " + a.equals(e));  // true
    }
}
```

## String Pool and Interning

```java
// StringPoolDemo.java — how the JVM optimizes String storage
package com.example;

public class StringPoolDemo {
    public static void main(String[] args) {
        // When you write a string literal, JVM checks the pool first.
        // If found, it reuses the reference — no new object.

        String s1 = "Java";
        String s2 = "Java";
        String s3 = new String("Java");       // heap object, NOT in pool
        String s4 = s3.intern();              // pool version of "Java"

        System.out.println("s1 == s2: " + (s1 == s2));   // true (both from pool)
        System.out.println("s1 == s3: " + (s1 == s3));   // false (heap vs pool)
        System.out.println("s1 == s4: " + (s1 == s4));   // true (interned → pool)

        // Dynamic strings are NOT automatically pooled
        String dynamic1 = new String("Dynamic");
        String dynamic2 = "Dynamic";

        System.out.println("\nDynamic:");
        System.out.println("dynamic1 == dynamic2: " + (dynamic1 == dynamic2));  // false
        System.out.println("dynamic1.intern() == dynamic2: " + (dynamic1.intern() == dynamic2));  // true

        // ── Pool behavior summary ──
        // Literals at compile-time → always pooled
        // new String(...)          → always on heap (unless interned)
        // .intern()                → returns pool version (adds if absent)
        // Concatenation of literals → pooled (compile-time constant)
        // Concatenation with variables → NOT pooled (runtime operation)
    }
}
```

## Common String Methods

```java
// StringMethodsDemo.java — the most frequently used String APIs
package com.example;

public class StringMethodsDemo {
    public static void main(String[] args) {
        String s = "  Hello, Java World!  ";

        // length — number of characters (including spaces)
        System.out.println("length():         " + s.length());

        // charAt — get character at a specific index
        System.out.println("charAt(0):        '" + s.charAt(0) + "'");

        // substring — extract a portion
        System.out.println("substring(2,7):   '" + s.substring(2, 7) + "'");  // [2, 7)

        // indexOf / lastIndexOf — find position of a character or string
        System.out.println("indexOf('J'):     " + s.indexOf('J'));
        System.out.println("indexOf(\"Java\"):  " + s.indexOf("Java"));
        System.out.println("lastIndexOf('l'): " + s.lastIndexOf('l'));

        // contains, startsWith, endsWith
        System.out.println("contains(\"Java\"): " + s.contains("Java"));
        System.out.println("startsWith(\"  \"): " + s.startsWith("  "));
        System.out.println("endsWith(\"!  \"):  " + s.endsWith("!  "));

        // replace — replaces all occurrences
        System.out.println("replace('l','L'): " + s.replace('l', 'L'));
        System.out.println("replace(\"Java\", \"Kotlin\"): " + s.replace("Java", "Kotlin"));

        // replaceAll (regex)
        System.out.println("replaceAll(\"\\\\s+\", \"-\"): " + s.replaceAll("\\s+", "-"));

        // split — splits by regex into String[]
        String csv = "apple,banana,cherry,date";
        String[] parts = csv.split(",");
        System.out.print("split(','):       ");
        for (String p : parts) System.out.print(p + "  ");
        System.out.println();

        // join — static method, joins parts with delimiter
        String joined = String.join(" | ", "A", "B", "C");
        System.out.println("String.join:      " + joined);

        // strip (vs trim) — Unicode-aware trimming (Java 11+)
        String spaced = "\u2000  text with spaces  \u2000";
        System.out.println("trim():           '" + spaced.trim() + "'");
        System.out.println("strip():          '" + spaced.strip() + "'");  // handles more whitespace

        // stripLeading / stripTrailing (Java 11+)
        System.out.println("stripLeading():   '" + s.stripLeading() + "'");
        System.out.println("stripTrailing():  '" + s.stripTrailing() + "'");

        // repeat (Java 11+)
        System.out.println("repeat(3):        '" + "Ha".repeat(3) + "'");  // HaHaHa

        // isBlank (vs isEmpty) — Java 11+
        System.out.println("isBlank():        " + "   ".isBlank());   // true (whitespace only)
        System.out.println("isEmpty():        " + "   ".isEmpty());   // false

        // toLowerCase / toUpperCase
        System.out.println("toLowerCase():    " + s.toLowerCase());
        System.out.println("toUpperCase():    " + s.toUpperCase());

        // valueOf — static, converts various types to String
        System.out.println("valueOf(42):      " + String.valueOf(42));
        System.out.println("valueOf(3.14):    " + String.valueOf(3.14));
        System.out.println("valueOf(true):    " + String.valueOf(true));
    }
}
```

## StringBuilder vs StringBuffer (Performance)

```java
// StringBuilderDemo.java — mutable strings for efficient concatenation
package com.example;

public class StringBuilderDemo {
    public static void main(String[] args) {
        // ── The problem with String concatenation in loops ──
        // Each + creates a new String → O(n²) memory churn!
        String result = "";
        for (int i = 0; i < 10; i++) {
            result = result + i + " ";   // creates 10 new String objects
        }
        System.out.println("Concatenated: " + result);

        // ── StringBuilder (fast, NOT thread-safe) ──
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(i).append(" ");    // mutates internal buffer — no new objects
        }
        System.out.println("StringBuilder: " + sb.toString());

        // ── Common StringBuilder operations ──
        StringBuilder builder = new StringBuilder("Hello");
        builder.append(", ").append("World").append("!");
        System.out.println("Builder: " + builder.toString());

        builder.insert(5, " there");
        System.out.println("After insert: " + builder.toString());

        builder.replace(0, 5, "Hi");
        System.out.println("After replace: " + builder.toString());

        builder.delete(2, 8);
        System.out.println("After delete: " + builder.toString());

        builder.reverse();
        System.out.println("After reverse: " + builder.toString());

        // ── StringBuffer (slower, but thread-safe) ──
        // Same API as StringBuilder, but all methods are synchronized
        StringBuffer buffer = new StringBuffer("Thread-safe");
        buffer.append(" buffer");
        System.out.println("StringBuffer:  " + buffer.toString());

        // ── Performance comparison ──
        int iterations = 100_000;

        long start = System.nanoTime();
        StringBuilder sbFast = new StringBuilder();
        for (int i = 0; i < iterations; i++) sbFast.append('a');
        long sbTime = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuffer bufSlow = new StringBuffer();
        for (int i = 0; i < iterations; i++) bufSlow.append('a');
        long bufTime = System.nanoTime() - start;

        System.out.println("\nStringBuilder: " + sbTime / 1_000_000.0 + " ms");
        System.out.println("StringBuffer:  " + bufTime / 1_000_000.0 + " ms");
        // StringBuilder is typically 2-3× faster (no synchronization overhead).
        // Rule: use StringBuilder unless you need thread safety (rare for local vars).
    }
}
```

## Text Blocks (`""" """`) — Java 13+

```java
// TextBlocksDemo.java — multi-line string literals (Java 13+ preview, 15+ final)
package com.example;

public class TextBlocksDemo {
    public static void main(String[] args) {
        // ── Basic text block ──
        // Delimiter: """ (opening and closing)
        // Leading whitespace is stripped based on the leftmost non-whitespace content
        String html = """
            <html>
                <body>
                    <h1>Hello, Text Blocks!</h1>
                    <p>This is much cleaner than concatenation.</p>
                </body>
            </html>
            """;    // closing """ determines the common indent

        System.out.println("HTML:");
        System.out.println(html);

        // ── JSON example ──
        String json = """
            {
                "name": "Alice",
                "age": 30,
                "languages": ["Java", "Python", "Rust"],
                "active": true
            }
            """;
        System.out.println("JSON:");
        System.out.println(json);

        // ── SQL example ──
        String query = """
            SELECT id, name, email
            FROM users
            WHERE active = true
              AND created_at > '2024-01-01'
            ORDER BY name
            LIMIT 50
            """;
        System.out.println("SQL:");
        System.out.println(query);

        // ── Trailing spaces and \s, \ line continuation ──
        // \s  → forces a space (visible trailing space marker)
        // \   → line continuation (joins with next line, no newline)
        String formatted = """
            First line.\s
            Second line.\
            Still second line.
            """;
        System.out.println("Formatted:");
        System.out.println(formatted);
        // The \s keeps the trailing space, the \ continues the line (no line break)
    }
}
```

## `formatted()` and `translateEscapes()` — Java 15+

```java
// FormattingDemo.java — String.formatted() and translateEscapes()
package com.example;

public class FormattingDemo {
    public static void main(String[] args) {
        // ── formatted() — instance method (Java 15+) ──
        // Equivalent to String.format(), but called on the template itself
        String template = "Hello, %s! You have %d new messages.";
        String message = template.formatted("Alice", 5);
        System.out.println("formatted(): " + message);

        // Also works on text blocks
        String receipt = """
            =========== RECEIPT ===========
            Item:      %s
            Price:     $%.2f
            Quantity:  %d
            ===============================
            Total:     $%.2f
            """.formatted("Widget", 19.99, 3, 59.97);
        System.out.println(receipt);

        // ── translateEscapes() — Java 15+ ──
        // Converts escape sequences like \n, \t into actual characters
        String raw = "Hello\\nWorld\\tTabbed";
        System.out.println("Raw string:       " + raw);
        // Prints: Hello\nWorld\tTabbed (literal backslash-n)

        String translated = raw.translateEscapes();
        System.out.println("Translated:        " + translated);
        // Prints: Hello
        //         World   Tabbed (actual newline and tab)
        // WARNING: only use translateEscapes() on strings you trust —
        //          it can produce arbitrary control characters!

        // ── String.format() static method (always available) ──
        String padded = String.format("|%10s|%-10s|", "right", "left");
        System.out.println("String.format:    " + padded);

        // ── printf (prints directly) ──
        System.out.printf("printf:  Value = %08d, Pi = %.4f%n", 42, Math.PI);
    }
}
```
