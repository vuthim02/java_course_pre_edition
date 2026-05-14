# Banking System — Complete Java Source Code

All classes belong to the `com.example.bank` package.

---

## `com.example.bank.model.Account` (abstract base class)

File: `com/example/bank/model/Account.java`

```java
package com.example.bank.model;

import java.util.Objects;

/**
 * Abstract base class for all bank accounts.
 * Provides shared state (accountId, ownerName, balance) and behavior
 * (deposit, withdraw) that subclasses inherit and extend.
 */
public abstract class Account {
    private final String accountId;
    private final String ownerName;
    private double balance;

    public Account(String accountId, String ownerName, double initialBalance) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.ownerName = Objects.requireNonNull(ownerName, "ownerName must not be null");
        if (initialBalance < 0) {
            throw new IllegalArgumentException("Initial balance cannot be negative");
        }
        this.balance = initialBalance;
    }

    // --- Getters ---
    public String getAccountId()     { return accountId; }
    public String getOwnerName()     { return ownerName; }
    public double getBalance()       { return balance; }

    // --- Core operations ---
    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive");
        if (amount > balance) throw new IllegalArgumentException("Insufficient funds");
        balance -= amount;
    }

    /** Each subclass returns its own type name */
    public abstract String getAccountType();

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%.2f",
                accountId, getAccountType(), ownerName, balance);
    }

    // --- File persistence helpers ---

    /** Serializes this account to a pipe-delimited string */
    public String toFileString() {
        return accountId + "|" + ownerName + "|" + balance + "|" + getClass().getSimpleName();
    }

    /** Deserializes an account from a pipe-delimited string */
    public static Account fromFileString(String line) {
        String[] parts = line.split("\\|");
        String id   = parts[0];
        String name = parts[1];
        double bal  = Double.parseDouble(parts[2]);
        String type = parts[3];
        if ("CheckingAccount".equals(type)) {
            return new CheckingAccount(id, name, bal);
        } else {
            return new SavingsAccount(id, name, bal);
        }
    }
}
```

---

## `com.example.bank.model.CheckingAccount`

File: `com/example/bank/model/CheckingAccount.java`

```java
package com.example.bank.model;

/**
 * A checking account with no interest.
 * Inherits deposit/withdraw behavior from Account.
 */
public class CheckingAccount extends Account {

    public CheckingAccount(String accountId, String ownerName, double initialBalance) {
        super(accountId, ownerName, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "Checking";
    }
}
```

---

## `com.example.bank.model.SavingsAccount`

File: `com/example/bank/model/SavingsAccount.java`

```java
package com.example.bank.model;

/**
 * A savings account that earns monthly interest.
 * Interest rate: 4.5% annual, compounded monthly.
 */
public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.045; // 4.5 % annual

    public SavingsAccount(String accountId, String ownerName, double initialBalance) {
        super(accountId, ownerName, initialBalance);
    }

    @Override
    public String getAccountType() {
        return "Savings";
    }

    /** Calculates one month's interest */
    public double calculateMonthlyInterest() {
        return getBalance() * (INTEREST_RATE / 12);
    }

    /** Applies monthly interest by depositing the earned amount */
    public void applyMonthlyInterest() {
        deposit(calculateMonthlyInterest());
    }

    public static double getInterestRate() {
        return INTEREST_RATE;
    }
}
```

---

## `com.example.bank.model.Transaction` (record)

File: `com/example/bank/model/Transaction.java`

```java
package com.example.bank.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable record representing a single financial transaction.
 * Records automatically provide constructor, equals, hashCode, and accessors.
 */
public record Transaction(
    String transactionId,
    String accountId,
    String type,          // DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT, INTEREST
    double amount,
    double balanceAfter,
    LocalDateTime timestamp
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%.2f | Balance: $%.2f",
                timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                transactionId, type, amount, balanceAfter);
    }

    /** Serializes to pipe-delimited string for file storage */
    public String toFileString() {
        return String.join("|",
                transactionId, accountId, type,
                String.valueOf(amount),
                String.valueOf(balanceAfter),
                timestamp.format(FORMATTER));
    }

    /** Deserializes from pipe-delimited string */
    public static Transaction fromFileString(String line) {
        String[] parts = line.split("\\|");
        return new Transaction(
                parts[0], parts[1], parts[2],
                Double.parseDouble(parts[3]),
                Double.parseDouble(parts[4]),
                LocalDateTime.parse(parts[5], FORMATTER));
    }
}
```

---

## `com.example.bank.service.BankService`

File: `com/example/bank/service/BankService.java`

