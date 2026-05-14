package com.trading.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketPrice(
    String symbol,
    BigDecimal bid,
    BigDecimal ask,
    BigDecimal last,
    double changePercent,
    long volume,
    LocalDateTime timestamp
) {}
