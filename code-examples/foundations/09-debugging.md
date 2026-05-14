# 09 — Debugging & Error Handling

Common exceptions, reading stack traces, `try-catch-finally`, `try-with-resources`, and creating custom exceptions.

## NullPointerException (NPE)

```java
// NullPointerDemo.java — the most common Java exception
package com.example;

class Person {
    String name;
    Address address;
}

class Address {
    String city;
}

public class NullPointerDemo {
    public static void main(String[] args) {
        // ── Classic NPE ──
        Person p = null;
        try {
            System.out.println(p.name);   // ❌ p is null
        } catch (NullPointerException e) {
            System.out.println("NPE caught! " + e.getMessage());
            e.printStackTrace();   // prints the full stack trace
        }

        // ── Chained null (dereferencing a null return) ──
        Person alice = new Person();
        alice.name = "Alice";
        // alice.address is null!

        try {
            System.out.println(alice.address.city);   // ❌ alice.address is null
        } catch (NullPointerException e) {
            System.out.println("NPE on chained access: " + e.getMessage());
        }

        // ── How to FIX with defensive checks ──
        if (alice.address != null) {
            System.out.println(alice.address.city);
        } else {
            System.out.println("No address for " + alice.name);
        }

        // ── Modern fix: Objects.requireNonNull (Java 7+) ──
        String maybeNull = null;
        try {
            java.util.Objects.requireNonNull(maybeNull, "Variable must not be null");
        } catch (NullPointerException e) {
            System.out.println("requireNonNull caught: " + e.getMessage());
        }
    }
}
```

## ArrayIndexOutOfBoundsException

```java
// ArrayIndexDemo.java — accessing indexes outside array bounds
package com.example;

public class ArrayIndexDemo {
    public static void main(String[] args) {
        int[] arr = {10, 20, 30};

        // ── Too high ──
        try {
            System.out.println(arr[5]);   // ❌ index 3 is out of bounds for length 3
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Index out of bounds: " + e.getMessage());
        }

        // ── Negative index ──
        try {
            System.out.println(arr[-1]);   // ❌ negative index
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Negative index: " + e.getMessage());
        }

        // ── Off-by-one (very common) ──
        try {
            for (int i = 0; i <= arr.length; i++) {   // <= instead of <
                System.out.print(arr[i] + " ");
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\nOff-by-one error! i = " + (arr.length) + " is out of bounds");
        }

        // ── Fix: use < instead of <=, and always check bounds ──
        System.out.print("Safe loop: ");
        for (int i = 0; i < arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();

        // ── Common in 2D arrays ──
        int[][] grid = {{1, 2}, {3, 4}};
        try {
            System.out.println(grid[2][0]);   // ❌ row 2 doesn't exist
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("2D array bounds: " + e.getMessage());
        }
    }
}
```

## StackOverflowError (Deep Recursion)

```java
// StackOverflowDemo.java — blowing the call stack
package com.example;

public class StackOverflowDemo {
    static int depth = 0;

    // Infinite recursion
    static void explode() {
        depth++;
        explode();   // never returns → stack fills up
    }

    public static void main(String[] args) {
        try {
            explode();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError after " + depth + " calls");
            // Typical depth: 5,000–15,000 calls depending on JVM stack size
        }

        // ── How to avoid ──
        // 1. Ensure recursion has a proper base case
        // 2. Convert deep recursion to iteration
        // 3. Increase stack size: java -Xss10m StackOverflowDemo
        // 4. Use tail-recursion-friendly languages (not Java — no TCO)
        System.out.println("Program continues after catching the error.");

        // StackOverflowError is an Error, NOT an Exception —
        // it's not checked, and you generally should NOT try to catch it.
        // The example above is for demonstration only.
    }
}
```

## ArithmeticException (Divide by Zero)

```java
// ArithmeticDemo.java — integer division by zero
package com.example;

public class ArithmeticDemo {
    public static void main(String[] args) {
        // ── Integer division by zero ──
        try {
            int result = 10 / 0;   // ❌ throws ArithmeticException
            System.out.println(result);
        } catch (ArithmeticException e) {
            System.out.println("Can't divide int by zero: " + e.getMessage());
        }

        // ── Floating-point does NOT throw! ──
        double d1 = 10.0 / 0.0;    // Infinity
        double d2 = -10.0 / 0.0;   // -Infinity
        double d3 = 0.0 / 0.0;     // NaN (Not a Number)
        System.out.println("10.0 / 0.0  = " + d1);
        System.out.println("-10.0 / 0.0 = " + d2);
        System.out.println("0.0 / 0.0   = " + d3);

        // ── Modulo by zero also throws ──
        try {
            int x = 10 % 0;
        } catch (ArithmeticException e) {
            System.out.println("Can't mod by zero either: " + e.getMessage());
        }

        // ── How to protect ──
        int numerator = 10;
        int denominator = 0;
        if (denominator != 0) {
            System.out.println("Result: " + (numerator / denominator));
        } else {
            System.out.println("Cannot divide by zero");
        }
    }
}
```

