package com.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;

public record UsageRecordDTO(
    Long id,
    @NotBlank String tenantId,
    @NotBlank String metric,
    @PositiveOrZero long value,
    LocalDateTime recordedAt
) {}
