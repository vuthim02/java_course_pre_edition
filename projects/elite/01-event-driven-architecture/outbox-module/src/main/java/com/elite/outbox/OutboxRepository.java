package com.elite.outbox;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class OutboxRepository {
    private final List<Outbox> store = new CopyOnWriteArrayList<>();

    public void save(Outbox outbox) {
        store.add(outbox);
    }

    public List<Outbox> findPending() {
        return store.stream()
            .filter(o -> "PENDING".equals(o.getStatus()))
            .collect(Collectors.toList());
    }

    public void markProcessed(String id) {
        store.stream()
            .filter(o -> o.getId().equals(id))
            .findFirst()
            .ifPresent(o -> {
                o.setStatus("PROCESSED");
                o.setProcessedAt(java.time.Instant.now());
            });
    }

    public void incrementRetry(String id) {
        store.stream()
            .filter(o -> o.getId().equals(id))
            .findFirst()
            .ifPresent(o -> {
                o.setRetryCount(o.getRetryCount() + 1);
                if (o.getRetryCount() >= 3) {
                    o.setStatus("FAILED");
                }
            });
    }

    public List<Outbox> findAll() {
        return List.copyOf(store);
    }
}
