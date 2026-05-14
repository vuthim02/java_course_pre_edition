package com.bank.model;

public class CheckingAccount extends Account {

    public CheckingAccount(String ownerName, double initialDeposit) {
        super(ownerName, AccountType.CHECKING, initialDeposit);
    }

    @Override
    public void applyMonthlyMaintenance() {
        if (getBalance() < 500) {
            decreaseBalance(5.0);
        }
    }

    @Override
    public String getAccountTypeDisplay() {
        return "Checking";
    }
}
