package com.hft.model;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

public class OrderBook {
    private final int symbolId;
    private final NavigableMap<Long, List<Order>> bids = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final NavigableMap<Long, List<Order>> asks = new ConcurrentSkipListMap<>();
    private final AtomicLong orderIdCounter = new AtomicLong(1);

    public OrderBook(int symbolId) {
        this.symbolId = symbolId;
    }

    public int getSymbolId() { return symbolId; }

    public long addOrder(Side side, long price, long quantity, OrderType type) {
        long orderId = orderIdCounter.getAndIncrement();
        Order order = new Order(orderId, symbolId, side, price, quantity, 0, type, System.nanoTime(), OrderStatus.NEW);
        if (side == Side.BUY) {
            bids.computeIfAbsent(price, k -> Collections.synchronizedList(new ArrayList<>())).add(order);
        } else {
            asks.computeIfAbsent(price, k -> Collections.synchronizedList(new ArrayList<>())).add(order);
        }
        return orderId;
    }

    public boolean cancelOrder(long orderId, Side side, long price) {
        Map<Long, List<Order>> book = side == Side.BUY ? bids : asks;
        List<Order> orders = book.get(price);
        if (orders == null) return false;
        synchronized (orders) {
            Iterator<Order> it = orders.iterator();
            while (it.hasNext()) {
                Order o = it.next();
                if (o.orderId() == orderId) {
                    it.remove();
                    if (orders.isEmpty()) book.remove(price);
                    return true;
                }
            }
        }
        return false;
    }

    public List<Trade> match() {
        List<Trade> trades = new ArrayList<>();
        while (!bids.isEmpty() && !asks.isEmpty()) {
            Map.Entry<Long, List<Order>> bestBid = bids.firstEntry();
            Map.Entry<Long, List<Order>> bestAsk = asks.firstEntry();
            if (bestBid == null || bestAsk == null) break;
            long bidPrice = bestBid.getKey();
            long askPrice = bestAsk.getKey();
            if (bidPrice < askPrice) break;
            long matchPrice = askPrice;
            List<Order> bidOrders = bestBid.getValue();
            List<Order> askOrders = bestAsk.getValue();
            Order bidOrder = bidOrders.get(0);
            Order askOrder = askOrders.get(0);
            long matchQty = Math.min(bidOrder.remainingQuantity(), askOrder.remainingQuantity());
            long tradeId = System.nanoTime();
            trades.add(new Trade(tradeId, bidOrder.orderId(), askOrder.orderId(),
                symbolId, matchPrice, matchQty, System.nanoTime()));
            updateFilled(bidOrders, bidOrder, matchQty, bids, bestBid.getKey());
            updateFilled(askOrders, askOrder, matchQty, asks, bestAsk.getKey());
        }
        return trades;
    }

    private void updateFilled(List<Order> orders, Order order, long fillQty,
                              NavigableMap<Long, List<Order>> book, long price) {
        synchronized (orders) {
            orders.remove(order);
            long newFilled = order.filledQuantity() + fillQty;
            if (newFilled < order.quantity()) {
                orders.add(new Order(order.orderId(), order.symbolId(), order.side(),
                    order.price(), order.quantity(), newFilled, order.type(),
                    order.timestampNanos(), OrderStatus.PARTIALLY_FILLED));
            }
            if (orders.isEmpty()) {
                book.remove(price);
            }
        }
    }

    public long getBidDepth() {
        return bids.values().stream().mapToLong(List::size).sum();
    }

    public long getAskDepth() {
        return asks.values().stream().mapToLong(List::size).sum();
    }

    public Long getBestBid() {
        Map.Entry<Long, List<Order>> entry = bids.firstEntry();
        return entry != null ? entry.getKey() : null;
    }

    public Long getBestAsk() {
        Map.Entry<Long, List<Order>> entry = asks.firstEntry();
        return entry != null ? entry.getKey() : null;
    }

    public MarketData snapshot() {
        return new MarketData(symbolId,
            getBestBid() != null ? getBestBid() : 0,
            getBestAsk() != null ? getBestAsk() : 0,
            getBidDepth(), getAskDepth(),
            0, 0, System.nanoTime());
    }
}
