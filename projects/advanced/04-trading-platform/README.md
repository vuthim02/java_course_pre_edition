# Low-Latency Trading Simulation

High-performance trading platform with order book matching engine, market data feed, portfolio tracking, risk management, and event-driven architecture using ring buffers and primitive collections.

## Architecture

```
                     ┌──────────────────┐
                     │   Market Data    │
                     │   Feed Handler   │
                     └───────┬──────────┘
                             │
              ┌──────────────┼──────────────┐
              │              │              │
        ┌─────▼─────┐ ┌──────▼──────┐ ┌────▼──────┐
        │  Order    │ │   Risk     │ │ Portfolio │
        │  Book     │ │  Manager   │ │  Tracker  │
        └─────┬─────┘ └──────┬──────┘ └────┬──────┘
              │              │              │
        ┌─────▼──────────────▼──────────────▼─────┐
        │          Event Bus (Ring Buffer)        │
        └─────────────────────────────────────────┘
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.trading</groupId>
    <artifactId>trading-platform</artifactId>
    <version>1.0.0</version>
    <name>trading-platform</name>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        <dependency>
            <groupId>org.openjdk.jol</groupId>
            <artifactId>jol-core</artifactId>
            <version>0.17</version>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml

```yaml
server:
  port: 8080
spring:
  application:
    name: trading-platform
  jackson:
    serialization:
      write-dates-as-timestamps: false

trading:
  order-book:
    max-levels: 100
  risk:
    max-position-size: 1000000
    max-order-value: 500000
    max-leverage: 5
    stop-loss-default-pct: 5.0
  market-data:
    symbols: AAPL,GOOGL,MSFT,AMZN,TSLA,NVDA,META,JPM,V,JNJ
    tick-interval-ms: 100
```

## TradingApplication.java

```java
package com.trading;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TradingApplication {
    public static void main(String[] args) {
        SpringApplication.run(TradingApplication.class, args);
    }
}
```

---

## Core Model

### Order.java
```java
package com.trading.model;

public class Order {
    private long orderId;
    private String symbol;
    private Side side;
    private OrderType type;
    private long price; // fixed-point (price * 10000)
    private long quantity;
    private long filledQuantity;
    private long remainingQuantity;
    private OrderStatus status;
    private long timestamp;
    private long userId;

    public enum Side { BUY, SELL }
    public enum OrderType { MARKET, LIMIT, STOP, STOP_LIMIT }
    public enum OrderStatus { NEW, PARTIALLY_FILLED, FILLED, CANCELLED, REJECTED, EXPIRED }

    public Order() {}

