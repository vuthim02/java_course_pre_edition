# Inventory System — Complete Java Source Code

---

## `Product.java`

```java
import java.util.Objects;

/**
 * Represents a single product in the inventory.
 * Immutable-style design: fields can be updated via setters with validation.
 */
public class Product {
    private String id;
    private String name;
    private String category;
    private double price;
    private int quantity;

    public Product(String id, String name, String category, double price, int quantity) {
        this.id       = Objects.requireNonNull(id, "id must not be null");
        this.name     = Objects.requireNonNull(name, "name must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        setPrice(price);
        setQuantity(quantity);
    }

    // --- Getters ---
    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getCategory() { return category; }
    public double getPrice()    { return price; }
    public int getQuantity()    { return quantity; }

    // --- Setters (with validation) ---
    public void setName(String name)           { this.name = Objects.requireNonNull(name); }
    public void setCategory(String category)   { this.category = Objects.requireNonNull(category); }
    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price cannot be negative");
        this.price = price;
    }
    public void setQuantity(int quantity) {
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        this.quantity = quantity;
    }

    /** Returns true if the current quantity is below the given threshold */
    public boolean isLowStock(int threshold) {
        return quantity < threshold;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | %s | $%.2f | Qty: %d",
                id, name, category, price, quantity);
    }

    // --- File persistence helpers ---

    /** Serializes this product to a pipe-delimited string */
    public String toFileString() {
        return String.join("|", id, name, category,
                String.valueOf(price), String.valueOf(quantity));
    }

    /** Deserializes a product from a pipe-delimited string */
    public static Product fromFileString(String line) {
        String[] parts = line.split("\\|");
        return new Product(
                parts[0], parts[1], parts[2],
                Double.parseDouble(parts[3]),
                Integer.parseInt(parts[4]));
    }
}
```

---

## `InventorySystem.java`

