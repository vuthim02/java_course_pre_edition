# Architecture & Design — Lesson 3: Refactoring & Code Smells

## What is Refactoring?

**Refactoring** is changing the internal structure of code without changing its external behavior. The code does the same thing — it's just cleaner, safer, and easier to modify.

```
             ┌─────────────────────┐
             │    WRITE TESTS      │
             │ (safety net first)  │
             └──────────┬──────────┘
                        │
             ┌──────────▼──────────┐
             │  IDENTIFY SMELLS    │
             │ (what's wrong?)     │
             └──────────┬──────────┘
                        │
             ┌──────────▼──────────┐
             │   APPLY REFACTOR    │
             │ (one change at a    │
             │  time, then test)   │
             └──────────┬──────────┘
                        │
             ┌──────────▼──────────┐
             │    RUN TESTS        │
             │ (still passing?)    │
             └──────────┬──────────┘
                        │
              YES ──────┴────── NO
               │                │
               ▼                ▼
           Continue         Revert
```

## Code Smells — Signs You Need to Refactor

### 1. Long Method

```java
// BEFORE — Too long, does too much
public void processOrder(Order order) {
    // Validate
    if (order.getItems().isEmpty()) throw new IllegalArgumentException();
    if (order.getUser() == null) throw new IllegalArgumentException();
    if (order.getTotal() < 0) throw new IllegalArgumentException();

    // Calculate discounts
    double discount = 0;
    if (order.getTotal() > 100) discount = 0.1;
    if (order.getUser().isVIP()) discount = Math.max(discount, 0.2);

    // Apply
    double finalTotal = order.getTotal() * (1 - discount);

    // Save
    order.setFinalTotal(finalTotal);
    orderRepository.save(order);

    // Notify
    emailService.sendConfirmation(order);
    analyticsService.trackOrder(order);
}

// AFTER — Extract methods
public void processOrder(Order order) {
    validateOrder(order);
    double finalTotal = calculateFinalTotal(order);
    order.setFinalTotal(finalTotal);
    orderRepository.save(order);
    notifyServices(order);
}

private void validateOrder(Order order) { /* ... */ }
private double calculateFinalTotal(Order order) { /* ... */ }
private void notifyServices(Order order) { /* ... */ }
```

### 2. Large Class

```java
// BEFORE — God class
public class OrderService {
    public void createOrder() { /* ... */ }
    public void validateOrder() { /* ... */ }
    public void calculateTax() { /* ... */ }
    public void applyDiscount() { /* ... */ }
    public void sendEmail() { /* ... */ }
    public void generateInvoice() { /* ... */ }
    public void updateInventory() { /* ... */ }
    public void processPayment() { /* ... */ }
    public void refundOrder() { /* ... */ }
    // 20+ more methods...
}

// AFTER — Each class has ONE responsibility
public class OrderService {
    private final TaxCalculator taxCalculator;
    private final DiscountCalculator discountCalculator;
    private final InvoiceGenerator invoiceGenerator;
    private final EmailService emailService;
    // ...
}
```

### 3. Primitive Obsession

```java
// BEFORE — Primitives everywhere
public void createUser(String name, String email, String phone, String street,
                        String city, String zip, String creditCard, String expiry) {
    // 8 primitive parameters — impossible to read!
}

// AFTER — Use value objects
public record Email(String value) {
    public Email {
        if (!value.contains("@")) throw new IllegalArgumentException("Invalid email");
    }
}

public record Phone(String value) {
    public Phone {
        if (!value.matches("\\+?[0-9]{10,15}")) throw new IllegalArgumentException();
    }
}

public record Address(String street, String city, String zip) {}

public record CreditCard(String number, String expiry) {}

public void createUser(String name, Email email, Phone phone, Address address,
                        CreditCard creditCard) {
    // 4 meaningful parameters — clear!
}
```

### 4. Switch Statements (Duplicated)

```java
// BEFORE — Switch scattered everywhere
public double calculateShipping(String type, double weight) {
    return switch (type) {
        case "standard" -> weight * 1.5;
        case "express" -> weight * 3.0;
        case "overnight" -> weight * 5.0;
        default -> throw new IllegalArgumentException();
    };
}

public String getEstimatedDelivery(String type) {
    return switch (type) {
        case "standard" -> "5-7 days";
        case "express" -> "2-3 days";
        case "overnight" -> "1 day";
        default -> throw new IllegalArgumentException();
    };
}

// AFTER — Polymorphism
public interface ShippingStrategy {
    double calculateCost(double weight);
    String getEstimatedDelivery();
}

public class StandardShipping implements ShippingStrategy {
    public double calculateCost(double weight) { return weight * 1.5; }
    public String getEstimatedDelivery() { return "5-7 days"; }
}
```

