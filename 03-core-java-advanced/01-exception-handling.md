# Core Java Advanced — Lesson 1: Exception Handling

## What is an Exception?

An **exception** is an event that disrupts the normal flow of a program. Instead of crashing, Java lets you **handle** exceptions gracefully.

```
Normal flow:    Step A → Step B → Step C → Done
                                       ↓
With exception: Step A → Step B → EXCEPTION → HANDLER → Done
```

## The Exception Hierarchy

```
                    Object
                       │
                  Throwable
                 ┌─────┴─────┐
                 │           │
            Exception      Error
          ┌───────┴───────┐  │
          │               │  │
    Checked           Unchecked  VirtualMachineError,
    (compile-time)    (runtime)  OutOfMemoryError,
                                StackOverflowError

    Checked examples:    RuntimeException examples:
    • IOException        • NullPointerException
    • SQLException       • ArrayIndexOutOfBoundsException
    • FileNotFoundException  • IllegalArgumentException
    • InterruptedException   • NumberFormatException
```

### Checked vs Unchecked

```java
// CHECKED — compiler forces you to handle!
public void readFile() throws IOException {  // MUST declare or handle
    FileReader reader = new FileReader("file.txt");  // IOException!
}

// UNCHECKED (RuntimeException) — compiler does NOT force handling
public void divide(int a, int b) {
    int result = a / b;  // ArithmeticException — not forced to handle
}
```

## Handling Exceptions

### try-catch

```java
try {
    // Code that might throw an exception
    int result = 10 / 0;  // ArithmeticException
    System.out.println("This won't print");
} catch (ArithmeticException e) {
    // Handle the exception
    System.err.println("Cannot divide by zero: " + e.getMessage());
}
System.out.println("Program continues...");
```

### Multiple catch Blocks

```java
try {
    String input = readInput();
    int number = Integer.parseInt(input);
    int result = 100 / number;
    System.out.println("Result: " + result);
} catch (NumberFormatException e) {
    System.err.println("Invalid number format: " + input);
} catch (ArithmeticException e) {
    System.err.println("Division by zero!");
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
}
```

### Multi-catch (Java 7+)

```java
try {
    // code
} catch (IOException | SQLException e) {
    // Handle both exception types the same way
    System.err.println("Data access error: " + e.getMessage());
}
```

### try-with-resources (Java 7+)

Auto-closes resources that implement `AutoCloseable`:

```java
// Before Java 7 — must close manually:
BufferedReader reader = null;
try {
    reader = new BufferedReader(new FileReader("file.txt"));
    String line = reader.readLine();
} finally {
    if (reader != null) {
        reader.close();  // Easy to forget!
    }
}

// Java 7+ — auto-close:
try (BufferedReader reader = new BufferedReader(new FileReader("file.txt"))) {
    String line = reader.readLine();
    // reader automatically closed, even if exception!
}
```

### finally Block

ALWAYS runs, whether exception or not:

```java
FileInputStream fis = null;
try {
    fis = new FileInputStream("file.txt");
    // ... use stream
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
} finally {
    // ALWAYS executes — even if try has return, or catch throws
    if (fis != null) {
        try {
            fis.close();
        } catch (IOException e) {
            // Log it
        }
    }
}
```

## Throwing Exceptions

```java
public void setAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("Age cannot be negative: " + age);
    }
    if (age > 150) {
        throw new IllegalArgumentException("Age cannot exceed 150: " + age);
    }
    this.age = age;
}
```

## Custom Exceptions

```java
public class InsufficientFundsException extends Exception {
    private final double balance;
    private final double amount;

    public InsufficientFundsException(double balance, double amount) {
        super("Insufficient funds: balance=" + balance + ", required=" + amount);
        this.balance = balance;
        this.amount = amount;
    }

    public double getBalance() { return balance; }
    public double getShortfall() { return amount - balance; }
}
```

```java
public void withdraw(double amount) throws InsufficientFundsException {
    if (amount > balance) {
        throw new InsufficientFundsException(balance, amount);
    }
    balance -= amount;
}
```

## Exception Handling Best Practices

### DO: Be Specific

```java
// BAD — catches everything
try {
    // ...
} catch (Exception e) {  // Too broad!
    System.out.println("Something went wrong");
}

// GOOD — specific
try {
    // ...
} catch (FileNotFoundException e) {
    System.err.println("File not found: " + e.getMessage());
} catch (IOException e) {
    System.err.println("IO error: " + e.getMessage());
}
```

### DO NOT: Swallow Exceptions

```java
// BAD — silent swallow
try {
    // ...
} catch (Exception e) {
    // DO NOTHING — bug hiding!
}

// BAD — print and continue
try {
    // ...
} catch (Exception e) {
    e.printStackTrace();  // Doesn't handle!
}

// GOOD — log and handle
try {
    // ...
} catch (IOException e) {
    logger.error("Failed to read file", e);
    throw new RuntimeException("Could not load configuration", e);
}
```

### DO: Use the Exception Hierarchy

```java
// BAD — throwing generic Exception
public void process() throws Exception { ... }

// GOOD — specific exception
public void process() throws IOException, ValidationException { ... }
```

### DO: Preserve the Cause (Exception Chaining)

```java
try {
    readFile();
} catch (IOException e) {
    throw new ServiceException("Failed to process file", e);  // ← e is the CAUSE
}
```

### DO: Use try-with-resources

Always prefer try-with-resources over manual closing.

## WARNING: Errors vs Exceptions

`Error` subclasses (like `OutOfMemoryError`, `StackOverflowError`) are **not meant to be caught** — they indicate JVM-level problems you can't recover from.

---

### Exercises

1. Write a method `int divideSafely(int a, int b)` that handles division by zero gracefully.
2. Create a custom `InvalidEmailException`. Write an email validator that throws it.
3. Write a file reader that uses try-with-resources. What happens if both the try block AND the close() throw exceptions? (Answer: the close exception is suppressed)
4. Create a bank account `withdraw()` method that throws `InsufficientFundsException` with balance info.
5. Explain in your own words: checked vs unchecked, throw vs throws, try-with-resources.
