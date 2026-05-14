package com.saas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingInterceptor implements HandlerInterceptor {
    private static final Logger log = LoggerFactory.getLogger(RateLimitingInterceptor.class);

    @Value("${rate-limiting.enabled:true}")
    private boolean enabled;

    @Value("${rate-limiting.default-limit:100}")
    private int defaultLimit;

    @Value("${rate-limiting.default-window-seconds:60}")
    private int windowSeconds;

    private final StringRedisTemplate redisTemplate;
    private final ConcurrentHashMap<String, RateLimitState> localStore = new ConcurrentHashMap<>();

    public RateLimitingInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!enabled) return true;

        String clientIp = request.getRemoteAddr();
        String key = "rate_limit:" + clientIp;

        try {
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == 1) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            if (count != null && count > defaultLimit) {
                response.setStatus(429);
                response.getWriter().write("Rate limit exceeded. Try again later.");
                return false;
            }
        } catch (Exception e) {
            RateLimitState state = localStore.computeIfAbsent(key, k -> new RateLimitState());
            state = localStore.get(key);
            long now = System.currentTimeMillis();
            if (now - state.windowStart > windowSeconds * 1000L) {
                state.count = 0;
                state.windowStart = now;
            }
            state.count++;
            if (state.count > defaultLimit) {
                response.setStatus(429);
                response.getWriter().write("Rate limit exceeded. Try again later.");
                return false;
            }
        }
        return true;
    }

    private static class RateLimitState {
        long count;
        long windowStart = System.currentTimeMillis();
    }
}
