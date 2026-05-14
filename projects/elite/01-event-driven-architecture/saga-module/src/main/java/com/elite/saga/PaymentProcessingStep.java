package com.elite.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

public class PaymentProcessingStep implements SagaStep<OrderSagaContext> {
    private static final Logger log = LoggerFactory.getLogger(PaymentProcessingStep.class);

    @Override
    public void execute(OrderSagaContext context) {
        log.info("Processing payment for order {}: {} {}", context.getOrderId(),
            context.getTotalAmount(), context.getCurrency());
        context.setPaymentId(UUID.randomUUID().toString());
        log.info("Payment completed: paymentId={}", context.getPaymentId());
    }

    @Override
    public void compensate(OrderSagaContext context) {
        if (context.getPaymentId() != null) {
            log.info("Refunding payment {} for order {}...", context.getPaymentId(), context.getOrderId());
        }
    }
}
