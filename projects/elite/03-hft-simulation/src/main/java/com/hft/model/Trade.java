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
