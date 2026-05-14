package com.bank.service;

import com.bank.model.Transaction;
import com.bank.model.TransactionType;
import java.time.Instant;
import java.util.*;

public class TransactionService {
    private final Map<UUID, List<Transaction>> transactions = new HashMap<>();

    public synchronized void recordTransaction(UUID accountId, TransactionType type,
                                                double amount, double balanceBefore,
                                                double balanceAfter, String description) {
        Transaction t = new Transaction(accountId, type, amount, balanceBefore,
                                         balanceAfter, description);
        transactions.computeIfAbsent(accountId, k -> new ArrayList<>()).add(t);
    }

    public synchronized List<Transaction> getTransactionHistory(UUID accountId) {
        return List.copyOf(transactions.getOrDefault(accountId, Collections.emptyList()));
    }

    public synchronized String generateStatement(UUID accountId, String ownerName) {
        List<Transaction> txs = transactions.getOrDefault(accountId, Collections.emptyList());
        StringBuilder sb = new StringBuilder();
        sb.append("=== Statement for ").append(ownerName)
          .append(" (").append(accountId).append(") ===\n");
        for (Transaction t : txs) {
            sb.append(String.format("[%s] %s: $%.2f (before: $%.2f, after: $%.2f) \u2014 %s%n",
                t.timestamp(), t.type(), t.amount(),
                t.balanceBefore(), t.balanceAfter(), t.description()));
        }
        return sb.toString();
    }

    public synchronized Map<UUID, List<Transaction>> getAllTransactions() {
        return transactions;
    }

    public synchronized void setAllTransactions(Map<UUID, List<Transaction>> txs) {
        transactions.clear();
        if (txs != null) {
            transactions.putAll(txs);
        }
    }
}
