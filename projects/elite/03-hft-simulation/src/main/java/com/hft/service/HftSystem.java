package com.hft.service;

import com.hft.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadLocalRandom;

public class HftSystem {
    private static final Logger log = LoggerFactory.getLogger(HftSystem.class);

    private final OrderManagementSystem oms;
    private final RiskEngine riskEngine;
    private MarketDataFeed marketDataFeed;
    private volatile boolean running = false;

    public HftSystem() {
        this.riskEngine = new RiskEngine();
        this.oms = new OrderManagementSystem(riskEngine);
    }

    public void start() {
        log.info("Starting HFT System...");
        oms.addOrderBook(1);
        oms.addOrderBook(2);
        oms.addOrderBook(3);

        marketDataFeed = new MarketDataFeed(1, 10_000, 10);
        marketDataFeed.start(data -> {
            if (running && ThreadLocalRandom.current().nextDouble() < 0.1) {
                Side side = ThreadLocalRandom.current().nextBoolean() ? Side.BUY : Side.SELL;
                long price = side == Side.BUY ? data.bidPrice() : data.askPrice();
                long qty = ThreadLocalRandom.current().nextLong(100, 1000);
                oms.placeOrder(data.symbolId(), side, price, qty, OrderType.LIMIT);
            }
        });
        running = true;
        log.info("HFT System started with 3 symbols.");
    }

    public void stop() {
        log.info("Stopping HFT System...");
        running = false;
        if (marketDataFeed != null) marketDataFeed.stop();
        oms.shutdown();
        log.info("HFT System stopped.");
    }
}
