# Banking System

A console-based banking system demonstrating OOP principles: abstract classes, inheritance, encapsulation, records, file I/O, and polymorphism.

## Features

- **Account management**: create checking and savings accounts with unique IDs
- **Deposit / Withdraw / Transfer**: perform financial transactions with validation
- **Interest calculation**: savings accounts earn 4.5% annual interest (applied monthly)
- **Transaction history**: view all transactions for any account
- **File persistence**: accounts and transactions are saved to disk automatically
- **Console UI**: menu-driven interface

## Architecture

```
com.example.bank/
├── model/
│   ├── Account.java            (abstract base class)
│   ├── CheckingAccount.java    (checking — no interest)
│   ├── SavingsAccount.java     (savings — earns interest)
│   └── Transaction.java        (immutable record)
├── service/
│   └── BankService.java        (business logic layer)
├── repository/
│   └── AccountRepository.java  (file-based persistence)
└── Main.java                   (entry point with menu)
```

## How to compile and run

```bash
javac com/example/bank/model/*.java \
       com/example/bank/service/*.java \
       com/example/bank/repository/*.java \
       com/example/bank/Main.java

java com.example.bank.Main
```

## Menu options

| Option | Description                         |
|--------|-------------------------------------|
| 1      | Create a new account                |
| 2      | Deposit money                       |
| 3      | Withdraw money                      |
| 4      | Transfer between accounts           |
| 5      | Check account balance               |
| 6      | View transaction history            |
| 7      | Apply monthly interest (savings)    |
| 8      | List all accounts                   |
| 0      | Exit                                |
