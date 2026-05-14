package com.hft.service;

import com.hft.model.Order;
import com.hft.model.Side;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RiskEngine {
    private static final Logger log = LoggerFactory.getLogger(RiskEngine.class);

    private static final long MAX_ORDER_SIZE = 100_000;
    private static final long MAX_POSITION_SIZE = 1_000_000;
    private static final long MAX_DAILY_TRADE_VOLUME = 10_000_000;
    private static final int MAX_OPEN_ORDERS = 500;
    private static final long CIRCUIT_BREAKER_THRESHOLD = 5_000_000;

    private final Map<Integer, AtomicLong> positions = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> dailyVolume = new ConcurrentHashMap<>();
    private final Map<Integer, AtomicLong> openOrders = new ConcurrentHashMap<>();
    private volatile boolean circuitBreakerTripped = false;

    public RiskCheckResult checkOrder(Order order) {
        if (circuitBreakerTripped) {
            return RiskCheckResult.REJECTED_CIRCUIT_BREAKER;
        }
        if (order.quantity() > MAX_ORDER_SIZE) {
            return RiskCheckResult.REJECTED_MAX_SIZE;
        }
        long currentPosition = positions.getOrDefault(order.symbolId(), new AtomicLong(0)).get();
        long newPosition = order.side() == Side.BUY
            ? currentPosition + order.quantity()
            : currentPosition - order.quantity();
        if (Math.abs(newPosition) > MAX_POSITION_SIZE) {
            return RiskCheckResult.REJECTED_POSITION_LIMIT;
        }
        long currentOpen = openOrders.getOrDefault(order.symbolId(), new AtomicLong(0)).get();
        if (currentOpen >= MAX_OPEN_ORDERS) {
            return RiskCheckResult.REJECTED_MAX_OPEN_ORDERS;
        }
        long daily = dailyVolume.getOrDefault(order.symbolId(), new AtomicLong(0)).get();
        if (daily + order.quantity() > MAX_DAILY_TRADE_VOLUME) {
            return RiskCheckResult.REJECTED_DAILY_LIMIT;
        }
        return RiskCheckResult.APPROVED;
    }

    public void onOrderAccepted(Order order) {
        positions.computeIfAbsent(order.symbolId(), k -> new AtomicLong(0))
            .addAndGet(order.side() == Side.BUY ? order.quantity() : -order.quantity());
        openOrders.computeIfAbsent(order.symbolId(), k -> new AtomicLong(0)).incrementAndGet();
    }

    public void onTrade(long symbolId, long quantity) {
        dailyVolume.computeIfAbsent((int) symbolId, k -> new AtomicLong(0)).addAndGet(quantity);
        long total = dailyVolume.get((int) symbolId).get();
        if (total > CIRCUIT_BREAKER_THRESHOLD) {
            circuitBreakerTripped = true;
            log.warn("CIRCUIT BREAKER TRIPPED for symbol {} at volume {}", symbolId, total);
        }
    }

    public void resetCircuitBreaker() {
        circuitBreakerTripped = false;
        log.info("Circuit breaker reset");
    }

    public boolean isCircuitBreakerTripped() { return circuitBreakerTripped; }

    public enum RiskCheckResult {
        APPROVED,
        REJECTED_MAX_SIZE,
        REJECTED_POSITION_LIMIT,
        REJECTED_MAX_OPEN_ORDERS,
        REJECTED_DAILY_LIMIT,
        REJECTED_CIRCUIT_BREAKER
    }
}
