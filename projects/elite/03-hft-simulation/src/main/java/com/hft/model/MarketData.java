package com.hft.model;

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