## NumberFormatException

```java
// NumberFormatDemo.java — parsing strings into numbers
package com.example;

public class NumberFormatDemo {
    public static void main(String[] args) {
        // ── Common parsing failure ──
        String badNumber = "12a34";

        try {
            int value = Integer.parseInt(badNumber);   // ❌
            System.out.println(value);
        } catch (NumberFormatException e) {
            System.out.println("Can't parse '" + badNumber + "': " + e.getMessage());
        }

        // ── Other parse failures ──
        String[] inputs = {"", "   ", "null", "2147483648", "3.14"};
        for (String input : inputs) {
            try {
                int val = Integer.parseInt(input);
                System.out.println("Parsed: " + val);
            } catch (NumberFormatException e) {
                System.out.println("Failed to parse '" + input + "': " + e.getMessage());
            }
        }

        // ── How to handle safely ──
        String userInput = "  42  ";
        try {
            // Strip whitespace before parsing
            int value = Integer.parseInt(userInput.strip());
            System.out.println("\nSafe parse result: " + value);
        } catch (NumberFormatException e) {
            System.out.println("Invalid number: " + userInput);
        }

        // ── Better: use Integer.parseInt with try-catch, or use a regex check ──
        String safe = "123";
        if (safe.matches("\\d+")) {   // basic check (doesn't handle negatives or overflow)
            System.out.println("Regex says it's a number: " + Integer.parseInt(safe));
        }
    }
}
```

## ClassCastException

```java
// ClassCastDemo.java — invalid downcasting
package com.example;

class Animal {
    void speak() { System.out.println("Some sound"); }
}

class Dog extends Animal {
    void bark() { System.out.println("Woof!"); }
}

class Cat extends Animal {
    void meow() { System.out.println("Meow!"); }
}

public class ClassCastDemo {
    public static void main(String[] args) {
        Animal animal = new Dog();   // upcast → implicit, always safe
        animal.speak();

        // ── Correct downcast (Dog was originally a Dog) ──
        if (animal instanceof Dog) {
            Dog dog = (Dog) animal;   // safe
            dog.bark();
        }

        // ── Incorrect downcast ──
        try {
            Cat cat = (Cat) animal;   // ❌ animal is actually a Dog
            cat.meow();
        } catch (ClassCastException e) {
            System.out.println("ClassCastException: " + e.getMessage());
        }

        // ── Fix: always use instanceof before downcasting ──
        Animal another = new Cat();
        if (another instanceof Cat) {
            Cat c = (Cat) another;    // safe
            c.meow();
        }

        // ── Pattern matching for instanceof (Java 16+) eliminates explicit cast ──
        if (another instanceof Cat c) {
            c.meow();   // no explicit cast needed
        }
    }
}
```

## try-catch-finally

```java
// TryCatchFinallyDemo.java — proper resource cleanup
package com.example;

import java.io.*;

public class TryCatchFinallyDemo {
    public static void main(String[] args) {
        BufferedReader reader = null;
        try {
            // Code that might throw
            reader = new BufferedReader(new FileReader("nonexistent.txt"));
            String line = reader.readLine();
            System.out.println(line);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        } finally {
            // ALWAYS executes — even if there's a return or exception in try block
            System.out.println("Finally: cleaning up resources...");
            if (reader != null) {
                try {
                    reader.close();   // closing can also throw!
                } catch (IOException e) {
                    System.out.println("Error closing reader: " + e.getMessage());
                }
            }
        }

        System.out.println("Program continues...");

        // ── finally without catch (rare, but valid) ──
        // If you don't catch, the exception propagates, but finally still runs.
        // try {
        //     riskyOperation();
        // } finally {
        //     System.out.println("Cleanup — even if exception propagates");
        // }
    }
}
```

## try-with-resources (Java 7+)

