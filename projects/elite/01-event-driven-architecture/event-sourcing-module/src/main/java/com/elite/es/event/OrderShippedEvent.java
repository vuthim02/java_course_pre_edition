package com.elite.es.event;

import java.time.Instant;

public class OrderShippedEvent {
    private final String eventId;
    private final String orderId;
    private final String trackingNumber;
    private final Instant timestamp;
    private final long version;

    public OrderShippedEvent(String eventId, String orderId, String trackingNumber,
                             Instant timestamp, long version) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getEventId() { return eventId; }
    public String getOrderId() { return orderId; }
    public String getTrackingNumber() { return trackingNumber; }
    public Instant getTimestamp() { return timestamp; }
    public long getVersion() { return version; }
}
