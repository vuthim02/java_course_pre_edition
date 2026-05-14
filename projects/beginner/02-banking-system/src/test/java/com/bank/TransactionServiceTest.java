package com.bank;

import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import com.bank.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {

    private TransactionService transactionService;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService();
        accountId = UUID.randomUUID();
    }

    @Nested
    class RecordTransaction {

        @Test
        void testRecordTransaction() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Initial deposit");

            List<Transaction> history = transactionService.getTransactionHistory(accountId);
            assertEquals(1, history.size());
            Transaction t = history.get(0);
            assertEquals(accountId, t.accountId());
            assertEquals(TransactionType.DEPOSIT, t.type());
            assertEquals(100.0, t.amount());
            assertEquals(0.0, t.balanceBefore());
            assertEquals(100.0, t.balanceAfter());
            assertEquals("Initial deposit", t.description());
        }

        @Test
        void testRecordMultipleTransactions() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "First");
            transactionService.recordTransaction(accountId, TransactionType.WITHDRAWAL, 30.0, 100.0, 70.0, "Second");

            List<Transaction> history = transactionService.getTransactionHistory(accountId);
            assertEquals(2, history.size());
        }

        @Test
        void testTransactionHasTimestamp() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Deposit");

            Transaction t = transactionService.getTransactionHistory(accountId).get(0);
            assertNotNull(t.timestamp());
        }

        @Test
        void testTransactionHasUniqueId() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "First");
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 200.0, 100.0, 300.0, "Second");

            List<Transaction> history = transactionService.getTransactionHistory(accountId);
            assertNotEquals(history.get(0).id(), history.get(1).id());
        }
    }

    @Nested
    class GetHistory {

        @Test
        void testGetHistory_EmptyForUnknownAccount() {
            List<Transaction> history = transactionService.getTransactionHistory(UUID.randomUUID());
            assertTrue(history.isEmpty());
        }

        @Test
        void testGetHistory_ReturnsImmutableCopy() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Test");

            List<Transaction> history = transactionService.getTransactionHistory(accountId);
            assertThrows(UnsupportedOperationException.class, () -> history.add(null));
        }

        @Test
        void testGetHistory_MultipleAccounts() {
            UUID otherId = UUID.randomUUID();
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "A");
            transactionService.recordTransaction(otherId, TransactionType.DEPOSIT, 200.0, 0.0, 200.0, "B");

            assertEquals(1, transactionService.getTransactionHistory(accountId).size());
            assertEquals(1, transactionService.getTransactionHistory(otherId).size());
        }
    }

    @Nested
    class GenerateStatement {

        @Test
        void testGenerateStatementWithTransactions() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 500.0, 0.0, 500.0, "Opening");

            String statement = transactionService.generateStatement(accountId, "Alice");

            assertTrue(statement.contains("Alice"));
            assertTrue(statement.contains(accountId.toString()));
            assertTrue(statement.contains("DEPOSIT"));
            assertTrue(statement.contains("500.00"));
            assertTrue(statement.contains("Opening"));
        }

        @Test
        void testGenerateStatement_EmptyHistory() {
            String statement = transactionService.generateStatement(accountId, "Bob");

            assertTrue(statement.contains("Bob"));
            assertTrue(statement.contains(accountId.toString()));
        }

        @Test
        void testGenerateStatement_WithMultipleTransactions() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Deposit");
            transactionService.recordTransaction(accountId, TransactionType.WITHDRAWAL, 25.0, 100.0, 75.0, "Withdrawal");

            String statement = transactionService.generateStatement(accountId, "Charlie");

            assertTrue(statement.contains("DEPOSIT"));
            assertTrue(statement.contains("WITHDRAWAL"));
        }

        @Test
        void testGenerateStatement_FormatDollarAmounts() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 99.5, 0.0, 99.5, "Test");

            String statement = transactionService.generateStatement(accountId, "Alice");
            assertTrue(statement.contains("$99.50"));
        }
    }

    @Nested
    class GetAllTransactions {

        @Test
        void testGetAllTransactions() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Test");

            assertFalse(transactionService.getAllTransactions().isEmpty());
            assertEquals(1, transactionService.getAllTransactions().size());
        }

        @Test
        void testSetAllTransactions() {
            transactionService.recordTransaction(accountId, TransactionType.DEPOSIT, 100.0, 0.0, 100.0, "Test");
            transactionService.setAllTransactions(null);
            assertTrue(transactionService.getAllTransactions().isEmpty());
        }
    }
}
