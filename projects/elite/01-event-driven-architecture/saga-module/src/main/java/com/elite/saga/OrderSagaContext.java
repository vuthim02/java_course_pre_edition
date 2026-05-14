package com.elite.saga;

import java.math.BigDecimal;
import java.util.List;

public class OrderSagaContext {
    private String orderId;
    private String customerId;
    private List<OrderItem> items;
    private BigDecimal totalAmount;
    private String currency;
    private String paymentId;
    private String status;

    public OrderSagaContext(String orderId, String customerId, List<OrderItem> items,
                            BigDecimal totalAmount, String currency) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.items = items;
        this.totalAmount = totalAmount;
        this.currency = currency;
        this.status = "PENDING";
    }

    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getCurrency() { return currency; }
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static record OrderItem(String productId, int quantity, BigDecimal unitPrice) {}
}
