package com.saas.dto;

import com.saas.model.SubscriptionPlan;
import com.saas.model.TenantStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record TenantDTO(
    Long id,
    @NotBlank String tenantId,
    @NotBlank String name,
    @NotNull SubscriptionPlan plan,
    @NotNull TenantStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
