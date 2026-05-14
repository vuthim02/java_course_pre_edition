package com.bank;

import com.bank.model.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    @Nested
    class CheckingAccountTest {

        @Test
        void testConstructor() {
            CheckingAccount account = new CheckingAccount("Alice", 1000.0);
            assertEquals("Alice", account.getOwnerName());
            assertEquals(AccountType.CHECKING, account.getType());
            assertEquals(1000.0, account.getBalance());
            assertTrue(account.isActive());
        }

        @Test
        void testDeposit() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            account.deposit(100.0);
            assertEquals(600.0, account.getBalance());
        }

        @Test
        void testDepositNegativeThrows() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            assertThrows(IllegalArgumentException.class, () -> account.deposit(-50.0));
        }

        @Test
        void testDepositZeroThrows() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            assertThrows(IllegalArgumentException.class, () -> account.deposit(0.0));
        }

        @Test
        void testWithdraw() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            account.withdraw(100.0);
            assertEquals(400.0, account.getBalance());
        }

        @Test
        void testWithdrawNegativeThrows() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            assertThrows(IllegalArgumentException.class, () -> account.withdraw(-50.0));
        }

        @Test
        void testWithdrawInsufficientFundsThrows() {
            CheckingAccount account = new CheckingAccount("Alice", 100.0);
            assertThrows(IllegalStateException.class, () -> account.withdraw(200.0));
        }

        @Test
        void testMaintenanceFee_Below500() {
            CheckingAccount account = new CheckingAccount("Alice", 400.0);
            account.applyMonthlyMaintenance();
            assertEquals(395.0, account.getBalance(), 0.001);
        }

        @Test
        void testMaintenanceFee_At500() {
            CheckingAccount account = new CheckingAccount("Alice", 500.0);
            account.applyMonthlyMaintenance();
            assertEquals(500.0, account.getBalance(), 0.001);
        }

        @Test
        void testMaintenanceFee_Above500() {
            CheckingAccount account = new CheckingAccount("Alice", 600.0);
            account.applyMonthlyMaintenance();
            assertEquals(600.0, account.getBalance(), 0.001);
        }

        @Test
        void testGetTypeDisplay() {
            CheckingAccount account = new CheckingAccount("Alice", 100.0);
            assertEquals("Checking", account.getAccountTypeDisplay());
        }

        @Test
        void testConstructorWithZeroDeposit() {
            CheckingAccount account = new CheckingAccount("Zero", 0.0);
            assertEquals(0.0, account.getBalance());
        }

        @Test
        void testSetActive() {
            CheckingAccount account = new CheckingAccount("Alice", 100.0);
            account.setActive(false);
            assertFalse(account.isActive());
        }
    }

    @Nested
    class SavingsAccountTest {

        @Test
        void testConstructor() {
            SavingsAccount account = new SavingsAccount("Bob", 2000.0);
            assertEquals("Bob", account.getOwnerName());
            assertEquals(AccountType.SAVINGS, account.getType());
            assertEquals(2000.0, account.getBalance());
            assertTrue(account.isActive());
        }

        @Test
        void testDeposit() {
            SavingsAccount account = new SavingsAccount("Bob", 1000.0);
            account.deposit(500.0);
            assertEquals(1500.0, account.getBalance());
        }

        @Test
        void testWithdraw() {
            SavingsAccount account = new SavingsAccount("Bob", 1000.0);
            account.withdraw(300.0);
            assertEquals(700.0, account.getBalance());
        }

        @Test
        void testApplyMonthlyInterest() {
            SavingsAccount account = new SavingsAccount("Bob", 1200.0);
            account.applyMonthlyMaintenance();
            double expectedInterest = 1200.0 * 0.025 / 12;
            assertEquals(1200.0 + expectedInterest, account.getBalance(), 0.001);
        }

        @Test
        void testInterestOnZeroBalance() {
            SavingsAccount account = new SavingsAccount("Bob", 0.0);
            account.applyMonthlyMaintenance();
            assertEquals(0.0, account.getBalance(), 0.001);
        }

        @Test
        void testConstructorWithZeroDeposit() {
            SavingsAccount account = new SavingsAccount("Zero", 0.0);
            assertEquals(0.0, account.getBalance());
        }

        @Test
        void testMultipleInterestApplications() {
            SavingsAccount account = new SavingsAccount("Bob", 1000.0);
            for (int i = 0; i < 12; i++) {
                account.applyMonthlyMaintenance();
            }
            assertTrue(account.getBalance() > 1000.0);
        }

        @Test
        void testGetTypeDisplay() {
            SavingsAccount account = new SavingsAccount("Bob", 100.0);
            assertEquals("Savings", account.getAccountTypeDisplay());
        }
    }
}
