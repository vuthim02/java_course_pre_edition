package com.elite.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InventoryReservationStep implements SagaStep<OrderSagaContext> {
    private static final Logger log = LoggerFactory.getLogger(InventoryReservationStep.class);

    @Override
    public void execute(OrderSagaContext context) {
        log.info("Reserving inventory for order {}...", context.getOrderId());
        context.getItems().forEach(item ->
            log.info("  Reserved {} units of product {}", item.quantity(), item.productId()));
    }

    @Override
    public void compensate(OrderSagaContext context) {
        log.info("Releasing inventory reservation for order {}...", context.getOrderId());
        context.getItems().forEach(item ->
            log.info("  Released {} units of product {}", item.quantity(), item.productId()));
    }
}
