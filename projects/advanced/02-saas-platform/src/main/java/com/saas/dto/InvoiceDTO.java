package com.saas.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InvoiceDTO(
    String invoiceNumber,
    String tenantId,
    String plan,
    BigDecimal amount,
    LocalDate issuedDate,
    LocalDate dueDate,
    String status
) {}
