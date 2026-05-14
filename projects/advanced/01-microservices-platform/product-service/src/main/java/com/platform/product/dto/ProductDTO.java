package com.platform.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductDTO(
    Long id,
    @NotBlank String name,
    String description,
    @Positive BigDecimal price,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
