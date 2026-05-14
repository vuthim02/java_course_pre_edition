package com.elite.es.aggregate;

import com.elite.es.command.CreateOrderCommand;
import com.elite.es.command.ShipOrderCommand;
import com.elite.es.event.OrderCreatedEvent;
import com.elite.es.event.OrderShippedEvent;
import com.elite.es.store.EventStore;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class OrderAggregate {
    private final EventStore eventStore;
    private String orderId;
    private String customerId;
    private BigDecimal totalAmount;
    private String currency;
    private String status;
    private long version;

    public OrderAggregate(EventStore eventStore) {
        this.eventStore = eventStore;
        this.version = 0;
    }

    public String getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public long getVersion() { return version; }

    public void handleCreateOrder(CreateOrderCommand cmd) {
        BigDecimal total = cmd.getItems().stream()
            .map(item -> item.unitPrice().multiply(BigDecimal.valueOf(item.quantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        List<OrderCreatedEvent.OrderItem> items = cmd.getItems().stream()
            .map(i -> new OrderCreatedEvent.OrderItem(i.productId(), i.quantity(), i.unitPrice(),
                    i.unitPrice().multiply(BigDecimal.valueOf(i.quantity()))))
            .toList();
        OrderCreatedEvent event = new OrderCreatedEvent(
            UUID.randomUUID().toString(), cmd.getOrderId(), cmd.getCustomerId(),
            items, total, cmd.getCurrency(), Instant.now(), version + 1);
        apply(event);
        eventStore.append(cmd.getOrderId(), event, version);
    }

    public void handleShipOrder(ShipOrderCommand cmd) {
        OrderShippedEvent event = new OrderShippedEvent(
            UUID.randomUUID().toString(), cmd.getOrderId(),
            cmd.getTrackingNumber(), Instant.now(), version + 1);
        apply(event);
        eventStore.append(cmd.getOrderId(), event, version);
    }

    public void loadFromHistory(String aggregateId) {
        List<Object> events = eventStore.readEvents(aggregateId);
        for (Object event : events) {
            apply(event);
        }
    }

    private void apply(Object event) {
        if (event instanceof OrderCreatedEvent e) {
            this.orderId = e.getOrderId();
            this.customerId = e.getCustomerId();
            this.totalAmount = e.getTotalAmount();
            this.currency = e.getCurrency();
            this.status = "CREATED";
            this.version = e.getVersion();
        } else if (event instanceof OrderShippedEvent e) {
            this.status = "SHIPPED";
            this.version = e.getVersion();
        }
    }
}
