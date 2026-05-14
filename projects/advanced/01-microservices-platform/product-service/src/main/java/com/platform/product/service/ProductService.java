package com.platform.product.service;

import com.platform.product.dto.ProductDTO;
import com.platform.product.model.Product;
import com.platform.product.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<ProductDTO> findAll() {
        return repository.findAll().stream().map(this::toDto).toList();
    }

    public ProductDTO findById(Long id) {
        return repository.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }

    public ProductDTO create(ProductDTO dto) {
        var product = new Product();
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        return toDto(repository.save(product));
    }

    public ProductDTO update(Long id, ProductDTO dto) {
        var product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        product.setName(dto.name());
        product.setDescription(dto.description());
        product.setPrice(dto.price());
        return toDto(repository.save(product));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Product not found: " + id);
        }
        repository.deleteById(id);
    }

    private ProductDTO toDto(Product product) {
        return new ProductDTO(product.getId(), product.getName(), product.getDescription(),
                product.getPrice(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
