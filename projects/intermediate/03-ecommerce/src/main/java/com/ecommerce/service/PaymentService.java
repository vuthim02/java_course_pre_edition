package com.ecommerce.service;

import com.ecommerce.dto.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    public PaymentResult processPayment(Long orderId, BigDecimal amount, String currency, String source) {
        log.info("Processing payment for order {}: {} {}", orderId, amount, currency);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return new PaymentResult(false, null, "Invalid amount");
        }
        String paymentIntentId = "pi_" + UUID.randomUUID().toString().replace("-", "");
        log.info("Payment successful for order {}: intent {}", orderId, paymentIntentId);
        return new PaymentResult(true, paymentIntentId, "Payment successful");
    }

    public PaymentResult refundPayment(String paymentIntentId) {
        log.info("Refunding payment intent: {}", paymentIntentId);
        return new PaymentResult(true, null, "Refund successful");
    }

    public record PaymentResult(boolean success, String paymentIntentId, String message) {}
}
