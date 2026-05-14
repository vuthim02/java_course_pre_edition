package com.ecommerce.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private int quantity;
    private BigDecimal subtotal;
}
