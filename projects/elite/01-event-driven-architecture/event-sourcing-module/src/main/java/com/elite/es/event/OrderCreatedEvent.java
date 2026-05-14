package com.elite.es.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderCreatedEvent {
    private final String eventId;
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final String currency;
    private final Instant timestamp;
    private final long version;

    public OrderCreatedEvent(String eventId, String orderId, String customerId,
                             List<OrderItem> items, BigDecimal totalAmount,
                             String currency, Instant timestamp, long version) {
        this.eventId = eventId;
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.timestamp = timestamp;
        this.version = version;
    }

    public String getEventId() { return eventId; }
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public Instant getTimestamp() { return timestamp; }
    public long getVersion() { return version; }

    public static record OrderItem(String productId, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {}
}
