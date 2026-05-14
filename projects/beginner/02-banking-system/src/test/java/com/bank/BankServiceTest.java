package com.bank;

import com.bank.model.*;
import com.bank.service.BankService;
import com.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceTest {

    @Mock
    private TransactionService transactionService;

    @Captor
    private ArgumentCaptor<UUID> accountIdCaptor;

    @Captor
    private ArgumentCaptor<TransactionType> typeCaptor;

    @Captor
    private ArgumentCaptor<Double> amountCaptor;

    private BankService bankService;

    @BeforeEach
    void setUp() {
        bankService = new BankService(transactionService);
    }

    @Nested
    class CreateAccount {

        @Test
        void testCreateCheckingAccount() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 1000.0);

            assertNotNull(account.getId());
            assertEquals("Alice", account.getOwnerName());
            assertEquals(AccountType.CHECKING, account.getType());
            assertEquals(1000.0, account.getBalance());
            assertTrue(account.isActive());
        }

        @Test
        void testCreateSavingsAccount() {
            Account account = bankService.createAccount("Bob", AccountType.SAVINGS, 500.0);

            assertNotNull(account.getId());
            assertEquals("Bob", account.getOwnerName());
            assertEquals(AccountType.SAVINGS, account.getType());
            assertEquals(500.0, account.getBalance());
        }

        @Test
        void testCreateAccountWithZeroDeposit() {
            Account account = bankService.createAccount("Charlie", AccountType.CHECKING, 0.0);

            assertEquals(0.0, account.getBalance());
            verify(transactionService, never()).recordTransaction(any(), any(), anyDouble(), anyDouble(), anyDouble(), anyString());
        }

        @Test
        void testCreateAccountWithNegativeDepositThrows() {
            assertThrows(IllegalArgumentException.class,
                    () -> bankService.createAccount("Bad", AccountType.CHECKING, -100.0));
        }

        @Test
        void testCreateAccountRecordsTransaction() {
            bankService.createAccount("Alice", AccountType.CHECKING, 1000.0);

            verify(transactionService).recordTransaction(any(UUID.class), eq(TransactionType.DEPOSIT),
                    eq(1000.0), eq(0.0), eq(1000.0), eq("Initial deposit"));
        }
    }

    @Nested
    class Deposit {

        @Test
        void testDepositIncreasesBalance() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 100.0);
            reset(transactionService);

            bankService.deposit(account.getId(), 50.0);

            assertEquals(150.0, bankService.getAccount(account.getId()).getBalance());
        }

        @Test
        void testDepositRecordsTransaction() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 100.0);
            reset(transactionService);

            bankService.deposit(account.getId(), 50.0);

            verify(transactionService).recordTransaction(eq(account.getId()), eq(TransactionType.DEPOSIT),
                    eq(50.0), eq(100.0), eq(150.0), eq("Deposit"));
        }

        @Test
        void testDepositToNonExistentAccountThrows() {
            assertThrows(NoSuchElementException.class,
                    () -> bankService.deposit(UUID.randomUUID(), 50.0));
        }
    }

    @Nested
    class Withdraw {

        @Test
        void testWithdrawDecreasesBalance() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 200.0);
            reset(transactionService);

            bankService.withdraw(account.getId(), 50.0);

            assertEquals(150.0, bankService.getAccount(account.getId()).getBalance());
        }

        @Test
        void testWithdrawRecordsTransaction() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 200.0);
            reset(transactionService);

            bankService.withdraw(account.getId(), 50.0);

            verify(transactionService).recordTransaction(eq(account.getId()), eq(TransactionType.WITHDRAWAL),
                    eq(50.0), eq(200.0), eq(150.0), eq("Withdrawal"));
        }

        @Test
        void testWithdrawInsufficientFundsThrows() {
            Account account = bankService.createAccount("Alice", AccountType.CHECKING, 10.0);
            reset(transactionService);

            assertThrows(IllegalStateException.class,
                    () -> bankService.withdraw(account.getId(), 50.0));
        }

        @Test
        void testWithdrawFromNonExistentAccountThrows() {
            assertThrows(NoSuchElementException.class,
                    () -> bankService.withdraw(UUID.randomUUID(), 50.0));
        }
    }

    @Nested
    class Transfer {

        @Test
        void testTransferMovesMoney() {
            Account from = bankService.createAccount("Alice", AccountType.CHECKING, 500.0);
            Account to = bankService.createAccount("Bob", AccountType.CHECKING, 100.0);
            reset(transactionService);

            bankService.transfer(from.getId(), to.getId(), 200.0);

            assertEquals(300.0, bankService.getAccount(from.getId()).getBalance());
            assertEquals(300.0, bankService.getAccount(to.getId()).getBalance());
        }

        @Test
        void testTransferRecordsTwoTransactions() {
            Account from = bankService.createAccount("Alice", AccountType.CHECKING, 500.0);
            Account to = bankService.createAccount("Bob", AccountType.CHECKING, 100.0);
            reset(transactionService);

            bankService.transfer(from.getId(), to.getId(), 200.0);

            verify(transactionService, times(2)).recordTransaction(any(), any(), anyDouble(), anyDouble(), anyDouble(), anyString());
        }

        @Test
        void testTransferInsufficientFundsThrows() {
            Account from = bankService.createAccount("Alice", AccountType.CHECKING, 50.0);
            Account to = bankService.createAccount("Bob", AccountType.CHECKING, 100.0);

            assertThrows(IllegalStateException.class,
                    () -> bankService.transfer(from.getId(), to.getId(), 200.0));
        }

        @Test
        void testTransferToNonExistentAccountThrows() {
            Account from = bankService.createAccount("Alice", AccountType.CHECKING, 500.0);

            assertThrows(NoSuchElementException.class,
                    () -> bankService.transfer(from.getId(), UUID.randomUUID(), 50.0));
        }
    }

    @Nested
    class InterestAndMaintenance {

        @Test
        void testApplyMonthlyMaintenanceOnSavings() {
            Account savings = bankService.createAccount("Alice", AccountType.SAVINGS, 1000.0);
            reset(transactionService);

            bankService.applyMonthlyMaintenance();

            double expectedInterest = 1000.0 * 0.025 / 12;
            assertEquals(1000.0 + expectedInterest, bankService.getAccount(savings.getId()).getBalance(), 0.001);
            verify(transactionService).recordTransaction(any(), eq(TransactionType.INTEREST), anyDouble(), anyDouble(), anyDouble(), anyString());
        }

        @Test
        void testApplyMonthlyMaintenanceOnCheckingBelow500() {
            Account checking = bankService.createAccount("Bob", AccountType.CHECKING, 400.0);
            reset(transactionService);

            bankService.applyMonthlyMaintenance();

            assertEquals(395.0, bankService.getAccount(checking.getId()).getBalance(), 0.001);
            verify(transactionService).recordTransaction(any(), eq(TransactionType.WITHDRAWAL), eq(5.0), anyDouble(), anyDouble(), anyString());
        }

        @Test
        void testApplyMonthlyMaintenanceOnCheckingAbove500() {
            Account checking = bankService.createAccount("Bob", AccountType.CHECKING, 600.0);
            reset(transactionService);

            bankService.applyMonthlyMaintenance();

            assertEquals(600.0, bankService.getAccount(checking.getId()).getBalance(), 0.001);
            verify(transactionService, never()).recordTransaction(any(), any(), anyDouble(), anyDouble(), anyDouble(), anyString());
        }

        @Test
        void testSavingsWithZeroBalanceNoInterest() {
            Account savings = bankService.createAccount("Zero", AccountType.SAVINGS, 0.0);
            reset(transactionService);

            bankService.applyMonthlyMaintenance();

            assertEquals(0.0, bankService.getAccount(savings.getId()).getBalance(), 0.001);
            verify(transactionService, never()).recordTransaction(any(), any(), anyDouble(), anyDouble(), anyDouble(), anyString());
        }
    }

    @Nested
    class GetNonExistentAccount {

        @Test
        void testGetNonExistentAccountThrows() {
            assertThrows(NoSuchElementException.class,
                    () -> bankService.getAccount(UUID.randomUUID()));
        }

        @Test
        void testGetAllAccountsReturnsCopy() {
            bankService.createAccount("Alice", AccountType.CHECKING, 100.0);
            bankService.createAccount("Bob", AccountType.SAVINGS, 200.0);

            assertEquals(2, bankService.getAllAccounts().size());
        }
    }
}
