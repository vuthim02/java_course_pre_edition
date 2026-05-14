# Java Foundations — Lesson 10: Packages & Access Modifiers

## Packages: Organizing Your Code

A **package** is a namespace that groups related classes — like folders on your computer:

```
File system:          Java packages:
src/                  package com.example.myapp;
├── com/                  │
│   └── example/          │
│       └── myapp/        │
│           └── Main.java │
└── ...                   
```

### Why Packages?

1. **Organization** — Group related classes together
2. **Namespace management** — Prevent naming conflicts
3. **Access control** — Packages provide visibility boundaries
4. **Distribution** — JAR files are organized by package

### Naming Convention

Use your **reversed domain name** as the root:

```
Website:       example.com
Package:       com.example
Company:       google.com
Package:       com.google
University:    mit.edu
Package:       edu.mit
```

### Creating and Using Packages

```java
// File: src/com/example/myapp/Hello.java
package com.example.myapp;

public class Hello {
    public void sayHi() {
        System.out.println("Hi from a package!");
    }
}
```

```java
// File: src/com/example/myapp/Main.java
package com.example.myapp;

public class Main {
    public static void main(String[] args) {
        Hello h = new Hello();  // Same package — no import needed
        h.sayHi();
    }
}
```

```java
// File: src/com/example/other/Test.java
package com.example.other;

import com.example.myapp.Hello;  // Different package — must import

public class Test {
    public static void main(String[] args) {
        Hello h = new Hello();
        h.sayHi();
    }
}
```

### Compiling with Packages

```bash
# From the src/ directory:
javac com/example/myapp/*.java

# Run with full qualified name:
java com.example.myapp.Main
```

### The `import` Statement

```java
// Import a specific class
import java.util.Scanner;
import java.util.ArrayList;

// Import all classes from a package
import java.util.*;  // Don't use in production — unclear what's used

// Import static members (Java 5+)
import static java.lang.Math.PI;
import static java.lang.Math.sqrt;

public class Test {
    double area = PI * 5 * 5;  // No Math. prefix needed!
    double root = sqrt(25);
}
```

### Default Package (No Package)

If you don't declare a package, your class goes in the **default package**. Only use for tiny examples — never in real projects.

## Access Modifiers

Access modifiers control **who can see/use** your class members.

```
                    ┌─────────────────────────────────────────────┐
                    │         VISIBILITY TABLE                   │
                    ├──────────┬──────────┬──────────┬───────────┤
                    │  Same    │  Same    │  Subclass │  Anywhere │
                    │  Class   │ Package  │  (diff pkg)│  (world)  │
├────────────────────┼──────────┼──────────┼───────────┼───────────┤
│ public            │    ✅    │    ✅    │    ✅     │    ✅     │
│ protected         │    ✅    │    ✅    │    ✅     │    ❌     │
│ (default/no mod)  │    ✅    │    ✅    │    ❌     │    ❌     │
│ private           │    ✅    │    ❌    │    ❌     │    ❌     │
└───────────────────┴──────────┴──────────┴───────────┴───────────┘
```

### Applied to Classes

```java
// Top-level classes can be: public OR package-private (default)
public class PublicClass {
    // Accessible from anywhere
}

class PackagePrivateClass {
    // Accessible only within the same package
}
```

### Applied to Members (Fields, Methods, Constructors)

```java
package com.example;

public class BankAccount {
    // PUBLIC — accessible from anywhere
    public String accountNumber;

    // PROTECTED — accessible from subclasses + same package
    protected double balance;

    // DEFAULT (no modifier) — accessible from same package only
    int transactionCount;

    // PRIVATE — accessible only within THIS class
    private String pinCode;

    // Constructor
    public BankAccount(String accountNumber, String pinCode) {
        this.accountNumber = accountNumber;
        this.pinCode = pinCode;
        this.balance = 0.0;
        this.transactionCount = 0;
    }

    // Public method — everyone can deposit
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
            transactionCount++;
        }
    }

    // Private method — internal use only
    private boolean validatePin(String inputPin) {
        return this.pinCode.equals(inputPin);
    }

    // Protected method — subclasses can access
    protected void applyInterest(double rate) {
        balance += balance * rate;
    }
}
```

### Best Practice: Encapsulation

**Rule:** Make fields `private`, provide `public` methods to access them:

```java
public class Person {
    private String name;   // ← PRIVATE field
    private int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {    // ← PUBLIC getter
        return name;
    }

    public void setName(String name) {  // ← PUBLIC setter (with validation)
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public int getAge() {
        return age;
    }
}
```

## The `import` Hierarchy

Java automatically imports:
1. Everything from `java.lang.*` — no import needed for `String`, `System`, `Math`, etc.
2. Everything in the same package
3. Everything you explicitly import

## Common Package Structure

```
com.example.myapp/
├── Main.java
├── model/              ← Data classes (Person, Order, etc.)
│   ├── Person.java
│   └── Order.java
├── service/            ← Business logic
│   ├── UserService.java
│   └── OrderService.java
├── repository/         ← Data access
│   └── UserRepository.java
├── controller/         ← Web layer
│   └── UserController.java
└── config/             ← Configuration
    └── AppConfig.java
```

---

### Exercises

1. Create a package `com.yourname.utils` with a class `StringUtils` that has a static method `reverse(String)`.
2. Use that class from a different package.
3. Create a class with fields at all 4 access levels. Show which are accessible from where.
4. Create a simple library project with proper package structure.
