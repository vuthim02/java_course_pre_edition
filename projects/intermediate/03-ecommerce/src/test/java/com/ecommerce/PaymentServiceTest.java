package com.ecommerce;

import com.ecommerce.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService();
    }

    @Nested
    class ProcessPayment {

        @Test
        void testProcessPayment_Success() {
            PaymentService.PaymentResult result = paymentService.processPayment(1L, BigDecimal.valueOf(99.99), "USD", "card_123");

            assertTrue(result.success());
            assertNotNull(result.paymentIntentId());
            assertTrue(result.paymentIntentId().startsWith("pi_"));
            assertEquals("Payment successful", result.message());
        }

        @Test
        void testProcessPayment_ZeroAmount() {
            PaymentService.PaymentResult result = paymentService.processPayment(1L, BigDecimal.ZERO, "USD", "card_123");

            assertFalse(result.success());
            assertNull(result.paymentIntentId());
            assertEquals("Invalid amount", result.message());
        }

        @Test
        void testProcessPayment_NegativeAmount() {
            PaymentService.PaymentResult result = paymentService.processPayment(1L, BigDecimal.valueOf(-50.00), "USD", "card_123");

            assertFalse(result.success());
            assertEquals("Invalid amount", result.message());
        }

        @Test
        void testProcessPayment_LargeAmount() {
            PaymentService.PaymentResult result = paymentService.processPayment(1L, BigDecimal.valueOf(999999.99), "USD", "card_123");

            assertTrue(result.success());
            assertNotNull(result.paymentIntentId());
        }

        @Test
        void testProcessPayment_DifferentCurrencies() {
            PaymentService.PaymentResult resultUsd = paymentService.processPayment(1L, BigDecimal.TEN, "USD", "card_1");
            PaymentService.PaymentResult resultEur = paymentService.processPayment(2L, BigDecimal.TEN, "EUR", "card_2");

            assertTrue(resultUsd.success());
            assertTrue(resultEur.success());
        }

        @Test
        void testProcessPayment_GeneratedIdIsUnique() {
            PaymentService.PaymentResult result1 = paymentService.processPayment(1L, BigDecimal.TEN, "USD", "card_1");
            PaymentService.PaymentResult result2 = paymentService.processPayment(1L, BigDecimal.TEN, "USD", "card_1");

            assertNotEquals(result1.paymentIntentId(), result2.paymentIntentId());
        }
    }

    @Nested
    class RefundPayment {

        @Test
        void testRefundPayment_Success() {
            PaymentService.PaymentResult result = paymentService.refundPayment("pi_valid123");

            assertTrue(result.success());
            assertEquals("Refund successful", result.message());
        }

        @Test
        void testRefundPayment_AlwaysSucceeds() {
            PaymentService.PaymentResult result = paymentService.refundPayment("any_payment_id");

            assertTrue(result.success());
            assertNull(result.paymentIntentId());
        }

        @Test
        void testRefundPayment_EmptyId() {
            PaymentService.PaymentResult result = paymentService.refundPayment("");

            assertTrue(result.success());
        }
    }
}
