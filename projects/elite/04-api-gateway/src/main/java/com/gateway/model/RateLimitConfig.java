package com.gateway.model;

public record RateLimitConfig(
    int replenishRate,
    int burstCapacity,
    int windowSeconds
) {
    public static RateLimitConfig defaultConfig() {
        return new RateLimitConfig(100, 200, 60);
    }

    public static RateLimitConfig strictConfig() {
        return new RateLimitConfig(10, 20, 60);
    }
}