```java
package com.example.bank.service;

import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Business-logic layer that coordinates accounts and transactions.
 * All financial operations go through this service.
 */
public class BankService {
    private final AccountRepository repository;

    public BankService(AccountRepository repository) {
        this.repository = repository;
    }

    /** Creates a new account (checking or savings) with an initial deposit */
    public Account createAccount(String type, String ownerName, double initialDeposit) {
        String id = UUID.randomUUID().toString().substring(0, 8);
        Account account;
        if (type.equalsIgnoreCase("checking")) {
            account = new CheckingAccount(id, ownerName, initialDeposit);
        } else if (type.equalsIgnoreCase("savings")) {
            account = new SavingsAccount(id, ownerName, initialDeposit);
        } else {
            throw new IllegalArgumentException("Account type must be 'checking' or 'savings'");
        }
        repository.saveAccount(account);
        recordTransaction(account.getAccountId(), "DEPOSIT", initialDeposit, account.getBalance());
        return account;
    }

    /** Deposits money into an account */
    public void deposit(String accountId, double amount) {
        Account account = findAccount(accountId);
        account.deposit(amount);
        repository.saveAccount(account);
        recordTransaction(accountId, "DEPOSIT", amount, account.getBalance());
    }

    /** Withdraws money from an account */
    public void withdraw(String accountId, double amount) {
        Account account = findAccount(accountId);
        account.withdraw(amount);
        repository.saveAccount(account);
        recordTransaction(accountId, "WITHDRAWAL", amount, account.getBalance());
    }

    /** Transfers money between two accounts */
    public void transfer(String fromId, String toId, double amount) {
        Account from = findAccount(fromId);
        Account to   = findAccount(toId);
        from.withdraw(amount);
        to.deposit(amount);
        repository.saveAccount(from);
        repository.saveAccount(to);
        recordTransaction(fromId, "TRANSFER_OUT", amount, from.getBalance());
        recordTransaction(toId,   "TRANSFER_IN",  amount, to.getBalance());
    }

    /** Applies monthly interest to all savings accounts */
    public void applyMonthlyInterest() {
        for (Account acc : repository.getAllAccounts()) {
            if (acc instanceof SavingsAccount savings) {
                double interest = savings.calculateMonthlyInterest();
                savings.applyMonthlyInterest();
                repository.saveAccount(savings);
                recordTransaction(savings.getAccountId(), "INTEREST", interest, savings.getBalance());
            }
        }
    }

    /** Returns full transaction history for an account */
    public List<Transaction> getTransactionHistory(String accountId) {
        return repository.getTransactions(accountId);
    }

    /** Finds an account or throws */
    public Account findAccount(String accountId) {
        Account acc = repository.getAccount(accountId);
        if (acc == null) throw new IllegalArgumentException("Account not found: " + accountId);
        return acc;
    }

    /** Lists all known accounts */
    public List<Account> getAllAccounts() {
        return repository.getAllAccounts();
    }

    // --- Private helpers ---

    private void recordTransaction(String accountId, String type,
                                   double amount, double balanceAfter) {
        Transaction txn = new Transaction(
                UUID.randomUUID().toString().substring(0, 8),
                accountId, type, amount, balanceAfter, LocalDateTime.now());
        repository.saveTransaction(txn);
    }
}
```

---

## `com.example.bank.repository.AccountRepository`

File: `com/example/bank/repository/AccountRepository.java`

```java
package com.example.bank.repository;

import com.example.bank.model.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * File-based persistence layer for accounts and transactions.
 * Data is stored under a bank_data/ directory:
 *   - bank_data/accounts.txt          — all accounts (pipe-delimited)
 *   - bank_data/transactions/<id>.txt — transactions per account
 */
public class AccountRepository {
    private static final Path DATA_DIR          = Path.of("bank_data");
    private static final Path ACCOUNTS_FILE     = DATA_DIR.resolve("accounts.txt");
    private static final Path TRANSACTIONS_DIR  = DATA_DIR.resolve("transactions");

    private final Map<String, Account> accountCache = new ConcurrentHashMap<>();

    public AccountRepository() {
        try {
            Files.createDirectories(DATA_DIR);
            Files.createDirectories(TRANSACTIONS_DIR);
            loadAccounts();
        } catch (IOException e) {
            System.err.println("Warning: could not initialize data directory: " + e.getMessage());
        }
    }

    // --- Account persistence ---

    /** Saves an account (insert or update) */
    public void saveAccount(Account account) {
        accountCache.put(account.getAccountId(), account);
        persistAccounts();
    }

    /** Retrieves a single account by ID */
    public Account getAccount(String accountId) {
        return accountCache.get(accountId);
    }

    /** Returns all accounts */
    public List<Account> getAllAccounts() {
        return List.copyOf(accountCache.values());
    }

    /** Writes all accounts to the file */
    private void persistAccounts() {
        try (BufferedWriter writer = Files.newBufferedWriter(ACCOUNTS_FILE)) {
            for (Account acc : accountCache.values()) {
                writer.write(acc.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving accounts: " + e.getMessage());
        }
    }

    /** Loads all accounts from the file into the cache */
    private void loadAccounts() {
        if (!Files.exists(ACCOUNTS_FILE)) return;
        try (BufferedReader reader = Files.newBufferedReader(ACCOUNTS_FILE)) {
            reader.lines()
                    .filter(l -> !l.isBlank())
                    .map(Account::fromFileString)
                    .forEach(a -> accountCache.put(a.getAccountId(), a));
        } catch (IOException e) {
            System.err.println("Error loading accounts: " + e.getMessage());
        }
    }

    // --- Transaction persistence ---

    /** Appends a transaction to the per-account transaction file */
    public void saveTransaction(Transaction transaction) {
        Path file = TRANSACTIONS_DIR.resolve(transaction.accountId() + ".txt");
        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(transaction.toFileString());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error saving transaction: " + e.getMessage());
        }
    }

    /** Reads all transactions for a given account */
    public List<Transaction> getTransactions(String accountId) {
        Path file = TRANSACTIONS_DIR.resolve(accountId + ".txt");
        if (!Files.exists(file)) return List.of();
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            return reader.lines()
                    .filter(l -> !l.isBlank())
                    .map(Transaction::fromFileString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            System.err.println("Error reading transactions: " + e.getMessage());
            return List.of();
        }
    }
}
```

