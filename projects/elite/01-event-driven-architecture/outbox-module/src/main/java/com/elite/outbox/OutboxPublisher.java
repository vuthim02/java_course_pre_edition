package com.elite.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository repository;
    private final OutboxEventHandler eventHandler;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public OutboxPublisher(OutboxRepository repository, OutboxEventHandler eventHandler) {
        this.repository = repository;
        this.eventHandler = eventHandler;
    }

    public void start(int intervalSeconds) {
        scheduler.scheduleAtFixedRate(this::publishPending, 0, intervalSeconds, TimeUnit.SECONDS);
        log.info("OutboxPublisher started with interval {}s", intervalSeconds);
    }

    public void stop() {
        scheduler.shutdown();
        log.info("OutboxPublisher stopped");
    }

    private void publishPending() {
        var pending = repository.findPending();
        for (Outbox outbox : pending) {
            try {
                eventHandler.handle(outbox);
                repository.markProcessed(outbox.getId());
                log.debug("Published outbox event {} to broker", outbox.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}: {}", outbox.getId(), e.getMessage());
                repository.incrementRetry(outbox.getId());
            }
        }
    }

    public interface OutboxEventHandler {
        void handle(Outbox outbox) throws Exception;
    }
}
