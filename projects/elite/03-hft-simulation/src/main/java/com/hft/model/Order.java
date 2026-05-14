package com.hft.model;

public record Order(
    long orderId,
    int symbolId,
    Side side,
    long price,
    long quantity,
    long filledQuantity,
    OrderType type,
    long timestampNanos,
    OrderStatus status
) {
    public long remainingQuantity() { return quantity - filledQuantity; }
    public boolean isFilled() { return filledQuantity >= quantity; }
}
