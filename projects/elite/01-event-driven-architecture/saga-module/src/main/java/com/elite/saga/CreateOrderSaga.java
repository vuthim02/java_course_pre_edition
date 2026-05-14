package com.elite.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateOrderSaga {
    private static final Logger log = LoggerFactory.getLogger(CreateOrderSaga.class);

    private final SagaStep<OrderSagaContext> reserveInventoryStep;
    private final SagaStep<OrderSagaContext> processPaymentStep;
    private final SagaStep<OrderSagaContext> confirmOrderStep;

    public CreateOrderSaga(SagaStep<OrderSagaContext> reserveInventoryStep,
                           SagaStep<OrderSagaContext> processPaymentStep,
                           SagaStep<OrderSagaContext> confirmOrderStep) {
        this.reserveInventoryStep = reserveInventoryStep;
        this.processPaymentStep = processPaymentStep;
        this.confirmOrderStep = confirmOrderStep;
    }

    public void execute(OrderSagaContext context) {
        try {
            reserveInventoryStep.execute(context);
            processPaymentStep.execute(context);
            confirmOrderStep.execute(context);
            context.setStatus("COMPLETED");
            log.info("Saga completed successfully for order {}", context.getOrderId());
        } catch (Exception e) {
            log.error("Saga failed for order {}, compensating...", context.getOrderId(), e);
            compensate(context);
            context.setStatus("COMPENSATED");
        }
    }

    private void compensate(OrderSagaContext context) {
        try { confirmOrderStep.compensate(context); } catch (Exception ex) { log.warn("Compensation failed for confirmOrderStep", ex); }
        try { processPaymentStep.compensate(context); } catch (Exception ex) { log.warn("Compensation failed for processPaymentStep", ex); }
        try { reserveInventoryStep.compensate(context); } catch (Exception ex) { log.warn("Compensation failed for reserveInventoryStep", ex); }
    }
}
