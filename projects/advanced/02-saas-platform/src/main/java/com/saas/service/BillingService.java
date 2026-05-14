package com.saas.service;

import com.saas.dto.InvoiceDTO;
import com.saas.model.SubscriptionPlan;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
public class BillingService {

    private static final Map<SubscriptionPlan, BigDecimal> PRICES = Map.of(
        SubscriptionPlan.FREE, BigDecimal.ZERO,
        SubscriptionPlan.PRO, new BigDecimal("29.99"),
        SubscriptionPlan.ENTERPRISE, new BigDecimal("299.99")
    );

    public InvoiceDTO generateInvoice(String tenantId, SubscriptionPlan plan) {
        var amount = PRICES.getOrDefault(plan, BigDecimal.ZERO);
        return new InvoiceDTO(
            "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            tenantId,
            plan.name(),
            amount,
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            "PENDING"
        );
    }

    public InvoiceDTO processPayment(String invoiceNumber) {
        return new InvoiceDTO(
            invoiceNumber, null, null, null, null, null, "PAID"
        );
    }
}
