package com.platform.order.service;

import com.platform.order.dto.OrderDTO;
import com.platform.order.model.Order;
import com.platform.order.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public List<OrderDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public OrderDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Order not found: " + id));
    }

    public List<OrderDTO> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream().map(this::toDto).toList();
    }

    public OrderDTO create(OrderDTO dto) {
        var order = new Order();
        order.setUserId(dto.userId());
        order.setProductName(dto.productName());
        order.setQuantity(dto.quantity());
        order.setPrice(dto.price());
        return toDto(repository.save(order));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Order not found: " + id);
        }
        repository.deleteById(id);
    }

    private OrderDTO toDto(Order order) {
        return new OrderDTO(order.getId(), order.getUserId(), order.getProductName(),
                order.getQuantity(), order.getPrice(), order.getCreatedAt());
    }
}
