package com.elite.es.command;

public class ShipOrderCommand {
    private final String orderId;
    private final String trackingNumber;

    public ShipOrderCommand(String orderId, String trackingNumber) {
        this.orderId = orderId;
        this.trackingNumber = trackingNumber;
    }

    public String getOrderId() { return orderId; }
    public String getTrackingNumber() { return trackingNumber; }
}
