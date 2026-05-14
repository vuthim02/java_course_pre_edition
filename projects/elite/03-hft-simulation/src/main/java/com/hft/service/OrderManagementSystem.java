package com.hft.service;

import com.hft.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;

public class OrderManagementSystem {
    private static final Logger log = LoggerFactory.getLogger(OrderManagementSystem.class);

    private final Map<Integer, OrderBook> orderBooks = new ConcurrentHashMap<>();
    private final RiskEngine riskEngine;
    private final ExecutorService executor = Executors.newFixedThreadPool(4);
    private final List<Trade> tradeHistory = new CopyOnWriteArrayList<>();

    public OrderManagementSystem(RiskEngine riskEngine) {
        this.riskEngine = riskEngine;
    }

    public void addOrderBook(int symbolId) {
        orderBooks.put(symbolId, new OrderBook(symbolId));
        log.info("Added OrderBook for symbol {}", symbolId);
    }

    public CompletableFuture<OrderResult> placeOrder(int symbolId, Side side, long price, long quantity, OrderType type) {
        return CompletableFuture.supplyAsync(() -> {
            OrderBook book = orderBooks.get(symbolId);
            if (book == null) {
                return new OrderResult(-1, OrderResultStatus.REJECTED_SYMBOL_NOT_FOUND);
            }
            long orderId = book.addOrder(side, price, quantity, type);
            Order order = new Order(orderId, symbolId, side, price, quantity, 0, type, System.nanoTime(), OrderStatus.NEW);
            RiskEngine.RiskCheckResult riskCheck = riskEngine.checkOrder(order);
            if (riskCheck != RiskEngine.RiskCheckResult.APPROVED) {
                book.cancelOrder(orderId, side, price);
                return new OrderResult(orderId, OrderResultStatus.REJECTED_RISK_CHECK);
            }
            riskEngine.onOrderAccepted(order);
            List<Trade> trades = book.match();
            for (Trade trade : trades) {
                riskEngine.onTrade(trade.symbolId(), trade.quantity());
                tradeHistory.add(trade);
                log.debug("Trade executed: {}", trade);
            }
            return new OrderResult(orderId, OrderResultStatus.FILLED);
        }, executor);
    }

    public boolean cancelOrder(int symbolId, long orderId, Side side, long price) {
        OrderBook book = orderBooks.get(symbolId);
        if (book == null) return false;
        return book.cancelOrder(orderId, side, price);
    }

    public OrderBook getOrderBook(int symbolId) {
        return orderBooks.get(symbolId);
    }

    public List<Trade> getTradeHistory() {
        return List.copyOf(tradeHistory);
    }

    public void shutdown() {
        executor.shutdown();
    }

    public record OrderResult(long orderId, OrderResultStatus status) {}

    public enum OrderResultStatus {
        FILLED, PARTIALLY_FILLED, REJECTED_RISK_CHECK, REJECTED_SYMBOL_NOT_FOUND
    }
}
