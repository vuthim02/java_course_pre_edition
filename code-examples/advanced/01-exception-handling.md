# Exception Handling

This document covers exception handling best practices in Java, including checked vs unchecked exceptions, try-catch-finally, try-with-resources, custom exceptions, exception chaining, and performance considerations.

## Custom Exception and try-catch-finally

```java
// Custom checked exception
class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Custom unchecked exception
class InvalidAccountOperationException extends RuntimeException {
    public InvalidAccountOperationException(String message) {
        super(message);
    }
}

class BankAccount {
    private String accountId;
    private double balance;

    public BankAccount(String accountId, double balance) {
        this.accountId = accountId;
        this.balance = balance;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount <= 0) {
            throw new InvalidAccountOperationException("Amount must be positive: " + amount);
        }
        if (amount > balance) {
            throw new InsufficientFundsException(
                "Insufficient funds: need " + amount + " but only have " + balance);
        }
        balance -= amount;
    }

    public double getBalance() {
        return balance;
    }
}

public class ExceptionHandlingDemo {
    public static void main(String[] args) {
        BankAccount account = new BankAccount("ACC-001", 500.00);

        // try-catch-finally demonstration
        System.out.println("--- try-catch-finally ---");
        try {
            System.out.println("Attempting withdrawal of $600...");
            account.withdraw(600.00);
            System.out.println("Withdrawal successful"); // never reached
        } catch (InsufficientFundsException e) {
            System.err.println("Caught: " + e.getMessage());
        } catch (InvalidAccountOperationException e) {
            System.err.println("Caught unchecked: " + e.getMessage());
        } finally {
            // Always executes — used for cleanup (closing streams, releasing locks)
            System.out.println("Finally block: balance is $" + account.getBalance());
        }

        // throw vs throws — demonstrated above:
        // 'throws' in method signature declares exception may be thrown
        // 'throw' actually creates and throws an exception object

        // Exception chaining
        System.out.println("\n--- Exception Chaining ---");
        try {
            methodThatChains();
        } catch (Exception e) {
            System.err.println("Chained exception: " + e.getMessage());
            System.err.println("Cause: " + e.getCause().getMessage());
        }
    }

    static void methodThatChains() throws Exception {
        try {
            riskyOperation();
        } catch (NumberFormatException e) {
            // Wrap with context and chain the cause
            throw new Exception("Failed to process input", e);
        }
    }

    static void riskyOperation() {
        Integer.parseInt("not-a-number"); // throws NumberFormatException
    }
}
```

## try-with-resources (AutoCloseable)

```java
import java.io.*;

// Custom resource implementing AutoCloseable
class DatabaseConnection implements AutoCloseable {
    private final String name;

    public DatabaseConnection(String name) {
        this.name = name;
        System.out.println("Opening connection: " + name);
    }

    public void query(String sql) {
        System.out.println("Executing: " + sql);
        if (sql.contains("FAIL")) {
            throw new RuntimeException("Query failed");
        }
    }

    @Override
    public void close() {
        System.out.println("Closing connection: " + name);
        // Resources are closed in reverse order of declaration
    }
}

public class TryWithResourcesDemo {
    public static void main(String[] args) {
        // Multiple resources — closed in reverse order (conn2 then conn1)
        System.out.println("--- try-with-resources ---");
        try (DatabaseConnection conn1 = new DatabaseConnection("DB-Primary");
             DatabaseConnection conn2 = new DatabaseConnection("DB-ReadReplica")) {

            conn1.query("SELECT * FROM users");
            conn2.query("SELECT * FROM cache");

        } // auto-close called even if exception occurs
        System.out.println("Resources automatically closed.\n");

        // With exception during close — suppressed exceptions
        System.out.println("--- Suppressed Exceptions ---");
        try (ResourceThatFailsOnClose r = new ResourceThatFailsOnClose()) {
            throw new RuntimeException("Primary exception");
        } catch (RuntimeException e) {
            System.err.println("Primary: " + e.getMessage());
            // getSuppressed() returns exceptions thrown during close
            for (Throwable suppressed : e.getSuppressed()) {
                System.err.println("  Suppressed: " + suppressed.getMessage());
            }
        }
    }
}

class ResourceThatFailsOnClose implements AutoCloseable {
    @Override
    public void close() {
        throw new RuntimeException("Close failure");
    }
}
```

## Multiple Catch and Multi-Catch

```java
import java.io.*;
import java.net.*;

public class MultipleCatchDemo {
    public static void main(String[] args) {
        // Multiple catch blocks — ordered from most specific to most general
        System.out.println("--- Multiple Catch Blocks ---");
        try {
            readAndParse("data.txt");
        } catch (FileNotFoundException e) {
            // Most specific first
            System.err.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            // Intermediate
            System.err.println("IO error: " + e.getMessage());
        } catch (Exception e) {
            // Most general last — catches anything not caught above
            System.err.println("General error: " + e.getMessage());
        }

        // Multi-catch (| operator) — same handling for unrelated exceptions
        System.out.println("\n--- Multi-Catch (| operator) ---");
        try {
            parseInput("abc");
        } catch (IllegalArgumentException | NullPointerException e) {
            // Catch multiple exception types with same handler
            // Variable 'e' is effectively final
            System.err.println("Invalid input: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    static void readAndParse(String filename) throws IOException {
        if (!new File(filename).exists()) {
            throw new FileNotFoundException("Cannot find: " + filename);
        }
        // Could also throw other IOExceptions
    }

    static void parseInput(String input) {
        if (input == null) {
            throw new NullPointerException("Input is null");
        }
        if (input.length() < 3) {
            throw new IllegalArgumentException("Too short: " + input);
        }
    }
}
```

## Performance Notes

```java
/*
 * EXCEPTION HANDLING PERFORMANCE NOTES:
 *
 * 1. Creating an exception is expensive (fills stack trace) — don't use exceptions
 *    for normal control flow.
 *
 * 2. try blocks themselves have near-zero overhead if no exception is thrown.
 *    JIT compilers optimize the happy path.
 *
 * 3. Once an exception is thrown, performance degrades significantly because:
 *    - Stack trace is populated (walking the stack)
 *    - Exception object is allocated
 *    - JIT-deoptimized code may need recompilation
 *
 * 4. Best practices:
 *    - Use exceptions for exceptional conditions only
 *    - Do NOT use exceptions for validation of expected input
 *    - Consider returning Optional<T> for expected "no result" cases
 *    - Prefer try-with-resources over try-finally for resource cleanup
 *    - Catch specific exceptions, not 'Exception' or 'Throwable'
 *    - Log exceptions at the appropriate level, or wrap with context
 *
 * 5. Suppressed exceptions: when both try block AND close() throw, the
 *    close exception is suppressed. Always check getSuppressed() when
 *    debugging resource issues.
 */
```
