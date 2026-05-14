package com.saas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.saas.model.SubscriptionPlan;

public record CreateTenantRequest(
    @NotBlank String tenantId,
    @NotBlank String name,
    @NotNull SubscriptionPlan plan
) {}