```java
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Console-based inventory management system.
 * Provides a menu-driven interface for CRUD operations, sorting,
 * low-stock alerts, reporting, and file persistence.
 */
public class InventorySystem {

    private static final Path DATA_FILE = Path.of("inventory.txt");
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Product> products = new ArrayList<>();
    private static int lowStockThreshold = 5;

    public static void main(String[] args) {
        // Load existing data from file
        loadInventory();
        System.out.println("========== INVENTORY SYSTEM ==========");

        while (true) {
            showMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1" -> addProduct();
                    case "2" -> updateProduct();
                    case "3" -> deleteProduct();
                    case "4" -> searchProduct();
                    case "5" -> listProducts();
                    case "6" -> setLowStockThreshold();
                    case "7" -> lowStockAlerts();
                    case "8" -> sortProducts();
                    case "9" -> generateReport();
                    case "0" -> {
                        saveInventory();
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;
                    }
                    default -> System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // ---------------------------------------------------------------
    //  Menu
    // ---------------------------------------------------------------
    private static void showMenu() {
        System.out.println("\n--- Menu ---");
        System.out.println("1.  Add product");
        System.out.println("2.  Update product");
        System.out.println("3.  Delete product");
        System.out.println("4.  Search product");
        System.out.println("5.  List all products");
        System.out.println("6.  Set low-stock threshold (current: " + lowStockThreshold + ")");
        System.out.println("7.  Low-stock alerts");
        System.out.println("8.  Sort products");
        System.out.println("9.  Generate inventory report");
        System.out.println("0.  Exit");
        System.out.print("Choose: ");
    }

    // ---------------------------------------------------------------
    //  CRUD operations
    // ---------------------------------------------------------------
    private static void addProduct() {
        System.out.print("Product ID: ");
        String id = scanner.nextLine().trim();
        if (findProductById(id) != null) {
            System.out.println("Product ID already exists.");
            return;
        }
        System.out.print("Name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Category: ");
        String category = scanner.nextLine().trim();
        System.out.print("Price: ");
        double price = Double.parseDouble(scanner.nextLine().trim());
        System.out.print("Quantity: ");
        int qty = Integer.parseInt(scanner.nextLine().trim());

        products.add(new Product(id, name, category, price, qty));
        System.out.println("Product added.");
    }

    private static void updateProduct() {
        System.out.print("Product ID to update: ");
        String id = scanner.nextLine().trim();
        Product p = findProductById(id);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }

        // Show current value and allow blank to keep it
        System.out.print("New name (" + p.getName() + "): ");
        String name = scanner.nextLine().trim();
        if (!name.isEmpty()) p.setName(name);

        System.out.print("New category (" + p.getCategory() + "): ");
        String cat = scanner.nextLine().trim();
        if (!cat.isEmpty()) p.setCategory(cat);

        System.out.print("New price (" + p.getPrice() + "): ");
        String priceStr = scanner.nextLine().trim();
        if (!priceStr.isEmpty()) p.setPrice(Double.parseDouble(priceStr));

        System.out.print("New quantity (" + p.getQuantity() + "): ");
        String qtyStr = scanner.nextLine().trim();
        if (!qtyStr.isEmpty()) p.setQuantity(Integer.parseInt(qtyStr));

        System.out.println("Product updated.");
    }

    private static void deleteProduct() {
        System.out.print("Product ID to delete: ");
        String id = scanner.nextLine().trim();
        Product p = findProductById(id);
        if (p == null) {
            System.out.println("Product not found.");
            return;
        }
        products.remove(p);
        System.out.println("Product deleted.");
    }

    // ---------------------------------------------------------------
    //  Search
    // ---------------------------------------------------------------
    private static void searchProduct() {
        System.out.print("Search by name or category: ");
        String query = scanner.nextLine().trim().toLowerCase();
        List<Product> results = products.stream()
                .filter(p -> p.getName().toLowerCase().contains(query)
                          || p.getCategory().toLowerCase().contains(query))
                .collect(Collectors.toList());
        if (results.isEmpty()) {
            System.out.println("No matching products.");
        } else {
            results.forEach(System.out::println);
        }
    }

    // ---------------------------------------------------------------
    //  List all
    // ---------------------------------------------------------------
    private static void listProducts() {
        if (products.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }
        System.out.println("--- All Products (" + products.size() + ") ---");
        products.forEach(System.out::println);
    }

    // ---------------------------------------------------------------
    //  Low-stock threshold
    // ---------------------------------------------------------------
    private static void setLowStockThreshold() {
        System.out.print("New low-stock threshold: ");
        lowStockThreshold = Integer.parseInt(scanner.nextLine().trim());
        System.out.println("Threshold set to " + lowStockThreshold);
    }

    private static void lowStockAlerts() {
        List<Product> low = products.stream()
                .filter(p -> p.isLowStock(lowStockThreshold))
                .collect(Collectors.toList());
        if (low.isEmpty()) {
            System.out.println("All products are sufficiently stocked.");
            return;
        }
        System.out.println("--- Low-Stock Alerts (threshold: " + lowStockThreshold + ") ---");
        low.forEach(p -> System.out.printf("  %s — only %d left!%n", p, p.getQuantity()));
    }

    // ---------------------------------------------------------------
    //  Sorting
    // ---------------------------------------------------------------
    private static void sortProducts() {
        System.out.println("Sort by: 1. Name  2. Price  3. Quantity");
        String choice = scanner.nextLine().trim();
        Comparator<Product> comparator = switch (choice) {
            case "1" -> Comparator.comparing(Product::getName);
            case "2" -> Comparator.comparingDouble(Product::getPrice);
            case "3" -> Comparator.comparingInt(Product::getQuantity);
            default  -> null;
        };
        if (comparator == null) {
            System.out.println("Invalid option.");
            return;
        }
        products.sort(comparator);
        System.out.println("Products sorted.");
        products.forEach(System.out::println);
    }

    // ---------------------------------------------------------------
    //  Report
    // ---------------------------------------------------------------
    private static void generateReport() {
        System.out.println("========== INVENTORY REPORT ==========");
        System.out.printf("Total products: %d%n", products.size());

        double totalValue = products.stream()
                .mapToDouble(p -> p.getPrice() * p.getQuantity())
                .sum();
        System.out.printf("Total inventory value: $%.2f%n", totalValue);

        System.out.printf("Low-stock threshold: %d%n", lowStockThreshold);
        long lowCount = products.stream()
                .filter(p -> p.isLowStock(lowStockThreshold))
                .count();
        System.out.printf("Low-stock items: %d%n", lowCount);

        // Category breakdown
        System.out.println("\nProducts by category:");
        products.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()))
                .forEach((cat, count) -> System.out.printf("  %s: %d%n", cat, count));

        System.out.println("======================================");
    }

    // ---------------------------------------------------------------
    //  Helpers
    // ---------------------------------------------------------------
    private static Product findProductById(String id) {
        return products.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    // ---------------------------------------------------------------
    //  File persistence
    // ---------------------------------------------------------------
    private static void loadInventory() {
        if (!Files.exists(DATA_FILE)) return;
        try (BufferedReader reader = Files.newBufferedReader(DATA_FILE)) {
            reader.lines()
                    .filter(l -> !l.isBlank())
                    .map(Product::fromFileString)
                    .forEach(products::add);
            System.out.println("Loaded " + products.size() + " product(s) from " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error loading inventory: " + e.getMessage());
        }
    }

    private static void saveInventory() {
        try (BufferedWriter writer = Files.newBufferedWriter(DATA_FILE)) {
            for (Product p : products) {
                writer.write(p.toFileString());
                writer.newLine();
            }
            System.out.println("Saved " + products.size() + " product(s) to " + DATA_FILE);
        } catch (IOException e) {
            System.err.println("Error saving inventory: " + e.getMessage());
        }
    }
}
```
