package com.ecommerce.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
}
