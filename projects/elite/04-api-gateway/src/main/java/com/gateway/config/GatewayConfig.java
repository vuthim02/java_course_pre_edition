package com.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("user-service", r -> r
                .path("/api/users/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("userServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/users"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())))
                .uri("lb://user-service"))
            .route("order-service", r -> r
                .path("/api/orders/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("orderServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/orders")))
                .uri("lb://order-service"))
            .route("product-service", r -> r
                .path("/api/products/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("productServiceCircuitBreaker")
                        .setFallbackUri("forward:/fallback/products")))
                .uri("lb://product-service"))
            .build();
    }

    @Bean
    public org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory requestRateLimiter() {
        return new org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory(
            redisRateLimiter(), keyResolver());
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter redisRateLimiter() {
        return new org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter(100, 200, 60);
    }

    @Bean
    public org.springframework.cloud.gateway.filter.ratelimit.KeyResolver keyResolver() {
        return exchange -> reactor.core.publisher.Mono.just(
            exchange.getRequest().getRemoteAddress().getAddress().getHostAddress());
    }
}
