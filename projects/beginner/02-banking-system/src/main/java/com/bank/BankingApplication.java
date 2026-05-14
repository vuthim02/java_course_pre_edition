package com.bank;

import com.bank.model.*;
import com.bank.repository.AccountRepository;
import com.bank.service.BankService;
import com.bank.service.TransactionService;
import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;

public class BankingApplication {
    private static final TransactionService transactionService = new TransactionService();
    private static final BankService bank = new BankService(transactionService);
    private static final AccountRepository repository = new AccountRepository();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            var data = repository.loadAll();
            bank.setAccounts(data.accounts());
            transactionService.setAllTransactions(data.transactions());
            System.out.println("Loaded " + data.accounts().size() + " account(s) from disk.");
        } catch (Exception e) {
            System.out.println("No existing data found, starting fresh.");
        }

        System.out.println("=== Banking System ===");
        System.out.println("Commands: create, deposit, withdraw, transfer, history, statement, maintenance, list, exit");

        while (true) {
            System.out.print("\n> ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            try {
                switch (cmd) {
                    case "create" -> { createAccount(); save(); }
                    case "deposit" -> { deposit(); save(); }
                    case "withdraw" -> { withdraw(); save(); }
                    case "transfer" -> { transfer(); save(); }
                    case "history" -> history();
                    case "statement" -> statement();
                    case "maintenance" -> {
                        bank.applyMonthlyMaintenance();
                        save();
                        System.out.println("Monthly maintenance applied.");
                    }
                    case "list" -> bank.getAllAccounts().forEach(a ->
                        System.out.printf("%s | %s | %s | $%.2f%n",
                            a.getId(), a.getOwnerName(), a.getType(), a.getBalance()));
                    case "exit" -> { System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Unknown command");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void save() {
        try {
            repository.saveAll(bank.getAllAccountsMap(),
                transactionService.getAllTransactions());
        } catch (IOException e) {
            System.out.println("Warning: Could not save data: " + e.getMessage());
        }
    }

    private static void createAccount() {
        System.out.print("Owner name: ");
        String name = scanner.nextLine();
        System.out.print("Account type (CHECKING/SAVINGS): ");
        AccountType type = AccountType.valueOf(scanner.nextLine().toUpperCase());
        System.out.print("Initial deposit: $");
        double deposit = Double.parseDouble(scanner.nextLine());
        Account account = bank.createAccount(name, type, deposit);
        System.out.println("Created account: " + account.getId());
    }

    private static void deposit() {
        System.out.print("Account ID: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Amount: $");
        bank.deposit(id, Double.parseDouble(scanner.nextLine()));
        System.out.println("Deposit successful. Balance: $" + bank.getAccount(id).getBalance());
    }

    private static void withdraw() {
        System.out.print("Account ID: ");
        UUID id = UUID.fromString(scanner.nextLine());
        System.out.print("Amount: $");
        bank.withdraw(id, Double.parseDouble(scanner.nextLine()));
        System.out.println("Withdrawal successful. Balance: $" + bank.getAccount(id).getBalance());
    }

    private static void transfer() {
        System.out.print("From account ID: ");
        UUID from = UUID.fromString(scanner.nextLine());
        System.out.print("To account ID: ");
        UUID to = UUID.fromString(scanner.nextLine());
        System.out.print("Amount: $");
        bank.transfer(from, to, Double.parseDouble(scanner.nextLine()));
        System.out.println("Transfer successful.");
    }

    private static void history() {
        System.out.print("Account ID: ");
        UUID id = UUID.fromString(scanner.nextLine());
        var txs = transactionService.getTransactionHistory(id);
        if (txs.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        txs.forEach(t ->
            System.out.printf("[%s] %s: $%.2f (before: $%.2f, after: $%.2f) \u2014 %s%n",
                t.timestamp(), t.type(), t.amount(),
                t.balanceBefore(), t.balanceAfter(), t.description()));
    }

    private static void statement() {
        System.out.print("Account ID: ");
        UUID id = UUID.fromString(scanner.nextLine());
        Account account = bank.getAccount(id);
        System.out.println(transactionService.generateStatement(id, account.getOwnerName()));
    }
}