### 5. Feature Envy

```java
// BEFORE — Method more interested in another class
public class OrderService {
    public boolean isOrderComplete(Order order) {
        return order.getStatus() == OrderStatus.PAID
            && order.getPayment().getTransactionId() != null
            && order.getShipment().getTrackingNumber() != null
            && order.getInvoice().isGenerated();
    }
}

// AFTER — Let the object do its own work
public class Order {
    public boolean isComplete() {
        return status == OrderStatus.PAID
            && payment.hasTransactionId()
            && shipment.hasTrackingNumber()
            && invoice.isGenerated();
    }
}
```

### 6. Comments as Deodorant

```java
// BEFORE — Comments explain BAD code
public class Calculator {
    // This method adds two numbers and returns the result
    // a is the first number
    // b is the second number
    // The result is a + b
    public int doStuff(int a, int b) {
        // Add a and b
        int c = a + b;
        // Return the result
        return c;
    }
}

// AFTER — Clean code doesn't need comments
public class Calculator {
    public int add(int first, int second) {
        return first + second;
    }
}
```

## Refactoring Techniques

### Extract Method

```java
// BEFORE
public void printCustomerSummary(Customer customer) {
    System.out.println("=== CUSTOMER SUMMARY ===");
    System.out.println("Name: " + customer.getName());
    System.out.println("Email: " + customer.getEmail());
    System.out.println("Orders: " + customer.getOrders().size());
    if (customer.getOrders().size() > 0) {
        double total = customer.getOrders().stream()
            .mapToDouble(Order::getTotal).sum();
        System.out.println("Total spent: $" + total);
    }
    System.out.println("=========================");
}

// AFTER
public void printCustomerSummary(Customer customer) {
    printHeader();
    printCustomerInfo(customer);
    printOrderSummary(customer);
    printFooter();
}
```

### Replace Conditional with Polymorphism

```java
// BEFORE
public double calculateBonus(Employee employee) {
    return switch (employee.getType()) {
        case "DEVELOPER" -> employee.getSalary() * 0.1;
        case "MANAGER" -> employee.getSalary() * 0.2;
        case "EXECUTIVE" -> employee.getSalary() * 0.3;
        default -> 0;
    };
}

// AFTER
public abstract class Employee {
    public abstract double calculateBonus();
}

public class Developer extends Employee {
    public double calculateBonus() { return getSalary() * 0.1; }
}

public class Manager extends Employee {
    public double calculateBonus() { return getSalary() * 0.2; }
}
```

### Introduce Parameter Object

```java
// BEFORE
public List<Order> searchOrders(Long userId, String status,
                                 LocalDate from, LocalDate to,
                                 BigDecimal minTotal, BigDecimal maxTotal,
                                 String sortBy, boolean ascending) {
    // 8 parameters!
}

// AFTER
public record OrderSearchCriteria(
    Long userId, String status,
    LocalDate from, LocalDate to,
    BigDecimal minTotal, BigDecimal maxTotal,
    String sortBy, boolean ascending
) {}

public List<Order> searchOrders(OrderSearchCriteria criteria) {
    // 1 parameter — clear, extensible
}
```

### Decompose Conditional

```java
// BEFORE
if (order.getTotal() > 500 && !order.isHoliday()
    && order.getUser().isMember() || order.getUser().isVIP()) {
    // Apply special discount
}

// AFTER
if (shouldApplySpecialDiscount(order)) {
    // Apply special discount
}

private boolean shouldApplySpecialDiscount(Order order) {
    return (order.getTotal() > 500
        && !order.isHoliday()
        && order.getUser().isMember())
        || order.getUser().isVIP();
}
```

## Refactoring Workflow

```
1. MAKE IT WORK     — Get the feature working (even if ugly)
2. MAKE IT RIGHT    — Refactor to clean code
3. MAKE IT FAST     — Optimize if needed

⚠️ NEVER refactor without tests! Tests are your safety net.
```

## Exercises

1. Find a long method (>20 lines) in your code. Extract 2+ smaller methods.
2. Identify a "primitive obsession" — create a value object to replace multiple primitives.
3. Replace a switch statement with polymorphism.
4. Find and fix a "feature envy" violation.
5. Run a code analysis tool (SonarLint, IntelliJ inspections) and fix 5 warnings.
