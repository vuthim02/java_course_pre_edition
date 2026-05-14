package com.ecommerce.controller;

import com.ecommerce.dto.OrderResponse;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestAttribute Long userId,
                                                      @RequestParam String shippingAddress) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(orderService.createOrder(userId, shippingAddress));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, size));
    }
}