    public Order(long orderId, String symbol, Side side, OrderType type, long price, long quantity, long userId) {
        this.orderId = orderId;
        this.symbol = symbol;
        this.side = side;
        this.type = type;
        this.price = price;
        this.quantity = quantity;
        this.filledQuantity = 0;
        this.remainingQuantity = quantity;
        this.status = OrderStatus.NEW;
        this.timestamp = System.nanoTime();
        this.userId = userId;
    }

    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public Side getSide() { return side; }
    public void setSide(Side side) { this.side = side; }
    public OrderType getType() { return type; }
    public void setType(OrderType type) { this.type = type; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public long getFilledQuantity() { return filledQuantity; }
    public void setFilledQuantity(long filledQuantity) { this.filledQuantity = filledQuantity; }
    public long getRemainingQuantity() { return remainingQuantity; }
    public void setRemainingQuantity(long remainingQuantity) { this.remainingQuantity = remainingQuantity; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
}
```

### Trade.java
```java
package com.trading.model;

public class Trade {
    private long tradeId;
    private long buyOrderId;
    private long sellOrderId;
    private String symbol;
    private long price;
    private long quantity;
    private long timestamp;

    public Trade(long tradeId, long buyOrderId, long sellOrderId, String symbol, long price, long quantity) {
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }

    public long getTradeId() { return tradeId; }
    public long getBuyOrderId() { return buyOrderId; }
    public long getSellOrderId() { return sellOrderId; }
    public String getSymbol() { return symbol; }
    public long getPrice() { return price; }
    public long getQuantity() { return quantity; }
    public long getTimestamp() { return timestamp; }
}
```

### OrderBookLevel.java
```java
package com.trading.model;

public class OrderBookLevel {
    private long price;
    private long totalQuantity;
    private int orderCount;

    public OrderBookLevel(long price, long totalQuantity, int orderCount) {
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.orderCount = orderCount;
    }

    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public long getTotalQuantity() { return totalQuantity; }
    public void setTotalQuantity(long totalQuantity) { this.totalQuantity = totalQuantity; }
    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public void addQuantity(long qty) { this.totalQuantity += qty; this.orderCount++; }
    public void removeQuantity(long qty) { this.totalQuantity -= qty; this.orderCount--; }
}
```

### MarketData.java
```java
package com.trading.model;

public class MarketData {
    private String symbol;
    private long bidPrice;
    private long askPrice;
    private long lastPrice;
    private long volume;
    private long timestamp;

    public MarketData(String symbol, long bidPrice, long askPrice, long lastPrice, long volume) {
        this.symbol = symbol;
        this.bidPrice = bidPrice;
        this.askPrice = askPrice;
        this.lastPrice = lastPrice;
        this.volume = volume;
        this.timestamp = System.nanoTime();
    }

    public String getSymbol() { return symbol; }
    public long getBidPrice() { return bidPrice; }
    public long getAskPrice() { return askPrice; }
    public long getLastPrice() { return lastPrice; }
    public long getVolume() { return volume; }
    public long getTimestamp() { return timestamp; }
    public long getMidPrice() { return (bidPrice + askPrice) / 2; }
    public long getSpread() { return askPrice - bidPrice; }
}
```

### Portfolio.java
```java
package com.trading.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Portfolio {
    private long userId;
    private long cashBalance;
    private final Map<String, Position> positions = new ConcurrentHashMap<>();
    private final Map<String, Long> averageEntryPrices = new ConcurrentHashMap<>();

    public Portfolio(long userId, long initialCash) {
        this.userId = userId;
        this.cashBalance = initialCash;
    }

    public long getUserId() { return userId; }
    public long getCashBalance() { return cashBalance; }
    public void setCashBalance(long cashBalance) { this.cashBalance = cashBalance; }

    public void addCash(long amount) { this.cashBalance += amount; }
    public void deductCash(long amount) { this.cashBalance -= amount; }

    public Position getPosition(String symbol) {
        return positions.getOrDefault(symbol, new Position(symbol, 0, 0));
    }

    public void updatePosition(String symbol, long quantityChange, long price) {
        Position pos = positions.computeIfAbsent(symbol, k -> new Position(symbol, 0, 0));
        long oldQuantity = pos.getQuantity();
        long newQuantity = oldQuantity + quantityChange;
        pos.setQuantity(newQuantity);

        if (newQuantity == 0) {
            averageEntryPrices.remove(symbol);
            pos.setAverageEntryPrice(0);
        } else if (quantityChange > 0) {
            long oldAvgPrice = averageEntryPrices.getOrDefault(symbol, 0L);
            long newAvg = oldQuantity > 0
                ? (oldAvgPrice * oldQuantity + price * quantityChange) / newQuantity
                : price;
            averageEntryPrices.put(symbol, newAvg);
            pos.setAverageEntryPrice(newAvg);
        }
        positions.put(symbol, pos);
    }

    public long getTotalUnrealizedPnl(Map<String, Long> currentPrices) {
        long total = 0;
        for (Map.Entry<String, Position> entry : positions.entrySet()) {
            String symbol = entry.getKey();
            Position pos = entry.getValue();
            Long currentPrice = currentPrices.get(symbol);
            if (currentPrice != null && pos.getQuantity() != 0 && pos.getAverageEntryPrice() > 0) {
                total += (currentPrice - pos.getAverageEntryPrice()) * pos.getQuantity();
            }
        }
        return total;
    }

    public long getTotalEquity(Map<String, Long> currentPrices) {
        return cashBalance + getTotalUnrealizedPnl(currentPrices);
    }
}
```

### Position.java
```java
package com.trading.model;

public class Position {
    private String symbol;
    private long quantity;
    private long averageEntryPrice;

    public Position() {}

    public Position(String symbol, long quantity, long averageEntryPrice) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageEntryPrice = averageEntryPrice;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public long getAverageEntryPrice() { return averageEntryPrice; }
    public void setAverageEntryPrice(long averageEntryPrice) { this.averageEntryPrice = averageEntryPrice; }
    public long getMarketValue(long currentPrice) { return quantity * currentPrice; }
    public long getUnrealizedPnl(long currentPrice) {
        return (currentPrice - averageEntryPrice) * quantity;
    }
}
```

---

## Ring Buffer Event Bus

### TradingEvent.java
```java
package com.trading.event;

public class TradingEvent {
    private EventType type;
    private long orderId;
    private long tradeId;
    private String symbol;
    private long price;
    private long quantity;
    private long timestamp;

    public enum EventType {
        ORDER_PLACED, ORDER_CANCELLED, ORDER_FILLED,
        TRADE_EXECUTED, MARKET_UPDATE, POSITION_UPDATE,
        RISK_ALERT, STOP_LOSS_TRIGGERED
    }

    public TradingEvent() {}

    public TradingEvent(EventType type, long orderId, String symbol, long price, long quantity) {
        this.type = type;
        this.orderId = orderId;
        this.symbol = symbol;
        this.price = price;
        this.quantity = quantity;
        this.timestamp = System.nanoTime();
    }

    public void reset() {
        this.type = null;
        this.orderId = 0;
        this.tradeId = 0;
        this.symbol = null;
        this.price = 0;
        this.quantity = 0;
        this.timestamp = 0;
    }

    public EventType getType() { return type; }
    public void setType(EventType type) { this.type = type; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getTradeId() { return tradeId; }
    public void setTradeId(long tradeId) { this.tradeId = tradeId; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }
    public long getTimestamp() { return timestamp; }
}
```

### RingBuffer.java
```java
package com.trading.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Component
public class RingBuffer {

    private static final Logger log = LoggerFactory.getLogger(RingBuffer.class);
    private static final int BUFFER_SIZE = 65536;
    private static final int MASK = BUFFER_SIZE - 1;

    private final AtomicReferenceArray<TradingEvent> buffer;
    private final AtomicLongArray sequence;
    private volatile long producerIndex = 0;
    private volatile long consumerIndex = 0;

    public RingBuffer() {
        this.buffer = new AtomicReferenceArray<>(BUFFER_SIZE);
        this.sequence = new AtomicLongArray(BUFFER_SIZE);
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer.set(i, new TradingEvent());
            sequence.set(i, -1);
        }
    }

    public boolean publish(TradingEvent event) {
        long seq = producerIndex++;
        int slot = (int) (seq & MASK);
        TradingEvent target = buffer.get(slot);
        target.setType(event.getType());
        target.setOrderId(event.getOrderId());
        target.setTradeId(event.getTradeId());
        target.setSymbol(event.getSymbol());
        target.setPrice(event.getPrice());
        target.setQuantity(event.getQuantity());
        target.setTimestamp(System.nanoTime());
        sequence.set(slot, seq);
        return true;
    }

    public TradingEvent consume() {
        long seq = consumerIndex;
        int slot = (int) (seq & MASK);
        if (sequence.get(slot) < seq) {
            return null;
        }
        TradingEvent event = buffer.get(slot);
        consumerIndex++;
        return event;
    }

    public void consumeAll(EventHandler handler) {
        TradingEvent event;
        int processed = 0;
        while ((event = consume()) != null) {
            try {
                handler.onEvent(event);
                processed++;
            } catch (Exception e) {
                log.error("Error processing event: {}", event.getType(), e);
            }
        }
    }

    @FunctionalInterface
    public interface EventHandler {
        void onEvent(TradingEvent event);
    }

    public int available() {
        return (int) (producerIndex - consumerIndex);
    }

    public long getProducerIndex() { return producerIndex; }
    public long getConsumerIndex() { return consumerIndex; }
}
```

---

## Order Book Matching Engine

### OrderBook.java
```java
package com.trading.engine;

import com.trading.event.RingBuffer;
import com.trading.event.TradingEvent;
import com.trading.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class OrderBook {

    private static final Logger log = LoggerFactory.getLogger(OrderBook.class);
    private final AtomicLong orderIdCounter = new AtomicLong(1);
    private final AtomicLong tradeIdCounter = new AtomicLong(1);

    private final Map<String, ConcurrentSkipListMap<Long, List<Order>>> buyOrders = new ConcurrentHashMap<>();
    private final Map<String, ConcurrentSkipListMap<Long, List<Order>>> sellOrders = new ConcurrentHashMap<>();
    private final Map<Long, Order> orderMap = new ConcurrentHashMap<>();
    private final Map<String, Long> lastPrices = new ConcurrentHashMap<>();

    private final RingBuffer eventBus;

    public OrderBook(RingBuffer eventBus) {
        this.eventBus = eventBus;
    }

    public Order placeOrder(String symbol, Order.Side side, Order.OrderType type, long price, long quantity, long userId) {
        long orderId = orderIdCounter.getAndIncrement();
        Order order = new Order(orderId, symbol, side, type, price, quantity, userId);
        orderMap.put(orderId, order);

        Map<String, ConcurrentSkipListMap<Long, List<Order>>> orders = (side == Order.Side.BUY) ? buyOrders : sellOrders;
        ConcurrentSkipListMap<Long, List<Order>> priceLevels = orders.computeIfAbsent(symbol, k -> new ConcurrentSkipListMap<>(
            side == Order.Side.BUY ? Comparator.reverseOrder() : Comparator.naturalOrder()
        ));
        priceLevels.computeIfAbsent(price, k -> Collections.synchronizedList(new ArrayList<>())).add(order);

        TradingEvent event = new TradingEvent(TradingEvent.EventType.ORDER_PLACED, orderId, symbol, price, quantity);
        eventBus.publish(event);

        matchOrders(symbol);
        return order;
    }

    private void matchOrders(String symbol) {
        ConcurrentSkipListMap<Long, List<Order>> buys = buyOrders.get(symbol);
        ConcurrentSkipListMap<Long, List<Order>> sells = sellOrders.get(symbol);
        if (buys == null || sells == null || buys.isEmpty() || sells.isEmpty()) return;

        boolean matched;
        do {
            matched = false;
            Map.Entry<Long, List<Order>> bestBid = buys.firstEntry();
            Map.Entry<Long, List<Order>> bestAsk = sells.firstEntry();

            if (bestBid == null || bestAsk == null) break;
            if (bestBid.getKey() < bestAsk.getKey()) break;

            long matchPrice = bestAsk.getKey();
            List<Order> bidOrders = bestBid.getValue();
            List<Order> askOrders = bestAsk.getValue();

            Order buyOrder = bidOrders.get(0);
            Order sellOrder = askOrders.get(0);
            long matchQuantity = Math.min(buyOrder.getRemainingQuantity(), sellOrder.getRemainingQuantity());

            if (matchQuantity > 0) {
                Trade trade = new Trade(
                    tradeIdCounter.getAndIncrement(),
                    buyOrder.getOrderId(),
                    sellOrder.getOrderId(),
                    symbol,
                    matchPrice,
                    matchQuantity
                );

                buyOrder.setFilledQuantity(buyOrder.getFilledQuantity() + matchQuantity);
                buyOrder.setRemainingQuantity(buyOrder.getRemainingQuantity() - matchQuantity);
                sellOrder.setFilledQuantity(sellOrder.getFilledQuantity() + matchQuantity);
                sellOrder.setRemainingQuantity(sellOrder.getRemainingQuantity() - matchQuantity);

                if (buyOrder.getRemainingQuantity() == 0) {
                    buyOrder.setStatus(Order.OrderStatus.FILLED);
                    bidOrders.remove(0);
                } else {
                    buyOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }
                if (sellOrder.getRemainingQuantity() == 0) {
                    sellOrder.setStatus(Order.OrderStatus.FILLED);
                    askOrders.remove(0);
                } else {
                    sellOrder.setStatus(Order.OrderStatus.PARTIALLY_FILLED);
                }

                if (bidOrders.isEmpty()) buys.remove(bestBid.getKey());
                if (askOrders.isEmpty()) sells.remove(bestAsk.getKey());

                lastPrices.put(symbol, matchPrice);

                TradingEvent execEvent = new TradingEvent(TradingEvent.EventType.TRADE_EXECUTED, 0, symbol, matchPrice, matchQuantity);
                execEvent.setTradeId(trade.getTradeId());
                eventBus.publish(execEvent);

                matched = true;
            }
        } while (matched);
    }

    public boolean cancelOrder(long orderId) {
        Order order = orderMap.get(orderId);
        if (order == null || order.getStatus() == Order.OrderStatus.FILLED) return false;

        order.setStatus(Order.OrderStatus.CANCELLED);
        Map<String, ConcurrentSkipListMap<Long, List<Order>>> orders = (order.getSide() == Order.Side.BUY) ? buyOrders : sellOrders;
        ConcurrentSkipListMap<Long, List<Order>> priceLevels = orders.get(order.getSymbol());
        if (priceLevels != null) {
            List<Order> level = priceLevels.get(order.getPrice());
            if (level != null) {
                level.removeIf(o -> o.getOrderId() == orderId);
                if (level.isEmpty()) priceLevels.remove(order.getPrice());
            }
        }

        TradingEvent event = new TradingEvent(TradingEvent.EventType.ORDER_CANCELLED, orderId, order.getSymbol(), order.getPrice(), order.getRemainingQuantity());
        eventBus.publish(event);
        return true;
    }

    public Order modifyOrder(long orderId, long newPrice, long newQuantity) {
        Order existing = orderMap.get(orderId);
        if (existing == null || existing.getStatus() == Order.OrderStatus.FILLED) return null;
        cancelOrder(orderId);
        return placeOrder(existing.getSymbol(), existing.getSide(), existing.getType(), newPrice, newQuantity, existing.getUserId());
    }

    public Order getOrder(long orderId) {
        return orderMap.get(orderId);
    }

    public List<OrderBookLevel> getBids(String symbol) {
        return getLevels(buyOrders.get(symbol), true);
    }

    public List<OrderBookLevel> getAsks(String symbol) {
        return getLevels(sellOrders.get(symbol), false);
    }

    private List<OrderBookLevel> getLevels(ConcurrentSkipListMap<Long, List<Order>> priceLevels, boolean reverse) {
        List<OrderBookLevel> levels = new ArrayList<>();
        if (priceLevels == null) return levels;
        for (Map.Entry<Long, List<Order>> entry : priceLevels.entrySet()) {
            long totalQty = entry.getValue().stream().mapToLong(Order::getRemainingQuantity).sum();
            levels.add(new OrderBookLevel(entry.getKey(), totalQty, entry.getValue().size()));
        }
        return levels;
    }

    public Long getLastPrice(String symbol) {
        return lastPrices.get(symbol);
    }

    public OrderBookSnapshot getSnapshot(String symbol) {
        OrderBookSnapshot snapshot = new OrderBookSnapshot();
        snapshot.setSymbol(symbol);
        snapshot.setBids(getBids(symbol));
        snapshot.setAsks(getAsks(symbol));
        snapshot.setLastPrice(lastPrices.get(symbol));
        snapshot.setTimestamp(System.nanoTime());
        return snapshot;
    }
}
```

### OrderBookSnapshot.java
```java
package com.trading.model;

import java.util.List;

public class OrderBookSnapshot {
    private String symbol;
    private List<OrderBookLevel> bids;
    private List<OrderBookLevel> asks;
    private Long lastPrice;
    private long timestamp;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
    public List<OrderBookLevel> getBids() { return bids; }
    public void setBids(List<OrderBookLevel> bids) { this.bids = bids; }
    public List<OrderBookLevel> getAsks() { return asks; }
    public void setAsks(List<OrderBookLevel> asks) { this.asks = asks; }
    public Long getLastPrice() { return lastPrice; }
    public void setLastPrice(Long lastPrice) { this.lastPrice = lastPrice; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
```

---

## Market Data Feed

### MarketDataFeed.java
```java
package com.trading.marketdata;

import com.trading.event.RingBuffer;
import com.trading.event.TradingEvent;
import com.trading.model.MarketData;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class MarketDataFeed {

    private static final Logger log = LoggerFactory.getLogger(MarketDataFeed.class);
    private final RingBuffer eventBus;
    private final Random random = new Random(42);

    @Value("${trading.market-data.symbols}")
    private List<String> symbols;

    @Value("${trading.market-data.tick-interval-ms}")
    private long tickIntervalMs;

    private final Map<String, MarketData> latestData = new ConcurrentHashMap<>();
    private final List<MarketDataListener> listeners = new CopyOnWriteArrayList<>();

    public MarketDataFeed(RingBuffer eventBus) {
        this.eventBus = eventBus;
    }

    @PostConstruct
    public void init() {
        for (String symbol : symbols) {
            long basePrice = getBasePrice(symbol);
            latestData.put(symbol, new MarketData(symbol, basePrice - 5, basePrice + 5, basePrice, 1000000));
        }
        log.info("Initialized market data feed for {} symbols", symbols.size());
    }

    @Scheduled(fixedDelayString = "${trading.market-data.tick-interval-ms}")
    public void tick() {
        for (String symbol : symbols) {
            MarketData current = latestData.get(symbol);
            long movement = (long) (random.nextGaussian() * 10);
            long lastPrice = current.getLastPrice() + movement;
            if (lastPrice < 100) lastPrice = 100;

            long bidPrice = lastPrice - Math.abs((long) (random.nextGaussian() * 2 + 1));
            long askPrice = lastPrice + Math.abs((long) (random.nextGaussian() * 2 + 1));
            long volume = Math.abs((long) (random.nextGaussian() * 10000 + 50000));

            MarketData md = new MarketData(symbol, bidPrice, askPrice, lastPrice, volume);
            latestData.put(symbol, md);

            TradingEvent event = new TradingEvent(TradingEvent.EventType.MARKET_UPDATE, 0, symbol, lastPrice, volume);
            eventBus.publish(event);

            for (MarketDataListener listener : listeners) {
                listener.onMarketData(md);
            }
        }
    }

    public MarketData getLatest(String symbol) {
        return latestData.get(symbol);
    }

    public Map<String, MarketData> getAllLatest() {
        return Map.copyOf(latestData);
    }

    public void addListener(MarketDataListener listener) {
        listeners.add(listener);
    }

    public interface MarketDataListener {
        void onMarketData(MarketData data);
    }

    private long getBasePrice(String symbol) {
        return switch (symbol) {
            case "AAPL" -> 15000; case "GOOGL" -> 14000; case "MSFT" -> 33000;
            case "AMZN" -> 14500; case "TSLA" -> 22000; case "NVDA" -> 45000;
            case "META" -> 31000; case "JPM" -> 15500; case "V" -> 25000;
            case "JNJ" -> 16000; default -> 10000;
        };
    }
}
```

---

## Risk Manager

### RiskManager.java
```java
package com.trading.risk;

import com.trading.event.RingBuffer;
import com.trading.event.TradingEvent;
import com.trading.model.Order;
import com.trading.model.Portfolio;
import com.trading.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RiskManager {

    private static final Logger log = LoggerFactory.getLogger(RiskManager.class);

    @Value("${trading.risk.max-position-size}")
    private long maxPositionSize;

    @Value("${trading.risk.max-order-value}")
    private long maxOrderValue;

    @Value("${trading.risk.max-leverage}")
    private int maxLeverage;

    @Value("${trading.risk.stop-loss-default-pct}")
    private double stopLossDefaultPct;

    private final RingBuffer eventBus;
    private final Map<Long, Map<String, Long>> stopLosses = new ConcurrentHashMap<>();

    public RiskManager(RingBuffer eventBus) {
        this.eventBus = eventBus;
    }

    public boolean checkOrder(Order order, Portfolio portfolio, Map<String, Long> currentPrices) {
        if (order.getQuantity() <= 0) {
            log.warn("Order rejected: invalid quantity {}", order.getQuantity());
            return false;
        }

        long orderValue = order.getPrice() * order.getQuantity();
        if (orderValue > maxOrderValue) {
            log.warn("Order rejected: value {} exceeds max {}", orderValue, maxOrderValue);
            return false;
        }

        Position existingPos = portfolio.getPosition(order.getSymbol());
        long newPositionSize = order.getSide() == Order.Side.BUY
            ? existingPos.getQuantity() + order.getQuantity()
            : existingPos.getQuantity() - order.getQuantity();

        if (Math.abs(newPositionSize) > maxPositionSize) {
            log.warn("Order rejected: position size {} exceeds max {}", newPositionSize, maxPositionSize);
            return false;
        }

        long totalExposure = orderValue;
        for (Map.Entry<String, Position> entry : portfolio.getPositions().entrySet()) {
            Long price = currentPrices.get(entry.getKey());
            if (price != null) {
                totalExposure += Math.abs(entry.getValue().getQuantity() * price);
            }
        }

        if (totalExposure > portfolio.getCashBalance() * maxLeverage) {
            log.warn("Order rejected: exposure {} exceeds leverage limit", totalExposure);
            return false;
        }

        return true;
    }

    public void setStopLoss(long userId, String symbol, long stopPrice) {
        stopLosses.computeIfAbsent(userId, k -> new ConcurrentHashMap<>())
            .put(symbol, stopPrice);
        log.info("Stop-loss set for user {} on {} at {}", userId, symbol, stopPrice);
    }

    public void checkStopLosses(long userId, Portfolio portfolio, Map<String, Long> currentPrices) {
        Map<String, Long> userStops = stopLosses.get(userId);
        if (userStops == null) return;

        for (Map.Entry<String, Long> entry : userStops.entrySet()) {
            String symbol = entry.getKey();
            Long stopPrice = entry.getValue();
            Long currentPrice = currentPrices.get(symbol);
            Position pos = portfolio.getPosition(symbol);

            if (currentPrice != null && pos != null && pos.getQuantity() > 0) {
                if (currentPrice <= stopPrice) {
                    log.warn("Stop-loss triggered for user {} on {} at price {} (stop: {})",
                        userId, symbol, currentPrice, stopPrice);

                    TradingEvent event = new TradingEvent(TradingEvent.EventType.STOP_LOSS_TRIGGERED,
                        0, symbol, currentPrice, pos.getQuantity());
                    eventBus.publish(event);

                    userStops.remove(symbol);
                }
            }
        }
    }

    public long calculateDefaultStopPrice(String symbol, long entryPrice) {
        return (long) (entryPrice * (1 - stopLossDefaultPct / 100.0));
    }
}
```

---

## Portfolio Manager

### PortfolioManager.java
```java
package com.trading.portfolio;

import com.trading.event.RingBuffer;
import com.trading.event.TradingEvent;
import com.trading.model.Portfolio;
import com.trading.model.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PortfolioManager {

    private static final Logger log = LoggerFactory.getLogger(PortfolioManager.class);
    private final RingBuffer eventBus;
    private final Map<Long, Portfolio> portfolios = new ConcurrentHashMap<>();

    public PortfolioManager(RingBuffer eventBus) {
        this.eventBus = eventBus;
    }

    public Portfolio createPortfolio(long userId, long initialCash) {
        Portfolio portfolio = new Portfolio(userId, initialCash);
        portfolios.put(userId, portfolio);
        log.info("Created portfolio for user {} with cash {}", userId, initialCash);
        return portfolio;
    }

    public Portfolio getPortfolio(long userId) {
        return portfolios.computeIfAbsent(userId, k -> createPortfolio(userId, 10000000));
    }

    public void processTrade(long userId, String symbol, long quantity, long price) {
        Portfolio portfolio = getPortfolio(userId);

        if (quantity > 0) {
            portfolio.deductCash(quantity * price);
        } else {
            portfolio.addCash(Math.abs(quantity) * price);
        }

        portfolio.updatePosition(symbol, quantity, price);

        TradingEvent event = new TradingEvent(TradingEvent.EventType.POSITION_UPDATE, 0, symbol, price, Math.abs(quantity));
        eventBus.publish(event);

        log.info("Processed trade for user {}: {} {} @ {}", userId,
            quantity > 0 ? "BUY" : "SELL", Math.abs(quantity), symbol, price);
    }

    public Map<String, Position> getPositions(long userId) {
        return getPortfolio(userId).getPositions();
    }

    public long getCashBalance(long userId) {
        return getPortfolio(userId).getCashBalance();
    }

    public void addFunds(long userId, long amount) {
        getPortfolio(userId).addCash(amount);
    }
}
```

---

## Order Management Service

### OrderManagementService.java
```java
package com.trading.service;

import com.trading.engine.OrderBook;
import com.trading.model.*;
import com.trading.portfolio.PortfolioManager;
import com.trading.risk.RiskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OrderManagementService {

    private static final Logger log = LoggerFactory.getLogger(OrderManagementService.class);
    private final OrderBook orderBook;
    private final RiskManager riskManager;
    private final PortfolioManager portfolioManager;

    public OrderManagementService(OrderBook orderBook, RiskManager riskManager,
                                   PortfolioManager portfolioManager) {
        this.orderBook = orderBook;
        this.riskManager = riskManager;
        this.portfolioManager = portfolioManager;
    }

    public Order placeOrder(long userId, String symbol, Order.Side side,
                             Order.OrderType type, long price, long quantity) {
        Order order = new Order(0, symbol, side, type, price, quantity, userId);
        Portfolio portfolio = portfolioManager.getPortfolio(userId);
        Map<String, Long> prices = Map.of(symbol, orderBook.getLastPrice(symbol) != null ? orderBook.getLastPrice(symbol) : price);

        if (!riskManager.checkOrder(order, portfolio, prices)) {
            order.setStatus(Order.OrderStatus.REJECTED);
            log.warn("Order rejected by risk manager for user {}", userId);
            return order;
        }

        Order placed = orderBook.placeOrder(symbol, side, type, price, quantity, userId);

        long netQuantity = side == Order.Side.BUY ? placed.getFilledQuantity() : -placed.getFilledQuantity();
        if (netQuantity != 0) {
            portfolioManager.processTrade(userId, symbol, netQuantity, price);
        }

        return placed;
    }

    public boolean cancelOrder(long userId, long orderId) {
        Order order = orderBook.getOrder(orderId);
        if (order == null || order.getUserId() != userId) {
            return false;
        }
        return orderBook.cancelOrder(orderId);
    }

    public Order modifyOrder(long userId, long orderId, long newPrice, long newQuantity) {
        Order existing = orderBook.getOrder(orderId);
        if (existing == null || existing.getUserId() != userId) {
            return null;
        }
        cancelOrder(userId, orderId);
        return placeOrder(userId, existing.getSymbol(), existing.getSide(),
            existing.getType(), newPrice, newQuantity);
    }

    public Order getOrderStatus(long orderId) {
        return orderBook.getOrder(orderId);
    }

    public void setStopLoss(long userId, String symbol, long stopPrice) {
        riskManager.setStopLoss(userId, symbol, stopPrice);
    }

    public OrderBookSnapshot getOrderBook(String symbol) {
        return orderBook.getSnapshot(symbol);
    }
}
```

---

## REST Controller

### TradingController.java
```java
package com.trading.controller;

import com.trading.engine.OrderBook;
import com.trading.model.*;
import com.trading.portfolio.PortfolioManager;
import com.trading.service.OrderManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class TradingController {

    private final OrderManagementService orderService;
    private final OrderBook orderBook;
    private final PortfolioManager portfolioManager;

    public TradingController(OrderManagementService orderService, OrderBook orderBook,
                              PortfolioManager portfolioManager) {
        this.orderService = orderService;
        this.orderBook = orderBook;
        this.portfolioManager = portfolioManager;
    }

    @PostMapping("/orders")
    public ResponseEntity<Order> placeOrder(@RequestBody Map<String, Object> body) {
        long userId = Long.parseLong(body.get("userId").toString());
        String symbol = (String) body.get("symbol");
        Order.Side side = Order.Side.valueOf((String) body.get("side"));
        Order.OrderType type = Order.OrderType.valueOf((String) body.get("type"));
        long price = Long.parseLong(body.get("price").toString());
        long quantity = Long.parseLong(body.get("quantity").toString());

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(orderService.placeOrder(userId, symbol, side, type, price, quantity));
    }

    @DeleteMapping("/orders/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @PathVariable long orderId, @RequestParam long userId) {
        if (orderService.cancelOrder(userId, orderId)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PutMapping("/orders/{orderId}")
    public ResponseEntity<Order> modifyOrder(
            @PathVariable long orderId,
            @RequestParam long userId,
            @RequestParam long price,
            @RequestParam long quantity) {
        Order modified = orderService.modifyOrder(userId, orderId, price, quantity);
        if (modified != null) return ResponseEntity.ok(modified);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrder(@PathVariable long orderId) {
        Order order = orderService.getOrderStatus(orderId);
        if (order != null) return ResponseEntity.ok(order);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/orderbook/{symbol}")
    public ResponseEntity<OrderBookSnapshot> getOrderBook(@PathVariable String symbol) {
        return ResponseEntity.ok(orderService.getOrderBook(symbol.toUpperCase()));
    }

    @GetMapping("/portfolio/{userId}")
    public ResponseEntity<Map<String, Object>> getPortfolio(@PathVariable long userId) {
        Portfolio portfolio = portfolioManager.getPortfolio(userId);
        Map<String, Position> positions = portfolio.getPositions();
        Map<String, Long> currentPrices = new HashMap<>();
        for (String symbol : positions.keySet()) {
            Long lastPrice = orderBook.getLastPrice(symbol);
            if (lastPrice != null) currentPrices.put(symbol, lastPrice);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("userId", userId);
        response.put("cashBalance", portfolio.getCashBalance());
        response.put("totalEquity", portfolio.getTotalEquity(currentPrices));
        response.put("unrealizedPnl", portfolio.getTotalUnrealizedPnl(currentPrices));
        response.put("positions", positions);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/stop-loss")
    public ResponseEntity<Void> setStopLoss(@RequestBody Map<String, Object> body) {
        long userId = Long.parseLong(body.get("userId").toString());
        String symbol = (String) body.get("symbol");
        long stopPrice = Long.parseLong(body.get("stopPrice").toString());
        orderService.setStopLoss(userId, symbol, stopPrice);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/portfolio/{userId}/funds")
    public ResponseEntity<Void> addFunds(
            @PathVariable long userId, @RequestParam long amount) {
        portfolioManager.addFunds(userId, amount);
        return ResponseEntity.ok().build();
    }
}
```

---

## WebSocket Market Data

### MarketDataWebSocketHandler.java
```java
package com.trading.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.marketdata.MarketDataFeed;
import com.trading.model.MarketData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class MarketDataWebSocketHandler extends TextWebSocketHandler
        implements MarketDataFeed.MarketDataListener {

    private static final Logger log = LoggerFactory.getLogger(MarketDataWebSocketHandler.class);
    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        log.info("WebSocket connected: {}", session.getId());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        log.info("WebSocket disconnected: {}", session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        if ("subscribe".equals(payload)) {
            session.sendMessage(new TextMessage("{\"status\":\"subscribed\"}"));
        }
    }

    @Override
    public void onMarketData(MarketData data) {
        try {
            String json = objectMapper.writeValueAsString(data);
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (Exception e) {
            log.error("Error sending WebSocket message", e);
        }
    }
}
```

### WebSocketConfig.java
```java
package com.trading.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarketDataWebSocketHandler marketDataHandler;

    public WebSocketConfig(MarketDataWebSocketHandler marketDataHandler) {
        this.marketDataHandler = marketDataHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(marketDataHandler, "/ws/market-data")
            .setAllowedOrigins("*");
    }
}
```

---

## Event Bus Consumer

### EventBusConsumer.java
```java
package com.trading.event;

import com.trading.portfolio.PortfolioManager;
import com.trading.risk.RiskManager;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class EventBusConsumer {

    private static final Logger log = LoggerFactory.getLogger(EventBusConsumer.class);
    private final RingBuffer ringBuffer;
    private final PortfolioManager portfolioManager;
    private final RiskManager riskManager;
    private final Map<Long, Map<String, Long>> lastPrices = new ConcurrentHashMap<>();
    private final AtomicLong totalEventsProcessed = new AtomicLong(0);

    public EventBusConsumer(RingBuffer ringBuffer, PortfolioManager portfolioManager,
                             RiskManager riskManager) {
        this.ringBuffer = ringBuffer;
        this.portfolioManager = portfolioManager;
        this.riskManager = riskManager;
    }

    @PostConstruct
    public void startConsumer() {
        Thread consumer = new Thread(() -> {
            while (true) {
                try {
                    ringBuffer.consumeAll(this::processEvent);
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "event-bus-consumer");
        consumer.setDaemon(true);
        consumer.start();
        log.info("Event bus consumer started");
    }

    private void processEvent(TradingEvent event) {
        totalEventsProcessed.incrementAndGet();

        switch (event.getType()) {
            case TRADE_EXECUTED -> {
                lastPrices.computeIfAbsent(0L, k -> new ConcurrentHashMap<>())
                    .put(event.getSymbol(), event.getPrice());
            }
            case MARKET_UPDATE -> {
                for (Map.Entry<Long, Map<String, Long>> entry : lastPrices.entrySet()) {
                    entry.getValue().put(event.getSymbol(), event.getPrice());
                }
            }
            default -> {}
        }
    }

    public long getTotalEventsProcessed() {
        return totalEventsProcessed.get();
    }
}
```

---

## Market Data REST Controller

### MarketDataController.java
```java
package com.trading.controller;

import com.trading.marketdata.MarketDataFeed;
import com.trading.model.MarketData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class MarketDataController {

    private final MarketDataFeed marketDataFeed;

    public MarketDataController(MarketDataFeed marketDataFeed) {
        this.marketDataFeed = marketDataFeed;
    }

    @GetMapping("/{symbol}")
    public ResponseEntity<MarketData> getMarketData(@PathVariable String symbol) {
        MarketData data = marketDataFeed.getLatest(symbol.toUpperCase());
        if (data == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @GetMapping
    public ResponseEntity<Map<String, MarketData>> getAllMarketData() {
        return ResponseEntity.ok(marketDataFeed.getAllLatest());
    }
}
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/orders` | Place a new order |
| DELETE | `/api/orders/{id}?userId=X` | Cancel an order |
| PUT | `/api/orders/{id}?userId=X&price=Y&quantity=Z` | Modify an order |
| GET | `/api/orders/{id}` | Get order status |
| GET | `/api/orderbook/{symbol}` | Get order book snapshot |
| GET | `/api/portfolio/{userId}` | Get portfolio summary |
| POST | `/api/stop-loss` | Set stop-loss |
| POST | `/api/portfolio/{userId}/funds?amount=X` | Add funds |
| GET | `/api/market/{symbol}` | Get market data for symbol |
| GET | `/api/market` | Get all market data |
| WS | `/ws/market-data` | Real-time market data stream |

## Running the Application

```bash
# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Place a buy order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"symbol":"AAPL","side":"BUY","type":"LIMIT","price":15000,"quantity":100}'

# Place a sell order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"userId":2,"symbol":"AAPL","side":"SELL","type":"LIMIT","price":14950,"quantity":100}'

# View order book
curl http://localhost:8080/api/orderbook/AAPL

# View portfolio
curl http://localhost:8080/api/portfolio/1

# Set stop-loss
curl -X POST http://localhost:8080/api/stop-loss \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"symbol":"AAPL","stopPrice":14000}'

# Get market data
curl http://localhost:8080/api/market/AAPL

# Connect to WebSocket for real-time data
# wscat -c ws://localhost:8080/ws/market-data
```
