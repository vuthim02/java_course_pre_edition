package com.elite.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderConfirmationStep implements SagaStep<OrderSagaContext> {
    private static final Logger log = LoggerFactory.getLogger(OrderConfirmationStep.class);

    @Override
    public void execute(OrderSagaContext context) {
        log.info("Confirming order {}...", context.getOrderId());
        log.info("Order {} confirmed successfully.", context.getOrderId());
    }

    @Override
    public void compensate(OrderSagaContext context) {
        log.info("Cancelling order {}...", context.getOrderId());
    }
}
