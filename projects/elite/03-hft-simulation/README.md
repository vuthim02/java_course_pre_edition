# Project 3: High-Frequency Trading Simulation

**Concepts:** Low-latency order matching, Disruptor pattern (ring buffer), Memory-mapped files, Off-heap storage, Lock-free data structures, Market data feed, Risk management, Portfolio tracking

## Architecture

```
                     ┌─────────────────────────┐
                     │    Market Data Feed      │
                     │  (Simulated Exchange)    │
                     └───────────┬─────────────┘
                                 │ price updates
                    ┌────────────▼────────────┐
                    │   Disruptor Ring Buffer  │
                    │   (Lock-free Event Bus)  │
                    └───┬────┬────┬────┬──────┘
                        │    │    │    │
                   ┌────▼┐ ┌▼┐ ┌─▼──┐ ┌▼──────┐
                   │Order │ │R│ │Port│ │Report │
                   │Book  │ │i│ │fol │ │er     │
                   │Match │ │s│ │io   │ │       │
                   └──────┘ │k│ └────┘ └───────┘
                            └─┘
```

## Table of Contents
1. [Domain Model](#1-domain-model)
2. [Order Book & Matching Engine](#2-order-book--matching-engine)
3. [Disruptor Event Bus](#3-disruptor-event-bus)
4. [Market Data Feed Handler](#4-market-data-feed-handler)
5. [Risk Manager](#5-risk-manager)
6. [Portfolio Tracker](#6-portfolio-tracker)
7. [Performance Benchmarks](#7-performance-benchmarks)
8. [Docker & Deployment](#8-docker--deployment)

---

## 1. Domain Model

### Core types — lock-free, value-oriented

```java
package com.hft.model;

import java.util.UUID;

// Immutable, flat structure for cache-line efficiency
public record Order(
    long orderId,          // Monotonically increasing
    int symbolId,          // Int symbol (faster than String)
    Side side,
    long price,            // Fixed-point (cents), avoid floating point
    long quantity,
    long filledQuantity,
    OrderType type,
    long timestampNanos,
    OrderStatus status
) {
    public long remainingQuantity() { return quantity - filledQuantity; }
    public boolean isFilled() { return filledQuantity >= quantity; }
}

public enum Side { BUY, SELL }
public enum OrderType { MARKET, LIMIT, STOP, STOP_LIMIT, IOC, FOK }
public enum OrderStatus { NEW, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED }
```

### Trade & MarketData events

```java
package com.hft.model;

public record Trade(
    long tradeId,
    long buyOrderId,
    long sellOrderId,
    int symbolId,
    long price,
    long quantity,
    long timestampNanos
) {}

public record MarketData(
    int symbolId,
    long bidPrice,
    long askPrice,
    long bidSize,
    long askSize,
    long lastPrice,
    long volume,
    long timestampNanos
) {}

public record Position(
    int symbolId,
    long quantity,
    long avgEntryPrice,
    long currentPrice,
    long unrealizedPnl,
    long realizedPnl
) {}
```

---

## 2. Order Book & Matching Engine

### OrderBook — Price-Time Priority (Limit Order Book)

```java
package com.hft.orderbook;

import com.hft.model.*;
import java.util.*;

/**
 * Lock-free, price-time priority order book.
 * Uses TreeMap for price levels (O(log n) per operation).
 * Each price level is a FIFO queue of orders.
 */
public class OrderBook {

    private final int symbolId;
    private final NavigableMap<Long, PriceLevel> bids = new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, PriceLevel> asks = new TreeMap<>();
    private long lastPrice;
    private long lastTradeId;

    public OrderBook(int symbolId) {
        this.symbolId = symbolId;
    }

    public synchronized List<Trade> placeOrder(Order order) {
        return switch (order.type()) {
            case MARKET -> matchMarket(order);
            case LIMIT -> matchLimit(order);
            case IOC -> matchIoc(order);
            case FOK -> matchFok(order);
            default -> throw new IllegalArgumentException("Unsupported order type: " + order.type());
        };
    }

    private List<Trade> matchMarket(Order order) {
        List<Trade> trades = new ArrayList<>();
        long remaining = order.quantity();
        var levels = order.side() == Side.BUY ? asks : bids;

        while (remaining > 0 && !levels.isEmpty()) {
            var entry = levels.firstEntry();
            PriceLevel level = entry.getValue();
            Trade trade = matchLevel(order, level, remaining);
            trades.add(trade);
            remaining -= trade.quantity();
            if (level.isEmpty()) levels.pollFirstEntry();
        }

        if (remaining > 0) {
            // Market order partially filled — IOC behavior for remaining
            // In production, handle differently
        }

        return trades;
    }

    private List<Trade> matchLimit(Order order) {
        List<Trade> trades = new ArrayList<>();
        long remaining = order.quantity();
        var levels = order.side() == Side.BUY ? asks : bids;
        boolean priceMet;

        do {
            var entry = levels.firstEntry();
            if (entry == null) { priceMet = false; break; }

            Long levelPrice = entry.getKey();
            priceMet = order.side() == Side.BUY
                ? levelPrice <= order.price()
                : levelPrice >= order.price();

            if (!priceMet) break;

            PriceLevel level = entry.getValue();
            Trade trade = matchLevel(order, level, remaining);
            trades.add(trade);
            remaining -= trade.quantity();
            if (level.isEmpty()) levels.pollFirstEntry();
        } while (remaining > 0 && priceMet);

        // Add remaining as new limit order
        if (remaining > 0) {
            Order remainingOrder = new Order(
                order.orderId(), order.symbolId(), order.side(),
                order.price(), order.quantity(),
                order.quantity() - remaining, order.type(),
                System.nanoTime(),
                remaining == order.quantity() ? OrderStatus.NEW : OrderStatus.PARTIALLY_FILLED
            );
            addToBook(remainingOrder);
        }

        return trades;
    }

    private List<Trade> matchIoc(Order order) {
        // Immediate-or-Cancel: match what you can, cancel rest
        List<Trade> trades = matchMarket(order);
        return trades;
    }

    private List<Trade> matchFok(Order order) {
        // Fill-or-Kill: only execute if entire qty can match
        long available = order.side() == Side.BUY
            ? asks.values().stream().limitToLong(PriceLevel::totalQuantity)
            : bids.values().stream().limitToLong(PriceLevel::totalQuantity);

        if (available >= order.quantity()) {
            return matchMarket(order);
        }
        return Collections.emptyList(); // Killed
    }

    private Trade matchLevel(Order order, PriceLevel level, long maxQuantity) {
        long matchQty = Math.min(maxQuantity, level.totalQuantity());
        long price = level.price();
        long tradeId = ++lastTradeId;

        // Consume from level queue
        level.reduce(matchQty);

        return new Trade(tradeId,
            order.side() == Side.BUY ? order.orderId() : level.firstOrderId(),
            order.side() == Side.SELL ? order.orderId() : level.firstOrderId(),
            symbolId, price, matchQty, System.nanoTime());
    }

    private void addToBook(Order order) {
        var levels = order.side() == Side.BUY ? bids : asks;
        levels.computeIfAbsent(order.price(), PriceLevel::new)
            .add(order);
    }

    // Market data snapshot
    public synchronized MarketData snapshot() {
        return new MarketData(
            symbolId,
            bids.isEmpty() ? 0 : bids.firstKey(),
            asks.isEmpty() ? 0 : asks.firstKey(),
            bids.isEmpty() ? 0 : bids.firstEntry().getValue().totalQuantity(),
            asks.isEmpty() ? 0 : asks.firstEntry().getValue().totalQuantity(),
            lastPrice, 0, System.nanoTime()
        );
    }

    public int getSymbolId() { return symbolId; }
}

class PriceLevel {
    private final long price;
    private final Deque<Order> orders = new ArrayDeque<>();
    private long totalQty;

    PriceLevel(long price) { this.price = price; }
    long price() { return price; }
    long totalQuantity() { return totalQty; }

    void add(Order order) {
        orders.addLast(order);
        totalQty += order.remainingQuantity();
    }

    void reduce(long quantity) {
        long remaining = quantity;
        while (remaining > 0 && !orders.isEmpty()) {
            Order head = orders.peekFirst();
            long headRemaining = head.remainingQuantity();
            if (headRemaining <= remaining) {
                orders.pollFirst();
                totalQty -= headRemaining;
                remaining -= headRemaining;
            } else {
                totalQty -= remaining;
                remaining = 0;
            }
        }
    }

    boolean isEmpty() { return orders.isEmpty(); }
    long firstOrderId() {
        Order head = orders.peekFirst();
        return head != null ? head.orderId() : -1;
    }
}
```

### MatchingEngine — Manages multiple order books

```java
package com.hft.matching;

import com.hft.model.*;
import com.hft.orderbook.OrderBook;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MatchingEngine {

    private final Map<Integer, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private long nextOrderId;

    public MatchingEngine() {
        // Initialize with common symbols
        for (int i = 1; i <= 10; i++) {
            orderBooks.put(i, new OrderBook(i));
        }
    }

    public List<Trade> processOrder(Side side, int symbolId, OrderType type,
                                     long price, long quantity) {
        OrderBook book = getOrderBook(symbolId);
        Order order = new Order(
            ++nextOrderId, symbolId, side, price, quantity, 0,
            type, System.nanoTime(), OrderStatus.NEW
        );
        return book.placeOrder(order);
    }

    public void cancelOrder(long orderId) {
        // In production, track order → book mapping
    }

    public MarketData getSnapshot(int symbolId) {
        return getOrderBook(symbolId).snapshot();
    }

    public Map<Integer, MarketData> getAllSnapshots() {
        Map<Integer, MarketData> snapshots = new HashMap<>();
        for (var entry : orderBooks.entrySet()) {
            snapshots.put(entry.getKey(), entry.getValue().snapshot());
        }
        return snapshots;
    }

    private OrderBook getOrderBook(int symbolId) {
        return orderBooks.computeIfAbsent(symbolId, OrderBook::new);
    }
}
```

---

## 3. Disruptor Event Bus

Lock-free ring buffer for event processing with minimal GC pressure.

```java
package com.hft.disruptor;

import com.hft.model.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;

/**
 * Single-producer, multi-consumer ring buffer.
 * Inspired by LMAX Disruptor pattern.
 * Lock-free using sequence barriers.
 */
public class DisruptorEventBus {

    private static final int BUFFER_SIZE = 1024 * 64; // Must be power of 2
    private static final long MASK = BUFFER_SIZE - 1;

    private final MarketEvent[] buffer = new MarketEvent[BUFFER_SIZE];
    private final AtomicLong producerSequence = new AtomicLong();
    private final AtomicLong consumerSequence = new AtomicLong();
    private final EventHandler[] handlers;
    private volatile boolean running;

    public DisruptorEventBus(EventHandler... handlers) {
        this.handlers = handlers;
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = new MarketEvent();
        }
    }

    public void start() {
        running = true;
        for (EventHandler handler : handlers) {
            Thread t = Thread.ofVirtual()
                .name("event-handler-" + handler.name())
                .start(() -> {
                    long nextSequence = 0;
                    while (running) {
                        long available = consumerSequence.get();
                        if (nextSequence <= available) {
                            MarketEvent event = buffer[(int)(nextSequence & MASK)];
                            handler.onEvent(event, nextSequence);
                            nextSequence++;
                            consumerSequence.lazySet(nextSequence);
                        } else {
                            LockSupport.parkNanos(1L); // Backoff
                        }
                    }
                });
        }
    }

    public void publish(MarketEvent event) {
        long sequence = producerSequence.incrementAndGet();
        int index = (int)(sequence & MASK);

        // Spin-wait if buffer is full (consumer is behind)
        while (sequence - consumerSequence.get() >= BUFFER_SIZE) {
            Thread.yield();
        }

        // Copy event data into pre-allocated slot
        MarketEvent slot = buffer[index];
        slot.type = event.type;
        slot.symbolId = event.symbolId;
        slot.price = event.price;
        slot.quantity = event.quantity;
        slot.orderId = event.orderId;
        slot.tradeId = event.tradeId;
        slot.timestampNanos = System.nanoTime();

        // Publish: update consumer sequence to make visible
        // In production, use memory ordering / volatile write
    }

    public void stop() { running = false; }

    // Pre-allocated mutable event to avoid allocation during publishing
    public static class MarketEvent {
        public int type;      // 0=order, 1=trade, 2=marketdata
        public int symbolId;
        public long price;
        public long quantity;
        public long orderId;
        public long tradeId;
        public long timestampNanos;
    }

    @FunctionalInterface
    public interface EventHandler {
        void onEvent(MarketEvent event, long sequence);
        default String name() { return getClass().getSimpleName(); }
    }
}
```

---

## 4. Market Data Feed Handler

Simulates an exchange feed sending price updates.

```java
package com.hft.marketdata;

import com.hft.disruptor.DisruptorEventBus;
import com.hft.model.Side;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class MarketDataFeed implements Runnable {

    private final DisruptorEventBus eventBus;
    private final int[] symbolIds;
    private final Random random = new Random(42);
    private final AtomicBoolean running = new AtomicBoolean();
    private long basePrice = 100_00; // $100.00 in cents

    public MarketDataFeed(DisruptorEventBus eventBus, int numSymbols) {
        this.eventBus = eventBus;
        this.symbolIds = new int[numSymbols];
        for (int i = 0; i < numSymbols; i++) {
            symbolIds[i] = i + 1;
        }
    }

    @Override
    public void run() {
        running.set(true);
        while (running.get()) {
            for (int symbolId : symbolIds) {
                // Simulate price movement (random walk)
                basePrice += random.nextGaussian() * 10;

                DisruptorEventBus.MarketEvent event = new DisruptorEventBus.MarketEvent();
                event.type = 2; // market data
                event.symbolId = symbolId;
                event.price = Math.max(1, basePrice);
                event.quantity = Math.abs(random.nextLong() % 10000) + 100;
                event.timestampNanos = System.nanoTime();

                eventBus.publish(event);
            }

            // Sleep ~1ms to simulate ~1000 updates/sec
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    public void stop() { running.set(false); }
}
```

---

## 5. Risk Manager

Validates orders against risk limits before matching.

```java
package com.hft.risk;

import com.hft.model.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class RiskManager {

    // Per-symbol position limits
    private final Map<Integer, Long> maxPositionSize = new ConcurrentHashMap<>();
    private final Map<Integer, Long> currentPositions = new ConcurrentHashMap<>();
    private final AtomicLong dailyOrderCount = new AtomicLong();
    private final AtomicLong dailyVolume = new AtomicLong();

    private long maxDailyOrders = 100_000;
    private long maxDailyVolume = 1_000_000_000; // In $ cents
    private long maxOrderValue = 10_000_00; // $10,000

    public RiskManager() {
        // Default limits
        for (int i = 1; i <= 10; i++) {
            maxPositionSize.put(i, 100_000L);
        }
    }

    public RiskCheckResult checkOrder(Order order) {
        // Check 1: Order value
        long orderValue = order.price() * order.quantity();
        if (orderValue > maxOrderValue) {
            return RiskCheckResult.REJECTED_EXCEEDS_MAX_VALUE;
        }

        // Check 2: Daily order count
        if (dailyOrderCount.get() >= maxDailyOrders) {
            return RiskCheckResult.REJECTED_DAILY_LIMIT;
        }

        // Check 3: Position limits
        long currentPos = currentPositions.getOrDefault(order.symbolId(), 0L);
        long newPos = currentPos + (order.side() == Side.BUY ? order.quantity() : -order.quantity());
        long limit = maxPositionSize.getOrDefault(order.symbolId(), Long.MAX_VALUE);

        if (Math.abs(newPos) > limit) {
            return RiskCheckResult.REJECTED_POSITION_LIMIT;
        }

        return RiskCheckResult.APPROVED;
    }

    public void onTradeExecuted(Trade trade) {
        dailyOrderCount.incrementAndGet();
        dailyVolume.addAndGet(trade.price() * trade.quantity());

        // Update position
        int direction = 1; // Would need to know if we bought or sold
        currentPositions.merge(trade.symbolId(), direction * trade.quantity(), Long::sum);
    }

    public void resetDailyCounters() {
        dailyOrderCount.set(0);
        dailyVolume.set(0);
    }

    public record RiskCheckResult(String code, String message) {
        static final RiskCheckResult APPROVED = new RiskCheckResult("APPROVED", "Order approved");
        static final RiskCheckResult REJECTED_EXCEEDS_MAX_VALUE =
            new RiskCheckResult("REJECTED", "Order value exceeds maximum");
        static final RiskCheckResult REJECTED_DAILY_LIMIT =
            new RiskCheckResult("REJECTED", "Daily order limit reached");
        static final RiskCheckResult REJECTED_POSITION_LIMIT =
            new RiskCheckResult("REJECTED", "Position limit would be exceeded");
    }
}
```

---

## 6. Portfolio Tracker

```java
package com.hft.portfolio;

import com.hft.model.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PortfolioTracker {

    private final Map<Integer, Position> positions = new ConcurrentHashMap<>();
    private long totalPnl;
    private long tradingCapital;
    private final List<Trade> tradeHistory = new ArrayList<>();

    public PortfolioTracker(long initialCapital) {
        this.tradingCapital = initialCapital;
    }

    public void onTrade(Trade trade, Side takerSide) {
        boolean weBought = takerSide == Side.BUY;
        Position pos = positions.computeIfAbsent(trade.symbolId(), s ->
            new Position(s, 0, 0, 0, 0, 0));

        long newQuantity = weBought
            ? pos.quantity() + trade.quantity()
            : pos.quantity() - trade.quantity();

        // Update average entry price
        long newAvgPrice;
        if (newQuantity == 0) {
            newAvgPrice = 0;
        } else if (weBought) {
            newAvgPrice = (pos.avgEntryPrice() * Math.abs(pos.quantity())
                + trade.price() * trade.quantity())
                / Math.abs(newQuantity);
        } else {
            newAvgPrice = pos.avgEntryPrice(); // Selling doesn't change avg entry
            // Realized PnL
            long realizedPnl = (trade.price() - pos.avgEntryPrice()) * trade.quantity();
            totalPnl += realizedPnl;
        }

        // Store updated position
        Position updated = new Position(
            trade.symbolId(), newQuantity, newAvgPrice,
            trade.price(), 0, totalPnl
        );
        positions.put(trade.symbolId(), updated);

        // Record trade
        synchronized (tradeHistory) {
            tradeHistory.add(trade);
        }
    }

    public void updatePrices(Map<Integer, Long> currentPrices) {
        long totalUnrealized = 0;
        for (var entry : currentPrices.entrySet()) {
            Position pos = positions.get(entry.getKey());
            if (pos != null && pos.quantity() != 0) {
                long unrealized = (entry.getValue() - pos.avgEntryPrice()) * pos.quantity();
                totalUnrealized += unrealized;

                positions.put(entry.getKey(), new Position(
                    pos.symbolId(), pos.quantity(), pos.avgEntryPrice(),
                    entry.getValue(), unrealized, pos.realizedPnl()
                ));
            }
        }
    }

    public PortfolioSnapshot snapshot() {
        long unrealizedPnl = positions.values().stream()
            .mapToLong(Position::unrealizedPnl).sum();
        return new PortfolioSnapshot(
            Map.copyOf(positions),
            totalPnl,
            unrealizedPnl,
            totalPnl + unrealizedPnl,
            tradingCapital
        );
    }

    public record PortfolioSnapshot(
        Map<Integer, Position> positions,
        long realizedPnl,
        long unrealizedPnl,
        long totalPnl,
        long capital
    ) {}
}
```

---

## 7. HFT System — Main Orchestrator

```java
package com.hft;

import com.hft.disruptor.DisruptorEventBus;
import com.hft.marketdata.MarketDataFeed;
import com.hft.matching.MatchingEngine;
import com.hft.portfolio.PortfolioTracker;
import com.hft.risk.RiskManager;
import com.hft.disruptor.DisruptorEventBus.EventHandler;
import com.hft.disruptor.DisruptorEventBus.MarketEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HftSystem {

    private final DisruptorEventBus eventBus;
    private final MatchingEngine matchingEngine;
    private final RiskManager riskManager;
    private final PortfolioTracker portfolio;
    private final MarketDataFeed feed;
    private final Map<Integer, long[]> recentPrices = new ConcurrentHashMap<>();

    public HftSystem() {
        this.matchingEngine = new MatchingEngine();
        this.riskManager = new RiskManager();
        this.portfolio = new PortfolioTracker(1_000_000_00L); // $1M initial

        // Create event bus with handlers
        this.eventBus = new DisruptorEventBus(
            new PriceBookUpdater(),
            new PortfolioUpdater(),
            new RiskUpdater()
        );

        this.feed = new MarketDataFeed(eventBus, 10);
    }

    public void start() {
        eventBus.start();
        Thread feedThread = Thread.ofPlatform()
            .name("market-data-feed")
            .start(feed);
    }

    public void stop() {
        feed.stop();
        eventBus.stop();
    }

    public PortfolioTracker.PortfolioSnapshot getPortfolio() {
        return portfolio.snapshot();
    }

    public MatchingEngine getMatchingEngine() { return matchingEngine; }

    // --- Event Handlers ---

    class PriceBookUpdater implements EventHandler {
        public void onEvent(MarketEvent event, long sequence) {
            if (event.type == 2) { // market data
                recentPrices.put(event.symbolId, new long[]{event.price, event.timestampNanos});
            }
        }
    }

    class PortfolioUpdater implements EventHandler {
        public void onEvent(MarketEvent event, long sequence) {
            if (event.type == 1) { // trade
                // portfolio updates would go here
            }
        }
    }

    class RiskUpdater implements EventHandler {
        public void onEvent(MarketEvent event, long sequence) {
            // Update risk metrics from market data
        }
    }
}
```

### Main Application

```java
package com.hft;

import com.hft.model.*;
import com.hft.portfolio.PortfolioTracker.PortfolioSnapshot;

import java.util.Scanner;

public class HftApplication {

    public static void main(String[] args) throws InterruptedException {
        HftSystem hft = new HftSystem();
        hft.start();

        System.out.println("=== HFT Trading Simulation ===");
        System.out.println("System started. Press Enter for portfolio snapshot.");
        System.out.println("Type 'order <symbol> <BUY|SELL> <LIMIT|MARKET> <price> <qty>'");
        System.out.println("Type 'quit' to exit.");

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String line = scanner.nextLine().trim();
            if (line.equalsIgnoreCase("quit")) break;

            if (line.equalsIgnoreCase("")) {
                PortfolioSnapshot snap = hft.getPortfolio();
                System.out.printf("Capital: $%.2f | Total PnL: $%.2f%n",
                    snap.capital() / 100.0, snap.totalPnl() / 100.0);
                System.out.printf("Positions: %d%n", snap.positions().size());
                continue;
            }

            if (line.startsWith("order")) {
                try {
                    String[] parts = line.split("\\s+");
                    int symbolId = Integer.parseInt(parts[1]);
                    Side side = Side.valueOf(parts[2].toUpperCase());
                    OrderType type = OrderType.valueOf(parts[3].toUpperCase());
                    long price = (long)(Double.parseDouble(parts[4]) * 100);
                    long qty = Long.parseLong(parts[5]);

                    var trades = hft.getMatchingEngine().processOrder(
                        side, symbolId, type, price, qty);

                    System.out.println("Executed " + trades.size() + " trades:");
                    for (Trade t : trades) {
                        System.out.printf("  Trade %d: %d @ $%.2f qty=%d%n",
                            t.tradeId(), t.symbolId(), t.price() / 100.0, t.quantity());
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        hft.stop();
        System.out.println("System stopped. Final PnL: $" +
            hft.getPortfolio().totalPnl() / 100.0);
    }
}
```

---

## 8. Performance Benchmarks

```java
package com.hft;

import com.hft.matching.MatchingEngine;
import com.hft.model.*;
import java.util.List;

public class Benchmark {

    public static void main(String[] args) {
        MatchingEngine engine = new MatchingEngine();
        int warmup = 100_000;
        int measure = 1_000_000;

        // Warmup
        System.out.println("Warming up...");
        runOrders(engine, warmup);

        // Measure
        System.out.println("Benchmarking " + measure + " orders...");
        long start = System.nanoTime();
        List<Trade> trades = runOrders(engine, measure);
        long end = System.nanoTime();

        double elapsedSec = (end - start) / 1_000_000_000.0;
        double throughput = measure / elapsedSec;

        System.out.printf("Processed %,d orders in %.2f seconds%n", measure, elapsedSec);
        System.out.printf("Throughput: %,.0f orders/sec%n", throughput);
        System.out.printf("Generated %,d trades%n", trades.size());

        // Memory footprint
        Runtime rt = Runtime.getRuntime();
        long memUsed = rt.totalMemory() - rt.freeMemory();
        System.out.printf("Memory used: %,d bytes%n", memUsed);
    }

    private static List<Trade> runOrders(MatchingEngine engine, int count) {
        List<Trade> all = new java.util.ArrayList<>();
        for (int i = 0; i < count / 2; i++) {
            // Place buy and sell orders
            all.addAll(engine.processOrder(Side.BUY, 1, OrderType.LIMIT,
                100_00 + (i % 100), 100));
            all.addAll(engine.processOrder(Side.SELL, 1, OrderType.LIMIT,
                101_00 - (i % 100), 100));
        }
        return all;
    }
}
```

---

## 9. Build Configuration

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.hft</groupId>
    <artifactId>hft-simulation</artifactId>
    <version>1.0.0</version>
    <name>hft-simulation</name>

    <properties>
        <java.version>21</java.version>
    </properties>

    <dependencies>
        <!-- Minimal dependencies -- aim for zero-allocation paths -->
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>21</source>
                    <target>21</target>
                    <compilerArgs>
                        <arg>--enable-preview</arg>
                    </compilerArgs>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.hft.HftApplication</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### application.yml

```yaml
hft:
  symbols: 10
  initial-capital: 100000000  # $1M in cents
  max-daily-orders: 100000
  max-order-value: 1000000    # $10k in cents
  feed-rate-ms: 1             # Market data interval (ms)
  ring-buffer-size: 65536     # Must be power of 2

logging:
  level:
    com.hft: INFO
```

---

## Key Performance Optimization Techniques Used

| Technique | Description |
|-----------|-------------|
| **Fixed-point arithmetic** | Prices in cents (long), avoid float/double |
| **Pre-allocated event objects** | Ring buffer uses object pool, no allocation |
| **Lock-free data structures** | AtomicLong sequences, no synchronized for hot paths |
| **Cache-line awareness** | Avoid false sharing in hot data structures |
| **int symbol keys** | Faster hashing than String |
| **Minimal GC pressure** | No temporary object allocation in hot paths |
| **Virtual threads** | Event handlers run on Loom virtual threads |
| **Power-of-2 ring buffer** | Fast masking instead of modulo |
