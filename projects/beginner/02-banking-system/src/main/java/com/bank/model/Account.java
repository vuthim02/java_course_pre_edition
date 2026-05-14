package com.bank.model;

import java.io.Serializable;
import java.util.UUID;

public abstract class Account implements Serializable {
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String ownerName;
    private final AccountType type;
    private double balance;
    private boolean active;

    protected Account(String ownerName, AccountType type, double initialDeposit) {
        this.id = UUID.randomUUID();
        this.ownerName = ownerName;
        this.type = type;
        this.balance = initialDeposit;
        this.active = true;
    }

    public synchronized void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit must be positive");
        balance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be positive");
        if (amount > balance) throw new IllegalStateException("Insufficient funds");
        balance -= amount;
    }

    protected synchronized void decreaseBalance(double amount) {
        balance -= amount;
    }

    public UUID getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public AccountType getType() { return type; }
    public double getBalance() { return balance; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public abstract void applyMonthlyMaintenance();
    public abstract String getAccountTypeDisplay();
}
