package com.ecommerce.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private String shippingAddress;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;
}
