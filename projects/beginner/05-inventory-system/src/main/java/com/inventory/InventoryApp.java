package com.inventory;

import com.inventory.model.*;
import com.inventory.service.InventoryService;
import java.util.*;

public class InventoryApp {

    private static final InventoryService service = new InventoryService();
    private static final Scanner scanner = new Scanner(System.in);
    private static final String DATA_FILE = "inventory.json";

    public static void main(String[] args) {
        System.out.println("=== Inventory Management System ===");
        try {
            service.loadFromFile(DATA_FILE);
            System.out.println("Loaded " + service.productCount() + " products from " + DATA_FILE);
        } catch (Exception e) {
            System.out.println("No saved data found, starting fresh.");
        }

        System.out.println("""
            Commands:
              add       - Add a new product
              list      - List all products
              search    - Search products by name
              update    - Update a product
              delete    - Delete a product
              low-stock - Show products below stock threshold
              sort      - Sort products (name/price/quantity)
              save      - Save to file
              load      - Load from file
              exit      - Save and exit
            """);

        while (true) {
            System.out.print("> ");
            String cmd = scanner.nextLine().trim().toLowerCase();
            try {
                switch (cmd) {
                    case "add" -> addProduct();
                    case "list" -> listProducts();
                    case "search" -> searchProducts();
                    case "update" -> updateProduct();
                    case "delete" -> deleteProduct();
                    case "low-stock" -> lowStock();
                    case "sort" -> sortProducts();
                    case "save" -> { service.saveToFile(DATA_FILE); System.out.println("Saved."); }
                    case "load" -> { service.loadFromFile(DATA_FILE); System.out.println("Loaded " + service.productCount() + " products."); }
                    case "exit" -> { service.saveToFile(DATA_FILE); System.out.println("Goodbye!"); return; }
                    default -> System.out.println("Unknown command: " + cmd);
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void addProduct() {
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category (ELECTRONICS, FOOD, CLOTHING, BOOKS, OTHER): ");
        Category category = Category.valueOf(scanner.nextLine().trim().toUpperCase());
        System.out.print("Quantity: ");
        int quantity = Integer.parseInt(scanner.nextLine().trim());
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        Product p = service.addProduct(name, category, quantity, price);
        System.out.println("Added product: " + p.id() + " - " + p.name());
    }

    private static void listProducts() {
        List<Product> products = service.listAll();
        if (products.isEmpty()) {
            System.out.println("No products in inventory.");
            return;
        }
        System.out.printf("%-8s | %-20s | %-12s | %-8s | %-8s | %s%n",
            "ID", "Name", "Category", "Qty", "Price", "Added");
        System.out.println("-".repeat(80));
        for (Product p : products) {
            System.out.printf("%-8s | %-20s | %-12s | %-8d | $%-6.2f | %s%n",
                p.id(), truncate(p.name(), 20), p.category(), p.quantity(), p.price(), p.addedDate());
        }
    }

    private static void searchProducts() {
        System.out.print("Search by (1) name or (2) category? ");
        String choice = scanner.nextLine().trim();
        if (choice.equals("1")) {
            System.out.print("Search query: ");
            String query = scanner.nextLine().trim();
            List<Product> results = service.searchByName(query);
            if (results.isEmpty()) {
                System.out.println("No products found.");
            } else {
                results.forEach(p -> System.out.printf("%-8s %-20s $%.2f (%d)%n",
                    p.id(), p.name(), p.price(), p.quantity()));
            }
        } else if (choice.equals("2")) {
            System.out.print("Category (ELECTRONICS, FOOD, CLOTHING, BOOKS, OTHER): ");
            Category cat = Category.valueOf(scanner.nextLine().trim().toUpperCase());
            List<Product> results = service.searchByCategory(cat);
            if (results.isEmpty()) {
                System.out.println("No products in category " + cat);
            } else {
                results.forEach(p -> System.out.printf("%-8s %-20s $%.2f (%d)%n",
                    p.id(), p.name(), p.price(), p.quantity()));
            }
        } else {
            System.out.println("Invalid choice.");
        }
    }

    private static void updateProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        Product existing = service.findProduct(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        System.out.print("Name (" + existing.name() + "): ");
        String name = scanner.nextLine().trim();
        if (name.isEmpty()) name = existing.name();
        System.out.print("Category (" + existing.category() + "): ");
        String catInput = scanner.nextLine().trim();
        Category category = catInput.isEmpty() ? existing.category() : Category.valueOf(catInput.toUpperCase());
        System.out.print("Quantity (" + existing.quantity() + "): ");
        String qtyInput = scanner.nextLine().trim();
        int quantity = qtyInput.isEmpty() ? existing.quantity() : Integer.parseInt(qtyInput);
        System.out.print("Price (" + existing.price() + "): ");
        String priceInput = scanner.nextLine().trim();
        double price = priceInput.isEmpty() ? existing.price() : Double.parseDouble(priceInput);
        service.updateProduct(id, name, category, quantity, price);
        System.out.println("Product updated.");
    }

    private static void deleteProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        Product removed = service.deleteProduct(id)
            .orElseThrow(() -> new NoSuchElementException("Product not found: " + id));
        System.out.println("Deleted: " + removed.name());
    }

    private static void lowStock() {
        System.out.print("Threshold: ");
        int threshold = Integer.parseInt(scanner.nextLine().trim());
        List<Product> low = service.lowStockReport(threshold);
        if (low.isEmpty()) {
            System.out.println("All products are above threshold " + threshold);
        } else {
            System.out.println("Products below " + threshold + ":");
            low.forEach(p -> System.out.printf("%-8s %-20s (qty: %d)%n", p.id(), p.name(), p.quantity()));
        }
    }

    private static void sortProducts() {
        System.out.print("Sort by (name/price/quantity): ");
        String field = scanner.nextLine().trim().toLowerCase();
        List<Product> sorted = service.sortedBy(field);
        for (Product p : sorted) {
            System.out.printf("%-8s %-20s $%.2f (%d)%n", p.id(), p.name(), p.price(), p.quantity());
        }
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
