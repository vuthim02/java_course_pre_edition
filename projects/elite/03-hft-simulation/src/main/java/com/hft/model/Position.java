package com.hft.model;

public record Position(
    int symbolId,
    long quantity,
    long avgEntryPrice,
    long currentPrice,
    long unrealizedPnl,
    long realizedPnl
) {}
