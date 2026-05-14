package com.platform.product.controller;

import com.platform.product.dto.ProductDTO;
import com.platform.product.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @GetMapping
    public List<ProductDTO> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ProductDTO getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO create(@Valid @RequestBody ProductDTO dto) {
        return service.create(dto);
    }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        return service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
