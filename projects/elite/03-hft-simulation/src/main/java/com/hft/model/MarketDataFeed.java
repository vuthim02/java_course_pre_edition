package com.hft.model;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public class MarketDataFeed {
    private final int symbolId;
    private long basePrice;
    private final long tickSize;
    private volatile boolean running = true;

    public MarketDataFeed(int symbolId, long basePrice, long tickSize) {
        this.symbolId = symbolId;
        this.basePrice = basePrice;
        this.tickSize = tickSize;
    }

    public void start(Consumer<MarketData> handler) {
        Thread feedThread = new Thread(() -> {
            while (running) {
                long bidPrice = basePrice - ThreadLocalRandom.current().nextLong(tickSize, tickSize * 5);
                long askPrice = basePrice + ThreadLocalRandom.current().nextLong(tickSize, tickSize * 5);
                long bidSize = ThreadLocalRandom.current().nextLong(100, 10000);
                long askSize = ThreadLocalRandom.current().nextLong(100, 10000);
                long lastPrice = ThreadLocalRandom.current().nextBoolean() ? bidPrice : askPrice;
                long volume = ThreadLocalRandom.current().nextLong(1000, 100000);

                MarketData data = new MarketData(symbolId, bidPrice, askPrice, bidSize, askSize,
                    lastPrice, volume, System.nanoTime());
                handler.accept(data);

                double change = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * tickSize;
                basePrice += (long) change;
                if (basePrice < tickSize) basePrice = tickSize * 10;

                try {
                    Thread.sleep(ThreadLocalRandom.current().nextLong(10, 100));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "market-data-" + symbolId);
        feedThread.setDaemon(true);
        feedThread.start();
    }

    public void stop() {
        running = false;
    }
}
