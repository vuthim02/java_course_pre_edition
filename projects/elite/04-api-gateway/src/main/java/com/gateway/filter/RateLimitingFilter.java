package com.gateway.filter;

import com.gateway.model.RateLimitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimitingFilter implements GatewayFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    private final ReactiveStringRedisTemplate redisTemplate;
    private final RateLimitConfig config;
    private final ConcurrentHashMap<String, RateLimitState> localStore = new ConcurrentHashMap<>();

    public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate, RateLimitConfig config) {
        this.redisTemplate = redisTemplate;
        this.config = config;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String clientIp = exchange.getRequest().getRemoteAddress() != null
            ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
            : "unknown";
        String key = "rate_limit:" + clientIp;

        return redisTemplate.opsForValue().increment(key)
            .flatMap(count -> {
                if (count == 1) {
                    return redisTemplate.expire(key, Duration.ofSeconds(config.windowSeconds()))
                        .thenReturn(count);
                }
                return Mono.just(count);
            })
            .flatMap(count -> {
                if (count > config.replenishRate()) {
                    log.warn("Rate limit exceeded for IP: {}", clientIp);
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            })
            .onErrorResume(e -> {
                RateLimitState state = localStore.computeIfAbsent(key, k -> new RateLimitState());
                long now = System.currentTimeMillis();
                if (now - state.windowStart > config.windowSeconds() * 1000L) {
                    state.count = 0;
                    state.windowStart = now;
                }
                state.count++;
                if (state.count > config.replenishRate()) {
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    return exchange.getResponse().setComplete();
                }
                return chain.filter(exchange);
            });
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private static class RateLimitState {
        long count;
        long windowStart = System.currentTimeMillis();
    }
}
