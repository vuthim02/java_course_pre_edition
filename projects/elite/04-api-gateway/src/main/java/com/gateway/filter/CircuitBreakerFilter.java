package com.gateway.filter;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CircuitBreakerFilter implements GatewayFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(CircuitBreakerFilter.class);

    private final Map<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    private final CircuitBreakerRegistry registry;

    public CircuitBreakerFilter() {
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();
        this.registry = CircuitBreakerRegistry.of(defaultConfig);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String routeIdAttr = exchange.getAttribute("routeId");
        String routeId = routeIdAttr != null ? routeIdAttr : "default";

        CircuitBreaker cb = circuitBreakers.computeIfAbsent(routeId, id -> {
            log.info("Creating circuit breaker for route: {}", id);
            return registry.circuitBreaker(id);
        });

        return chain.filter(exchange)
            .transformDeferred(CircuitBreakerOperator.of(cb))
            .onErrorResume(e -> {
                log.warn("Circuit breaker {} open, returning fallback", routeId);
                exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                return exchange.getResponse().setComplete();
            });
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
