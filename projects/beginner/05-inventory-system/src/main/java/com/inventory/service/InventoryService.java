package com.inventory.service;

import com.inventory.model.*;
import com.inventory.repository.InventoryRepository;
import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class InventoryService {

    private final InventoryRepository repository = new InventoryRepository();

    public Product addProduct(String name, Category category, int quantity, double price) {
        String id = generateId();
        Product product = new Product(id, name, category, quantity, price, LocalDate.now());
        repository.add(product);
        return product;
    }

    public Optional<Product> updateProduct(String id, String name, Category category, int quantity, double price) {
        Product updated = new Product(id, name, category, quantity, price, LocalDate.now());
        return repository.update(id, updated);
    }

    public Optional<Product> deleteProduct(String id) {
        return repository.delete(id);
    }

    public Optional<Product> findProduct(String id) {
        return repository.findById(id);
    }

    public List<Product> listAll() {
        return repository.findAll();
    }

    public List<Product> searchByName(String query) {
        return repository.searchByName(query);
    }

    public List<Product> searchByCategory(Category category) {
        return repository.searchByCategory(category);
    }

    public void updateStock(String id, int newQuantity) {
        Product p = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        Product updated = new Product(p.id(), p.name(), p.category(), newQuantity, p.price(), p.addedDate());
        repository.update(id, updated);
    }

    public void applyDiscount(String id, double percent) {
        Product p = repository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        double discounted = Math.round(p.price() * (1 - percent / 100) * 100.0) / 100.0;
        Product updated = new Product(p.id(), p.name(), p.category(), p.quantity(), discounted, p.addedDate());
        repository.update(id, updated);
    }

    public List<Product> lowStockReport(int threshold) {
        return repository.getLowStock(threshold);
    }

    public List<Product> sortedBy(String field) {
        return switch (field.toLowerCase()) {
            case "name" -> repository.sortByName();
            case "price" -> repository.sortByPrice();
            case "quantity" -> repository.sortByQuantity();
            default -> throw new IllegalArgumentException("Unknown sort field: " + field);
        };
    }

    public void importFromCsv(String csvContent) {
        String[] lines = csvContent.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",");
            if (parts.length < 4) {
                System.out.println("Skipping malformed line: " + line);
                continue;
            }
            try {
                String name = parts[0].trim();
                Category cat = Category.valueOf(parts[1].trim().toUpperCase());
                int qty = Integer.parseInt(parts[2].trim());
                double price = Double.parseDouble(parts[3].trim());
                addProduct(name, cat, qty, price);
            } catch (IllegalArgumentException e) {
                System.out.println("Skipping invalid entry: " + line);
            }
        }
    }

    public void saveToFile(String path) throws IOException {
        repository.saveToFile(path);
    }

    public void loadFromFile(String path) throws IOException {
        repository.loadFromFile(path);
    }

    public int productCount() {
        return repository.findAll().size();
    }

    private static String generateId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
