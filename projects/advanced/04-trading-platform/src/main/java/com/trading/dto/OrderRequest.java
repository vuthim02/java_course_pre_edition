package com.trading.dto;

import com.trading.model.OrderSide;
import com.trading.model.OrderType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderRequest(
    @NotBlank String symbol,
    @NotNull OrderType orderType,
    @NotNull OrderSide side,
    @Positive BigDecimal quantity,
    BigDecimal price,
    BigDecimal stopPrice,
    @NotNull Long userId
) {}