```java
// TryWithResourcesDemo.java — automatic resource management
package com.example;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class TryWithResourcesDemo {
    public static void main(String[] args) {
        // ── try-with-resources ──
        // Any class implementing AutoCloseable is automatically closed.
        // This is the PREFERRED way to handle resources.

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("demo", ".txt");
            Files.writeString(tempFile, "Hello, try-with-resources!");

            // Resources declared in the try header are closed automatically
            try (BufferedReader reader = Files.newBufferedReader(tempFile)) {
                String line = reader.readLine();
                System.out.println("Read from file: " + line);
            }   // reader.close() called automatically here (even if exception occurs)

        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
        } finally {
            // Clean up temp file
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    System.out.println("Could not delete temp file");
                }
            }
        }

        // ── Multiple resources in one try ──
        // They are closed in reverse order of declaration.
        try (Scanner scanner = new Scanner(System.in);
             Formatter formatter = new Formatter(System.out)) {
            // Both scanner and formatter will be closed automatically
        }

        // ── Custom AutoCloseable ──
        try (MyResource r = new MyResource("Resource-A")) {
            r.work();
            // throw new RuntimeException("Something went wrong");
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
        }
        // MyResource.close() is called even though we caught the exception
    }
}

// Custom class implementing AutoCloseable
class MyResource implements AutoCloseable {
    private final String name;

    MyResource(String name) {
        this.name = name;
        System.out.println("  Opened: " + name);
    }

    void work() {
        System.out.println("  Working on " + name);
    }

    @Override
    public void close() {
        System.out.println("  Closed: " + name);
    }
}
```

## Custom Exception Class

```java
// CustomExceptionDemo.java — creating and using your own exception types
package com.example;

// ── Custom checked exception ──
// Extend Exception to make it checked (compiler forces you to handle it).
class InsufficientFundsException extends Exception {
    private final double deficit;

    // Constructor with message
    public InsufficientFundsException(String message) {
        super(message);
        this.deficit = 0;
    }

    // Constructor with message and cause
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
        this.deficit = 0;
    }

    // Constructor with additional data
    public InsufficientFundsException(double balance, double amount) {
        super("Insufficient funds: balance=" + balance + ", requested=" + amount);
        this.deficit = amount - balance;
    }

    public double getDeficit() {
        return deficit;
    }
}

// ── Custom unchecked exception ──
// Extend RuntimeException to make it unchecked.
class InvalidTransactionException extends RuntimeException {
    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}

// ── Bank account using custom exceptions ──
class BankAccount {
    private double balance;

    public BankAccount(double initialBalance) {
        if (initialBalance < 0) {
            throw new InvalidTransactionException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount < 0) {
            throw new InvalidTransactionException("Withdrawal amount cannot be negative");
        }
        if (amount > balance) {
            throw new InsufficientFundsException(balance, amount);
        }
        balance -= amount;
    }

    public double getBalance() { return balance; }
}

// ── Demo ──
public class CustomExceptionDemo {
    public static void main(String[] args) {
        BankAccount account = new BankAccount(500);

        System.out.println("Initial balance: $" + account.getBalance());

        // 1 — Successful withdrawal
        try {
            account.withdraw(100);
            System.out.println("Withdrew $100. Balance: $" + account.getBalance());
        } catch (InsufficientFundsException e) {
            System.out.println("Error: " + e.getMessage());
        }

        // 2 — Insufficient funds (checked exception — must be handled)
        try {
            account.withdraw(1000);
        } catch (InsufficientFundsException e) {
            System.out.println("Overdraft prevented: " + e.getMessage());
            System.out.println("  Deficit: $" + e.getDeficit());
        }

        // 3 — Invalid amount (unchecked — no try-catch required)
        try {
            account.withdraw(-50);   // throws InvalidTransactionException
        } catch (InvalidTransactionException e) {
            System.out.println("Invalid transaction: " + e.getMessage());
        } catch (InsufficientFundsException e) {
            System.out.println("Shouldn't reach here");
        }

        // 4 — Checked exception must be declared or caught
        // The compiler would complain if we didn't handle InsufficientFundsException.
        System.out.println("\nFinal balance: $" + account.getBalance());

        // ── Best practices for custom exceptions ──
        // 1. Suffix with "Exception" (e.g., InsufficientFundsException)
        // 2. Provide constructors that match superclass patterns
        // 3. Consider including useful data (deficit, account id, etc.)
        // 4. Use checked exceptions for recoverable conditions
        // 5. Use unchecked (RuntimeException) for programming errors
    }
}
```
