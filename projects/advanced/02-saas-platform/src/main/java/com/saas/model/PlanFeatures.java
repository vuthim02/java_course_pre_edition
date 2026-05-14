package com.saas.model;

import java.util.Set;

public record PlanFeatures(
    SubscriptionPlan plan,
    Set<FeatureFlag> features,
    int maxUsers,
    int maxStorageGb,
    int requestsPerMinute
) {
    public static PlanFeatures forPlan(SubscriptionPlan plan) {
        return switch (plan) {
            case FREE -> new PlanFeatures(
                SubscriptionPlan.FREE,
                Set.of(FeatureFlag.API_ACCESS),
                5, 1, 50
            );
            case PRO -> new PlanFeatures(
                SubscriptionPlan.PRO,
                Set.of(FeatureFlag.API_ACCESS, FeatureFlag.ADVANCED_ANALYTICS,
                      FeatureFlag.PRIORITY_SUPPORT, FeatureFlag.WEBHOOKS),
                50, 50, 500
            );
            case ENTERPRISE -> new PlanFeatures(
                SubscriptionPlan.ENTERPRISE,
                Set.of(FeatureFlag.values()),
                1000, 1000, 5000
            );
        };
    }
}
