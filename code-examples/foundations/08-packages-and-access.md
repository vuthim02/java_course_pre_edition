# 08 — Packages & Access Modifiers

`package`, `import`, `static import`, and the four access levels: `public`, `protected`, `default` (package-private), `private`.

## Packages and Imports

```java
// File: com/example/packages/PackageDemo.java
package com.example.packages;

// ── Import declarations ──

// Single type import
import java.util.ArrayList;
import java.util.Scanner;

// Wildcard import (imports all public types in java.util)
import java.util.*;

// Static import — imports static members directly
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static java.lang.System.out;   // so we can write out.println() instead of System.out.println()

// Import of a static method
import static java.util.Collections.sort;

public class PackageDemo {
    public static void main(String[] args) {
        // PI and sqrt are available without Math. prefix (static import)
        out.println("PI = " + PI);
        out.println("sqrt(2) = " + sqrt(2.0));

        // Fully qualified name — no import needed (but verbose)
        java.math.BigDecimal bd = new java.math.BigDecimal("123.45");
        out.println("BigDecimal: " + bd);

        // Using imported types
        ArrayList<String> list = new ArrayList<>();
        list.add("banana");
        list.add("apple");
        list.add("cherry");
        sort(list);   // static import of Collections.sort
        out.println("Sorted: " + list);
    }
}
```

## The 4 Access Modifiers — All in One File

```java
// AccessModifiers.java — demonstrates public, protected, default, private
// NOTE: In a real project each class would be in its own file.
//       We put them in one file here for clarity (only one can be public).

package com.example.access;

// ── Public class: accessible from anywhere ──
public class AccessModifiers {
    public static void main(String[] args) {
        // Can we access the members of Parent?
        Parent p = new Parent();
        System.out.println("--- Accessing Parent from same class (main in AccessModifiers) ---");
        System.out.println("publicField:    " + p.publicField);       // ✅ same class
        System.out.println("protectedField: " + p.protectedField);    // ✅ same class
        System.out.println("defaultField:  " + p.defaultField);       // ✅ same class
        // System.out.println(p.privateField);  // ❌ private — not accessible

        p.publicMethod();
        p.protectedMethod();
        p.defaultMethod();

        System.out.println("\n--- Child class in SAME PACKAGE ---");
        ChildInSamePackage child = new ChildInSamePackage();
        child.testAccess();
    }
}

// ── Default (package-private) class: accessible only within the package ──
class Parent {
    public       String publicField    = "public";
    protected    String protectedField = "protected";
    /* default */ String defaultField  = "default (package-private)";
    private      String privateField   = "private";

    public       void publicMethod()    { System.out.println("   public method called"); }
    protected    void protectedMethod() { System.out.println("   protected method called"); }
    /* default */ void defaultMethod()  { System.out.println("   default method called"); }
    private      void privateMethod()   { System.out.println("   private method called"); }

    // Within the same class, everything is accessible
    void showPrivate() {
        System.out.println("   Can access private from within class: " + privateField);
    }
}

// ── Another class in the SAME package ──
class ChildInSamePackage extends Parent {
    void testAccess() {
        System.out.println("From subclass in same package:");
        System.out.println("   publicField:    " + publicField);      // ✅ public
        System.out.println("   protectedField: " + protectedField);   // ✅ protected (inherited)
        System.out.println("   defaultField:  " + defaultField);      // ✅ same package
        // System.out.println(privateField);  // ❌ private — not accessible
        publicMethod();
        protectedMethod();
        defaultMethod();   // ✅ same package
    }
}

// ── Unrelated class in the SAME package ──
class SamePackageClass {
    void testAccess() {
        Parent p = new Parent();
        System.out.println("From unrelated class in same package:");
        System.out.println("   publicField:    " + p.publicField);     // ✅ public
        System.out.println("   protectedField: " + p.protectedField);  // ✅ same package
        System.out.println("   defaultField:  " + p.defaultField);     // ✅ same package
        // System.out.println(p.privateField);  // ❌ private
    }
}
```

## Different Package — Demonstrating What Breaks

```java
// File: com/example/other/OtherPackageTest.java
// A class in a DIFFERENT package trying to access AccessModifiers classes
package com.example.other;

// Import only the public class — the package-private ones are NOT importable
import com.example.access.AccessModifiers;
// import com.example.access.Parent;   // ❌ Parent is package-private!

// Import another public class from yet another package
import com.example.access.PublicHelper;   // assumed public class

public class OtherPackageTest {
    public static void main(String[] args) {
        // ── We CAN use the public class ──
        AccessModifiers demo = new AccessModifiers();

        // ── But we can't instantiate Parent (it's package-private) ──
        // Parent p = new Parent();   // ❌ compilation error

        // What if we get a reference to Parent somehow?
        // Even then, access to members is restricted.
    }
}

// ── Subclass in a DIFFERENT package ──
class ChildInDifferentPackage extends com.example.access.Parent {
    // ❌ Can't extend Parent because Parent is package-private!
    // This class won't compile — Parent is not visible.
}

// ── Correct way: extend a public class ──
class CorrectChild extends AccessModifiers {
    // AccessModifiers is public, so this is fine
}
```

## Access Modifier Summary Table

| Modifier  | Same Class | Same Package | Subclass (diff pkg) | Anywhere |
|-----------|:----------:|:------------:|:-------------------:|:--------:|
| `public`  | ✅ Yes     | ✅ Yes       | ✅ Yes              | ✅ Yes   |
| `protected`| ✅ Yes    | ✅ Yes       | ✅ Yes (via inheritance)| ❌ No |
| default (none)| ✅ Yes | ✅ Yes       | ❌ No               | ❌ No    |
| `private` | ✅ Yes     | ❌ No        | ❌ No               | ❌ No    |

**Key rules:**
- `public` — no restrictions.
- `protected` — accessible in subclasses (even in different packages) via inheritance.
- `default` (no modifier) — package-private. Only classes in the same package can access.
- `private` — only the enclosing class.

**Class-level access:**
- A top-level class can be `public` or `default` (package-private).
- `protected` and `private` only apply to **inner/nested classes**.

## Static Imports — Deeper Example

```java
// StaticImportDemo.java — making static members available without class name
package com.example.packages;

// Static import of all static members of a class
import static java.lang.Math.*;
import static java.util.Calendar.*;

public class StaticImportDemo {
    public static void main(String[] args) {
        // Without static import:
        double r1 = Math.sqrt(Math.pow(3, 2) + Math.pow(4, 2));

        // With static import:
        // sqrt, pow, PI, etc. are all directly available
        double r2 = sqrt(pow(3, 2) + pow(4, 2));
        double area = PI * r2 * r2;

        System.out.println("r2 = " + r2);
        System.out.println("Area = " + area);

        // Static import from Calendar
        // int month = Calendar.JANUARY;   // without import
        int month = JANUARY;                // with import
        System.out.println("Month constant: " + month);

        // ── Caution ──
        // Overuse of static imports can hurt readability.
        // Good for: Math, Collections, constants you use very frequently.
        // Bad for: making everything look like a built-in.
    }
}
```
