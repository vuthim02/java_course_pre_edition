package com.trading.dto;

import java.math.BigDecimal;

public record PortfolioResponse(
    Long id,
    Long userId,
    String symbol,
    BigDecimal quantity,
    BigDecimal avgCost,
    BigDecimal currentPrice,
    BigDecimal totalValue,
    BigDecimal profitLoss
) {}
