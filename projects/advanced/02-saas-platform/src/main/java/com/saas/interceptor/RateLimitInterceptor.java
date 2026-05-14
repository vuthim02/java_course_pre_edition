package com.saas.interceptor;

import com.saas.tenant.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> windowStart = new ConcurrentHashMap<>();
    private final int defaultLimit;

    private final Map<String, Integer> planLimits;

    public RateLimitInterceptor(
            @Value("${app.rate-limit.default:100}") int defaultLimit,
            @Value("#{${app.rate-limit.limits}}") Map<String, Integer> planLimits) {
        this.defaultLimit = defaultLimit;
        this.planLimits = planLimits != null ? planLimits : Map.of();
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        var tenantId = TenantContext.getTenantId();
        if (tenantId == null) return true;

        var now = System.currentTimeMillis();
        windowStart.putIfAbsent(tenantId, now);
        requestCounts.putIfAbsent(tenantId, new AtomicInteger(0));

        if (now - windowStart.get(tenantId) > 60_000) {
            windowStart.put(tenantId, now);
            requestCounts.get(tenantId).set(0);
        }

        var limit = planLimits.getOrDefault(tenantId, defaultLimit);
        if (requestCounts.get(tenantId).incrementAndGet() > limit) {
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded for tenant: " + tenantId);
            return false;
        }
        return true;
    }
}