---

## `com.example.bank.Main`

File: `com/example/bank/Main.java`

```java
package com.example.bank;

import com.example.bank.model.*;
import com.example.bank.repository.AccountRepository;
import com.example.bank.service.BankService;
import java.util.List;
import java.util.Scanner;

/**
 * Console-based entry point for the Banking System.
 * Provides a menu-driven UI that interacts with BankService.
 */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final BankService bankService =
            new BankService(new AccountRepository());

    public static void main(String[] args) {
        System.out.println("========== BANKING SYSTEM ==========");
        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> createAccount();
                    case "2" -> deposit();
                    case "3" -> withdraw();
                    case "4" -> transfer();
                    case "5" -> showBalance();
                    case "6" -> transactionHistory();
                    case "7" -> applyInterest();
                    case "8" -> listAccounts();
                    case "0" -> {
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // --- Menu display ---

    private static void showMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1. Create account");
        System.out.println("2. Deposit");
        System.out.println("3. Withdraw");
        System.out.println("4. Transfer");
        System.out.println("5. Check balance");
        System.out.println("6. Transaction history");
        System.out.println("7. Apply interest (savings)");
        System.out.println("8. List all accounts");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    // --- Menu actions ---

    private static void createAccount() {
        System.out.print("Account type (checking/savings): ");
        String type = scanner.nextLine().trim().toLowerCase();
        System.out.print("Owner name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Initial deposit: ");
        double deposit = Double.parseDouble(scanner.nextLine().trim());
        Account acc = bankService.createAccount(type, name, deposit);
        System.out.println("Created: " + acc);
    }

    private static void deposit() {
        System.out.print("Account ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Amount: ");
        double amt = Double.parseDouble(scanner.nextLine().trim());
        bankService.deposit(id, amt);
        System.out.println("Deposit successful. New balance: $"
                + String.format("%.2f", bankService.findAccount(id).getBalance()));
    }

    private static void withdraw() {
        System.out.print("Account ID: ");
        String id = scanner.nextLine().trim();
        System.out.print("Amount: ");
        double amt = Double.parseDouble(scanner.nextLine().trim());
        bankService.withdraw(id, amt);
        System.out.println("Withdrawal successful. New balance: $"
                + String.format("%.2f", bankService.findAccount(id).getBalance()));
    }

    private static void transfer() {
        System.out.print("From account ID: ");
        String from = scanner.nextLine().trim();
        System.out.print("To account ID: ");
        String to   = scanner.nextLine().trim();
        System.out.print("Amount: ");
        double amt  = Double.parseDouble(scanner.nextLine().trim());
        bankService.transfer(from, to, amt);
        System.out.println("Transfer successful.");
    }

    private static void showBalance() {
        System.out.print("Account ID: ");
        String id = scanner.nextLine().trim();
        Account acc = bankService.findAccount(id);
        System.out.println(acc);
    }

    private static void transactionHistory() {
        System.out.print("Account ID: ");
        String id = scanner.nextLine().trim();
        List<Transaction> txns = bankService.getTransactionHistory(id);
        if (txns.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.println("--- Transaction History ---");
        txns.forEach(System.out::println);
    }

    private static void applyInterest() {
        bankService.applyMonthlyInterest();
        System.out.println("Monthly interest applied to all savings accounts.");
    }

    private static void listAccounts() {
        List<Account> accounts = bankService.getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("No accounts.");
            return;
        }
        System.out.println("--- All Accounts ---");
        accounts.forEach(System.out::println);
    }
}
```
