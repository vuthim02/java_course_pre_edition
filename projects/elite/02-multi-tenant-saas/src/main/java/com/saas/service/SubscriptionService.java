package com.saas.service;

import com.saas.model.Tenant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;

@Service
public class SubscriptionService {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);

    private static final Map<String, PlanConfig> PLANS = Map.of(
        "FREE", new PlanConfig(10, 5, Set.of("basic-reports")),
        "STARTER", new PlanConfig(50, 50, Set.of("basic-reports", "api-access", "email-support")),
        "PROFESSIONAL", new PlanConfig(200, 200, Set.of("basic-reports", "advanced-reports", "api-access",
            "priority-support", "audit-logs", "sso")),
        "ENTERPRISE", new PlanConfig(10000, 2000, Set.of("basic-reports", "advanced-reports", "api-access",
            "priority-support", "audit-logs", "sso", "custom-integrations", "dedicated-infrastructure"))
    );

    public PlanConfig getPlanConfig(String tier) {
        return PLANS.getOrDefault(tier.toUpperCase(), PLANS.get("FREE"));
    }

    public boolean isFeatureEnabled(Tenant tenant, String feature) {
        PlanConfig config = getPlanConfig(tenant.getTier().name());
        return config.features().contains(feature);
    }

    public boolean canAddUser(Tenant tenant) {
        return tenant.getMaxUsers() > 0;
    }

    public PlanConfig upgradePlan(String currentTier, String newTier) {
        log.info("Upgrading plan from {} to {}", currentTier, newTier);
        return getPlanConfig(newTier);
    }

    public record PlanConfig(int maxUsers, int maxStorageGb, Set<String> features) {}
}
