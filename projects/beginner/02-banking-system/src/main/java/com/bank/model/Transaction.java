package com.bank.model;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

public record Transaction(UUID id, UUID accountId, TransactionType type, double amount,
                          double balanceBefore, double balanceAfter, Instant timestamp,
                          String description) implements Serializable {

    private static final long serialVersionUID = 1L;

    public Transaction(UUID accountId, TransactionType type, double amount,
                       double balanceBefore, double balanceAfter, String description) {
        this(UUID.randomUUID(), accountId, type, amount, balanceBefore, balanceAfter,
             Instant.now(), description);
    }
}
