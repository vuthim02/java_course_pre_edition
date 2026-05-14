package com.trading.dto;

import com.trading.model.OrderStatus;
import com.trading.model.OrderType;
import com.trading.model.OrderSide;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderResponse(
    Long id,
    String symbol,
    OrderType orderType,
    OrderSide side,
    BigDecimal quantity,
    BigDecimal price,
    BigDecimal stopPrice,
    BigDecimal filledQuantity,
    OrderStatus status,
    Long userId,
    LocalDateTime createdAt
) {}
