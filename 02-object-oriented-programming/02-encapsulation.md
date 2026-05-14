# OOP — Lesson 2: Encapsulation

## What is Encapsulation?

**Encapsulation** = bundling data (fields) with methods that operate on that data, AND hiding internal details from outside.

```
BEFORE (No Encapsulation):     AFTER (Encapsulated):
┌─────────────────────┐       ┌─────────────────────┐
│  BankAccount        │       │  BankAccount        │
│                     │       │                     │
│  balance = public   │       │  - balance: private │
│  (anyone can        │       │  (hidden!)          │
│   directly change)  │       │                     │
│                     │       │  + deposit(amount)  │
│  account.deposit =  │       │  + withdraw(amount) │
│  -1000000  ← BAD!   │       │  + getBalance()     │
└─────────────────────┘       └─────────────────────┘
```

**The Golden Rule:** Make fields `private`, provide `public` methods to access them.

## Why Encapsulation?

### 1. Validation — Prevent Invalid State

```java
public class Person {
    private int age;  // ← PRIVATE — can't access directly

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be 0-150");
        }
        this.age = age;
    }

    public int getAge() {
        return age;
    }
}
```

Without encapsulation:
```java
person.age = -5;  // ← Completely invalid, but allowed!
```

With encapsulation:
```java
person.setAge(-5);  // ← Throws IllegalArgumentException!
```

### 2. Flexibility — Change Internal Implementation

Encapsulated code lets you change HOW things work without breaking callers:

```java
// Version 1: Simple field
public class Temperature {
    private double celsius;

    public double getFahrenheit() {
        return celsius * 9/5 + 32;
    }
}

// Version 2: Changed to store Fahrenheit internally
// Callers don't care — getFahrenheit() still works!
public class Temperature {
    private double fahrenheit;  // Changed internal storage!

    public double getFahrenheit() {
        return fahrenheit;  // Same method, different implementation
    }
}
```

### 3. Maintainability — Controlled Access

```java
public class BankAccount {
    private double balance;
    private List<String> transactionLog = new ArrayList<>();

    public void deposit(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        balance += amount;
        transactionLog.add("Deposit: $" + amount);
    }

    public double getBalance() {
        return balance;
    }

    public List<String> getTransactionHistory() {
        return Collections.unmodifiableList(transactionLog);  // Read-only!
    }
}
```

## Getters and Setters — The Java Pattern

```java
public class Employee {
    private String name;
    private double salary;
    private boolean active;

    // Getter — read access
    public String getName() {
        return name;
    }

    // Setter — write access (with validation)
    public void setName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        this.name = name;
    }

    // Getter for boolean — uses 'is' prefix!
    public boolean isActive() {
        return active;
    }

    // Read-only field (no setter!)
    public double getSalary() {
        return salary;
    }
}
```

### Getter/Setter Naming Convention

| Field Type | Getter Pattern | Setter Pattern |
|-----------|----------------|----------------|
| `String name` | `getName()` | `setName(String)` |
| `int age` | `getAge()` | `setAge(int)` |
| `boolean active` | `isActive()` | `setActive(boolean)` |
| `Boolean active` (object) | `getActive()` | `setActive(Boolean)` |

### Immutable Objects (No Setters At All)

```java
public final class Point {  // final class = can't be extended
    private final int x;     // final field = set once in constructor
    private final int y;

    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // Only getters — NO setters!
    public int getX() { return x; }
    public int getY() { return y; }

    // Methods return NEW objects instead of modifying
    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);  // New object!
    }
}
```

**Benefits of immutability:** Thread-safe, cacheable, predictable, no defensive copies needed.

## Data Hiding with Access Modifiers

```java
public class Service {
    // Constants — public is OK (can't change final primitives)
    public static final int MAX_RETRIES = 3;

    // Internal implementation — HIDE IT
    private HttpClient client;
    private Queue<String> requestQueue;

    // Configuration — protected (subclasses can see)
    protected int timeoutSeconds = 30;

    // Package-private — visible to test classes in same package
    Logger logger = Logger.getLogger();

    // Public API — what users SHOULD call
    public String fetchData(String url) {
        validateUrl(url);  // private method — internal
        return client.get(url);
    }

    // Private helper — implementation detail
    private void validateUrl(String url) {
        if (url == null || !url.startsWith("https")) {
            throw new IllegalArgumentException("Invalid URL");
        }
    }
}
```

## Real-World Best Practices

1. **Prefer immutability** — Make fields `final` if they never change
2. **Don't expose internal collections** — Return copies or unmodifiable views
3. **Validate in setters/constructors** — Never trust incoming data
4. **Use `Optional` for possibly-null returns** (not for fields or parameters)
5. **Avoid getters/setters for every field** — Only expose what's needed
6. **Tell, Don't Ask** — Give objects behavior, don't ask for data to do it yourself

### Tell, Don't Ask — Applied

```java
// BAD: Ask for data, do work externally
double tax = employee.getSalary() * 0.2;

// GOOD: Tell the object to do the work
double tax = employee.calculateTax();  // Object knows its own salary!
```

---

### Exercises

1. Create an encapsulated `BankAccount` class with: private balance, public deposit/withdraw/getBalance. Add validation.
2. Create an `immutable` `CreditCard` class with card number, expiry, CVV. Only getters, no setters.
3. Create a `ShoppingCart` class that internally uses a `List<Item>`. Provide methods to add/remove items but return an unmodifiable view of the list.
4. Refactor a non-encapsulated class into an encapsulated one.
