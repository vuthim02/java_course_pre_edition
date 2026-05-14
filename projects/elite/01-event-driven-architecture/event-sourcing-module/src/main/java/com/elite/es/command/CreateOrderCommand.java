package com.elite.es.command;

import java.math.BigDecimal;
import java.util.List;

public class CreateOrderCommand {
    private final String orderId;
    private final String customerId;
    private final List<OrderLineItem> items;
    private final String currency;

    public CreateOrderCommand(String orderId, String customerId, List<OrderLineItem> items, String currency) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.currency = currency;
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderLineItem> getItems() { return items; }
    public String getCurrency() { return currency; }

    public static record OrderLineItem(String productId, int quantity, BigDecimal unitPrice) {}
}
