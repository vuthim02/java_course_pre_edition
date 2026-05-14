package com.trading.service;

import com.trading.dto.OrderRequest;
import com.trading.dto.OrderResponse;
import com.trading.model.*;
import com.trading.repository.TradeOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class OrderService {
    private final TradeOrderRepository repository;
    private final PortfolioService portfolioService;

    public OrderService(TradeOrderRepository repository, PortfolioService portfolioService) {
        this.repository = repository;
        this.portfolioService = portfolioService;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        var order = new TradeOrder();
        order.setSymbol(request.symbol().toUpperCase());
        order.setOrderType(request.orderType());
        order.setSide(request.side());
        order.setQuantity(request.quantity());
        order.setUserId(request.userId());
        order.setStatus(OrderStatus.PENDING);

        switch (request.orderType()) {
            case MARKET -> {
                order.setPrice(BigDecimal.ZERO);
                fillOrder(order);
            }
            case LIMIT -> {
                if (request.price() == null)
                    throw new IllegalArgumentException("Limit price required for LIMIT orders");
                order.setPrice(request.price());
            }
            case STOP -> {
                if (request.stopPrice() == null)
                    throw new IllegalArgumentException("Stop price required for STOP orders");
                order.setStopPrice(request.stopPrice());
                order.setPrice(request.price());
            }
        }

        return toResponse(repository.save(order));
    }

    public List<OrderResponse> getUserOrders(Long userId) {
        return repository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    public OrderResponse getOrder(Long id) {
        return repository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        var order = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
        if (order.getStatus() == OrderStatus.FILLED) {
            throw new RuntimeException("Cannot cancel filled order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        return toResponse(repository.save(order));
    }

    private void fillOrder(TradeOrder order) {
        order.setFilledQuantity(order.getQuantity());
        order.setStatus(OrderStatus.FILLED);
        portfolioService.updatePortfolio(order.getUserId(), order.getSymbol(),
                order.getSide(), order.getQuantity(), order.getPrice());
    }

    private OrderResponse toResponse(TradeOrder order) {
        return new OrderResponse(
            order.getId(), order.getSymbol(), order.getOrderType(),
            order.getSide(), order.getQuantity(), order.getPrice(),
            order.getStopPrice(), order.getFilledQuantity(),
            order.getStatus(), order.getUserId(), order.getCreatedAt());
    }
}
