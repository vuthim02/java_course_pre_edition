package com.saas.controller;

import com.saas.dto.InvoiceDTO;
import com.saas.model.SubscriptionPlan;
import com.saas.service.BillingService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/billing")
public class BillingController {
    private final BillingService service;

    public BillingController(BillingService service) {
        this.service = service;
    }

    @PostMapping("/invoice")
    public InvoiceDTO generateInvoice(@RequestParam String tenantId,
                                       @RequestParam SubscriptionPlan plan) {
        return service.generateInvoice(tenantId, plan);
    }

    @PostMapping("/pay/{invoiceNumber}")
    public InvoiceDTO payInvoice(@PathVariable String invoiceNumber) {
        return service.processPayment(invoiceNumber);
    }
}
