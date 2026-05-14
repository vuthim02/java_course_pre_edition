package com.elite.outbox;

import java.time.Instant;
import java.util.UUID;

public class Outbox {
    private String id;
    private String aggregateId;
    private String aggregateType;
    private String eventType;
    private String payload;
    private Instant createdAt;
    private Instant processedAt;
    private int retryCount;
    private String status;

    public Outbox() {}

    public Outbox(String aggregateId, String aggregateType, String eventType, String payload) {
        this.id = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.retryCount = 0;
        this.status = "PENDING";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAggregateId() { return aggregateId; }
    public void setAggregateId(String aggregateId) { this.aggregateId = aggregateId; }
    public String getAggregateType() { return aggregateType; }
    public void setAggregateType(String aggregateType) { this.aggregateType = aggregateType; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getPayload() { return payload; }
    public void setPayload(String payload) { this.payload = payload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getProcessedAt() { return processedAt; }
    public void setProcessedAt(Instant processedAt) { this.processedAt = processedAt; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
