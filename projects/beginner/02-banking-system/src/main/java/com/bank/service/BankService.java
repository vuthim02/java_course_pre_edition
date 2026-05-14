package com.bank.service;

import com.bank.model.*;
import java.util.*;

public class BankService {
    private final Map<UUID, Account> accounts = new HashMap<>();
    private final TransactionService transactionService;

    public BankService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public Account createAccount(String ownerName, AccountType type, double initialDeposit) {
        if (initialDeposit < 0) throw new IllegalArgumentException("Initial deposit must be >= 0");
        Account account = switch (type) {
            case CHECKING -> new CheckingAccount(ownerName, initialDeposit);
            case SAVINGS -> new SavingsAccount(ownerName, initialDeposit);
        };
        accounts.put(account.getId(), account);
        if (initialDeposit > 0) {
            transactionService.recordTransaction(account.getId(), TransactionType.DEPOSIT,
                initialDeposit, 0, initialDeposit, "Initial deposit");
        }
        return account;
    }

    public void deposit(UUID accountId, double amount) {
        Account account = getAccount(accountId);
        double before = account.getBalance();
        account.deposit(amount);
        transactionService.recordTransaction(accountId, TransactionType.DEPOSIT,
            amount, before, account.getBalance(), "Deposit");
    }

    public void withdraw(UUID accountId, double amount) {
        Account account = getAccount(accountId);
        double before = account.getBalance();
        account.withdraw(amount);
        transactionService.recordTransaction(accountId, TransactionType.WITHDRAWAL,
            amount, before, account.getBalance(), "Withdrawal");
    }

    public void transfer(UUID fromId, UUID toId, double amount) {
        Account from = getAccount(fromId);
        Account to = getAccount(toId);
        double beforeFrom = from.getBalance();
        from.withdraw(amount);
        double beforeTo = to.getBalance();
        to.deposit(amount);
        transactionService.recordTransaction(fromId, TransactionType.TRANSFER_OUT,
            amount, beforeFrom, from.getBalance(), "Transfer to " + toId);
        transactionService.recordTransaction(toId, TransactionType.TRANSFER_IN,
            amount, beforeTo, to.getBalance(), "Transfer from " + fromId);
    }

    public void applyMonthlyMaintenance() {
        for (Account account : accounts.values()) {
            double before = account.getBalance();
            account.applyMonthlyMaintenance();
            double after = account.getBalance();
            if (Double.compare(before, after) != 0) {
                double diff = after - before;
                TransactionType tType = diff > 0 ? TransactionType.INTEREST : TransactionType.WITHDRAWAL;
                String desc = diff > 0 ? "Monthly interest" : "Monthly maintenance fee";
                transactionService.recordTransaction(account.getId(), tType,
                    Math.abs(diff), before, after, desc);
            }
        }
    }

    public Account getAccount(UUID id) {
        Account account = accounts.get(id);
        if (account == null) throw new NoSuchElementException("Account not found: " + id);
        return account;
    }

    public List<Account> getAllAccounts() { return List.copyOf(accounts.values()); }

    public Map<UUID, Account> getAllAccountsMap() { return accounts; }

    public void setAccounts(Map<UUID, Account> loaded) {
        accounts.clear();
        if (loaded != null) {
            accounts.putAll(loaded);
        }
    }
}
