package com.trading.service;

import com.trading.model.MarketPrice;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class MarketDataService {
    private final Map<String, MarketPrice> prices = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        prices.put("AAPL", new MarketPrice("AAPL", new BigDecimal("175.30"), new BigDecimal("175.35"),
                new BigDecimal("175.32"), 0.5, 45000000, LocalDateTime.now()));
        prices.put("GOOGL", new MarketPrice("GOOGL", new BigDecimal("141.50"), new BigDecimal("141.55"),
                new BigDecimal("141.52"), -0.2, 22000000, LocalDateTime.now()));
        prices.put("MSFT", new MarketPrice("MSFT", new BigDecimal("378.90"), new BigDecimal("378.95"),
                new BigDecimal("378.92"), 0.8, 31000000, LocalDateTime.now()));
        prices.put("AMZN", new MarketPrice("AMZN", new BigDecimal("178.20"), new BigDecimal("178.25"),
                new BigDecimal("178.22"), 0.3, 28000000, LocalDateTime.now()));
        prices.put("TSLA", new MarketPrice("TSLA", new BigDecimal("245.60"), new BigDecimal("245.65"),
                new BigDecimal("245.62"), -1.2, 55000000, LocalDateTime.now()));
    }

    public Optional<MarketPrice> getPrice(String symbol) {
        return Optional.ofNullable(prices.get(symbol.toUpperCase()));
    }

    public BigDecimal getLatestPrice(String symbol) {
        var price = prices.get(symbol.toUpperCase());
        return price != null ? price.last() : BigDecimal.ZERO;
    }

    public Map<String, MarketPrice> getAllPrices() {
        return prices;
    }

    public void simulateTick() {
        prices.replaceAll((symbol, price) -> {
            var change = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2.0;
            var newLast = price.last().multiply(BigDecimal.ONE.add(BigDecimal.valueOf(change / 100)));
            var newBid = newLast.multiply(BigDecimal.valueOf(0.999));
            var newAsk = newLast.multiply(BigDecimal.valueOf(1.001));
            return new MarketPrice(symbol, newBid, newAsk, newLast, change,
                    price.volume() + ThreadLocalRandom.current().nextInt(1000, 100000),
                    LocalDateTime.now());
        });
    }
}
