package com.platform.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderDTO(
    Long id,
    Long userId,
    @NotBlank String productName,
    @Positive Integer quantity,
    @Positive BigDecimal price,
    LocalDateTime createdAt
) {}
