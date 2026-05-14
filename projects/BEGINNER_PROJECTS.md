# Beginner Projects

## Project 1: Calculator (Console)

**Concepts:** Variables, methods, control flow, switch expressions

```java
public class Calculator {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=== Calculator ===");
        System.out.print("Enter first number: ");
        double a = scanner.nextDouble();
        System.out.print("Enter operator (+, -, *, /): ");
        String op = scanner.next();
        System.out.print("Enter second number: ");
        double b = scanner.nextDouble();

        double result = switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : Double.NaN;
            default -> throw new IllegalArgumentException("Invalid operator");
        };

        System.out.printf("%.2f %s %.2f = %.2f%n", a, op, b, result);
    }
}
```

**Extend with:** history, memory, scientific functions, GUI

## Project 2: Banking System

**Concepts:** OOP, encapsulation, ArrayList, file I/O

Build a banking system with:
- Account creation (checking, savings)
- Deposit/Withdraw/Transfer
- Transaction history
- Balance inquiry
- Interest calculation (savings)
- File persistence

**Architecture:**
```
com.example.bank/
├── model/
│   ├── Account.java (abstract)
│   ├── CheckingAccount.java
│   ├── SavingsAccount.java
│   └── Transaction.java (record)
├── service/
│   ├── BankService.java
│   └── TransactionService.java
├── repository/
│   └── AccountRepository.java (file-based)
└── Main.java
```

## Project 3: File Manager

**Concepts:** NIO, recursion, streams

Build a file manager with:
- List files/directories
- Navigate directories
- Copy/move/delete files
- Search by name/pattern
- Show file sizes
- File preview (text files)

## Project 4: Chat Application

**Concepts:** Networking, threads, I/O

Build a console-based chat:
- Server (multi-threaded)
- Client
- Multiple chat rooms
- Username support
- Message history

## Project 5: Inventory System

**Concepts:** Collections, CRUD, serialization, sorting

Build an inventory management system:
- Add/update/delete products
- Search products
- Low-stock alerts
- Sort by name/price/quantity
- Save/load from file (serialization or JSON)
