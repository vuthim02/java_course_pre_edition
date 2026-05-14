# Architecture & Design — Lesson 1: Clean Code & SOLID Principles

## Clean Code Philosophy

**"Any fool can write code that a computer can understand. Good programmers write code that humans can understand."** — Martin Fowler

### Why Clean Code Matters

```
READABILITY (70% of time)                  BUGS
    │                                        │
    ▼                                        ▼
Code is read FAR more than it's written   Unclear code = bugs hiding
    │                                        │
    ▼                                        ▼
Average dev spends 70% READING code        Clear code = bugs are obvious
    │
    ▼
Clean code = faster development over time
```

### The Boy Scout Rule

**"Leave the campground cleaner than you found it."**

Every time you touch code, make it a little better. Rename a bad variable, extract a method, add a test.

## Naming Conventions

```java
// BAD — what is this?
int d;  // elapsed days?
List<Integer> lst;
public void prc() { }

// GOOD — clear intent
int elapsedDays;
List<Integer> customerIds;
public void processPayment() { }
```

### Rules for Good Names

1. **Reveal intent** — name says WHAT and WHY
2. **Avoid abbreviations** — `cust` → `customer`, `idx` → `index`
3. **Use pronounceable names** — `genymdhms` → `generationTimestamp`
4. **Use searchable names** — single-letter names are hard to find
5. **Class names** — nouns: `Customer`, `Account`, `PaymentProcessor`
6. **Method names** — verbs: `calculateTotal()`, `sendEmail()`, `save()`
7. **Boolean variables** — `isActive`, `hasPermission`, `shouldDelete`

## Functions

```java
// BAD — too many things, too many levels
public void processOrder(Order order) {
    if (order != null) {
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            double total = 0;
            for (Item item : order.getItems()) {
                total += item.getPrice() * item.getQuantity();
            }
            order.setTotal(total);
            if (order.getTotal() > 100) {
                order.setDiscount(0.1);
            }
            EmailSender.send(order.getCustomerEmail(), "Order confirmed");
            Logger.log("Order " + order.getId() + " processed");
        }
    }
}

// GOOD — single responsibility, one level of abstraction
public void processOrder(Order order) {
    validateOrder(order);
    calculateTotal(order);
    applyDiscount(order);
    sendConfirmation(order);
    logProcessing(order);
}

private void validateOrder(Order order) {
    if (order == null || order.getItems().isEmpty()) {
        throw new IllegalArgumentException("Order must have items");
    }
}
```

### Function Rules

1. **Small** — ideally < 20 lines
2. **Do one thing** — one level of abstraction
3. **No side effects** — don't modify unexpected state
4. **Command-Query Separation** — either do something OR return something
5. **Prefer exceptions over error codes**

## Comments

```java
// BAD — explains what, not why
// Increment i by 1
i = i + 1;

// GOOD — explains WHY
// Retry up to 3 times because the external API is unreliable
const int MAX_RETRIES = 3;

// BAD — commented-out code
// private void oldMethod() { ... }

// GOOD — TODO with context
// TODO: Implement pagination when the dataset grows beyond 10K rows
```

## SOLID Principles

### S — Single Responsibility Principle

A class should have ONE reason to change.

```java
// BAD — User class does too much
public class User {
    public void save() { /* DB code */ }
    public void sendEmail() { /* email code */ }
    public void generateReport() { /* report code */ }
}

// GOOD — separated responsibilities
public class User { /* Just data */ }
public class UserRepository { /* DB operations */ }
public class EmailService { /* Email operations */ }
public class ReportGenerator { /* Report operations */ }
```

### O — Open/Closed Principle

Open for extension, closed for modification.

```java
// BAD — adding new shape requires modifying this class
public class AreaCalculator {
    public double calculate(Object shape) {
        if (shape instanceof Circle) { ... }
        else if (shape instanceof Rectangle) { ... }
        // Adding Triangle requires new else-if!
    }
}

// GOOD — polymorphic
public interface Shape {
    double area();
}

public class Circle implements Shape {
    public double area() { return PI * r * r; }
}

public class Rectangle implements Shape {
    public double area() { return w * h; }
}
```

### L — Liskov Substitution

Subtypes must be substitutable for their base types.

```java
// BAD — violates LSP
public class Rectangle {
    private int w, h;
    public void setWidth(int w) { this.w = w; }
    public void setHeight(int h) { this.h = h; }
}

public class Square extends Rectangle {
    @Override
    public void setWidth(int w) { super.setWidth(w); super.setHeight(w); }
    @Override
    public void setHeight(int h) { super.setWidth(h); super.setHeight(h); }
}

// Client code breaks!
Rectangle r = new Square();
r.setWidth(5);
r.setHeight(10);
// r is 10x10, NOT 5x10! Code expected 5x10!
```

### I — Interface Segregation

Don't force clients to implement methods they don't use.

```java
// BAD — fat interface
public interface Worker {
    void work();
    void eat();
    void sleep();
}

// GOOD — segregated interfaces
public interface Workable { void work(); }
public interface Eatable { void eat(); }
public interface Sleepable { void sleep(); }

public class Robot implements Workable { /* work() only */ }
public class Human implements Workable, Eatable, Sleepable { }
```

### D — Dependency Inversion

Depend on abstractions, not concretions.

```java
// BAD — depends on concrete class
public class EmailService {
    private SmtpServer server = new SmtpServer();  // Hard dependency!
}

// GOOD — depends on abstraction
public class EmailService {
    private final MailServer server;  // Abstraction

    public EmailService(MailServer server) {  // Injected
        this.server = server;
    }
}
```

---

### Exercises

1. Refactor a 100+ line method into smaller, well-named methods.
2. Identify violations of each SOLID principle in a given codebase (use your own old code).
3. Apply DRY (Don't Repeat Yourself) to duplicated code.
4. Rename variables in a legacy code file to be self-documenting.
5. Extract a class that violates SRP into multiple focused classes.
