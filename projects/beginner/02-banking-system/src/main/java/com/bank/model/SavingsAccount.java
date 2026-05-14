package com.bank.model;

public class SavingsAccount extends Account {
    private static final double INTEREST_RATE = 0.025;

    public SavingsAccount(String ownerName, double initialDeposit) {
        super(ownerName, AccountType.SAVINGS, initialDeposit);
    }

    @Override
    public void applyMonthlyMaintenance() {
        if (getBalance() > 0) {
            double interest = getBalance() * INTEREST_RATE / 12;
            deposit(interest);
        }
    }

    @Override
    public String getAccountTypeDisplay() {
        return "Savings";
    }
}
