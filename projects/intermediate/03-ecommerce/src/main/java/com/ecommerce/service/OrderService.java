package com.ecommerce.service;

import com.ecommerce.dto.*;
import com.ecommerce.model.*;
import com.ecommerce.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public OrderResponse createOrder(Long userId, String shippingAddress) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal total = cart.getItems().stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .user(cart.getUser())
                .status("PENDING")
                .totalAmount(total)
                .shippingAddress(shippingAddress)
                .build();
        order = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getProduct().getPrice())
                    .build();
            order.getItems().add(orderItem);
        }

        orderRepository.save(order);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);

        return toResponse(order);
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return toResponse(order);
    }

    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        return orderRepository.findByUserId(userId, PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    public Page<OrderResponse> getAllOrders(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .map(this::toResponse);
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        order = orderRepository.save(order);
        return toResponse(order);
    }

    private OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .createdAt(order.getCreatedAt())
                .items(order.getItems().stream().map(item -> OrderItemResponse.builder()
                        .id(item.getId())
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .price(item.getPrice())
                        .build()).toList())
                .build();
    }
}
