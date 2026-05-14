package com.platform.order.controller;

import com.platform.order.dto.OrderDTO;
import com.platform.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public OrderDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/user/{userId}")
    public List<OrderDTO> getByUserId(@PathVariable Long userId) {
        return service.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDTO create(@Valid @RequestBody OrderDTO dto) {
        return service.create(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
