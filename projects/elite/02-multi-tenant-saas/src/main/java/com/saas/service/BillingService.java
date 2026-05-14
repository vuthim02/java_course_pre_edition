package com.saas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class BillingService {
    private static final Logger log = LoggerFactory.getLogger(BillingService.class);

    private static final java.util.Map<String, BigDecimal> PRICES = java.util.Map.of(
        "FREE", BigDecimal.ZERO,
        "STARTER", new BigDecimal("49.99"),
        "PROFESSIONAL", new BigDecimal("199.99"),
        "ENTERPRISE", new BigDecimal("999.99")
    );

    public Invoice createInvoice(String tenantId, String tier) {
        BigDecimal amount = PRICES.getOrDefault(tier.toUpperCase(), BigDecimal.ZERO);
        Invoice invoice = new Invoice(UUID.randomUUID().toString(), tenantId, amount, "PENDING", Instant.now());
        log.info("Created invoice {} for tenant {}: {}", invoice.id(), tenantId, amount);
        return invoice;
    }

    public boolean processPayment(String invoiceId) {
        log.info("Processing payment for invoice {}...", invoiceId);
        return true;
    }

    public record Invoice(String id, String tenantId, BigDecimal amount, String status, Instant createdAt) {}
}
