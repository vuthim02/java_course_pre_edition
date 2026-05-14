# Architecture & Design — Lesson 2: Design Patterns (GoF & Enterprise)

## What Are Design Patterns?

Design patterns are **reusable solutions to common problems** in software design. They are not code — they are templates for solving problems.

```
Pattern Structure:
┌─────────────────────────────────────────────────────────────┐
│  Name:        Singleton                                      │
│  Problem:     Need exactly ONE instance of a class           │
│  Solution:    Private constructor + static getInstance()     │
│  Consequence: Global access, but tight coupling             │
└─────────────────────────────────────────────────────────────┘
```

## Creational Patterns — Object Creation

### Singleton

```java
public class DatabaseConnectionPool {
    private static volatile DatabaseConnectionPool instance;
    private final List<Connection> pool = new ArrayList<>();

    private DatabaseConnectionPool() {
        // Initialize connections
    }

    public static DatabaseConnectionPool getInstance() {
        if (instance == null) {
            synchronized (DatabaseConnectionPool.class) {
                if (instance == null) {
                    instance = new DatabaseConnectionPool();
                }
            }
        }
        return instance;
    }
}

// Java 21+ — enum singleton (best)
public enum ConfigManager {
    INSTANCE;

    private final Properties props = new Properties();

    public String get(String key) { return props.getProperty(key); }
}
```

### Factory Method

```java
// Define interface
public interface PaymentGateway {
    PaymentResult charge(PaymentRequest request);
}

// Concrete implementations
public class StripeGateway implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        return StripeApi.charge(request);
    }
}

public class PayPalGateway implements PaymentGateway {
    public PaymentResult charge(PaymentRequest request) {
        return PayPalApi.execute(request);
    }
}

// Factory
public class PaymentGatewayFactory {
    public static PaymentGateway create(String type) {
        return switch (type) {
            case "stripe" -> new StripeGateway();
            case "paypal" -> new PayPalGateway();
            default -> throw new IllegalArgumentException("Unknown: " + type);
        };
    }
}
```

### Builder

```java
public class HttpRequest {
    private final String url;
    private final String method;
    private final Map<String, String> headers;
    private final String body;

    private HttpRequest(Builder builder) {
        this.url = builder.url;
        this.method = builder.method;
        this.headers = Collections.unmodifiableMap(builder.headers);
        this.body = builder.body;
    }

    public static class Builder {
        private String url;
        private String method = "GET";
        private Map<String, String> headers = new HashMap<>();
        private String body;

        public Builder url(String url) { this.url = url; return this; }
        public Builder method(String method) { this.method = method; return this; }
        public Builder header(String key, String value) { headers.put(key, value); return this; }
        public Builder body(String body) { this.body = body; return this; }

        public HttpRequest build() {
            Objects.requireNonNull(url, "URL is required");
            return new HttpRequest(this);
        }
    }
}

// Usage
HttpRequest request = new HttpRequest.Builder()
    .url("https://api.example.com/users")
    .method("POST")
    .header("Authorization", "Bearer token")
    .body("{\"name\": \"Alice\"}")
    .build();
```

## Structural Patterns — Object Composition

### Adapter

```java
// New system interface
public interface PaymentProcessor {
    boolean processPayment(double amount);
}

// Old (legacy) system — incompatible interface
public class LegacyPaymentSystem {
    public String sendPayment(BigDecimal amt) {
        return "SUCCESS";  // Returns string, not boolean
    }
}

// Adapter makes old system work with new interface
public class LegacyPaymentAdapter implements PaymentProcessor {
    private final LegacyPaymentSystem legacy = new LegacyPaymentSystem();

    @Override
    public boolean processPayment(double amount) {
        String result = legacy.sendPayment(BigDecimal.valueOf(amount));
        return "SUCCESS".equals(result);
    }
}
```

### Decorator

```java
// Base interface
public interface Coffee {
    double cost();
    String description();
}

// Concrete component
public class SimpleCoffee implements Coffee {
    public double cost() { return 2.0; }
    public String description() { return "Coffee"; }
}

// Decorator
public abstract class CoffeeDecorator implements Coffee {
    protected final Coffee coffee;
    public CoffeeDecorator(Coffee coffee) { this.coffee = coffee; }
}

// Concrete decorators
public class MilkDecorator extends CoffeeDecorator {
    public MilkDecorator(Coffee coffee) { super(coffee); }
    public double cost() { return coffee.cost() + 0.5; }
    public String description() { return coffee.description() + " + Milk"; }
}

public class SugarDecorator extends CoffeeDecorator {
    public SugarDecorator(Coffee coffee) { super(coffee); }
    public double cost() { return coffee.cost() + 0.25; }
    public String description() { return coffee.description() + " + Sugar"; }
}

// Usage
Coffee coffee = new SugarDecorator(new MilkDecorator(new SimpleCoffee()));
System.out.println(coffee.description() + " = $" + coffee.cost());
// "Coffee + Milk + Sugar = $2.75"
```

## Behavioral Patterns — Object Interaction

### Observer

```java
// Event interface
public interface OrderEventListener {
    void onOrderCreated(Order order);
}

// Publisher
public class OrderEventPublisher {
    private final List<OrderEventListener> listeners = new ArrayList<>();

    public void subscribe(OrderEventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(OrderEventListener listener) {
        listeners.remove(listener);
    }

    public void publishOrderCreated(Order order) {
        for (OrderEventListener listener : listeners) {
            listener.onOrderCreated(order);
        }
    }
}

// Listeners
public class EmailListener implements OrderEventListener {
    public void onOrderCreated(Order order) {
        System.out.println("Sending email for order: " + order.getId());
    }
}

public class InventoryListener implements OrderEventListener {
    public void onOrderCreated(Order order) {
        System.out.println("Reserving inventory for order: " + order.getId());
    }
}

// Usage
OrderEventPublisher publisher = new OrderEventPublisher();
publisher.subscribe(new EmailListener());
publisher.subscribe(new InventoryListener());
publisher.publishOrderCreated(order);
```

### Strategy

```java
// Strategy interface
public interface ShippingStrategy {
    double calculateCost(double weight, String destination);
}

// Concrete strategies
public class StandardShipping implements ShippingStrategy {
    public double calculateCost(double weight, String destination) {
        return weight * 1.5 + 5.0;
    }
}

public class ExpressShipping implements ShippingStrategy {
    public double calculateCost(double weight, String destination) {
        return weight * 3.0 + 10.0;
    }
}

public class InternationalShipping implements ShippingStrategy {
    public double calculateCost(double weight, String destination) {
        return weight * 5.0 + 20.0;
    }
}

// Context
public class ShippingCalculator {
    private ShippingStrategy strategy;

    public void setStrategy(ShippingStrategy strategy) {
        this.strategy = strategy;
    }

    public double calculate(double weight, String destination) {
        return strategy.calculateCost(weight, destination);
    }
}

// Usage
ShippingCalculator calc = new ShippingCalculator();
calc.setStrategy(new ExpressShipping());
double cost = calc.calculate(2.5, "New York");
```

## Enterprise Patterns

### Repository

```java
public interface UserRepository {
    Optional<User> findById(Long id);
    List<User> findAll();
    User save(User user);
    void deleteById(Long id);
}

public class JdbcUserRepository implements UserRepository {
    private final JdbcTemplate jdbc;

    public Optional<User> findById(Long id) {
        return jdbc.queryForObject(
            "SELECT * FROM users WHERE id = ?",
            new BeanPropertyRowMapper<>(User.class), id
        );
    }
    // ...
}
```

### Unit of Work

```java
@Service
public class OrderService {
    @Autowired private UserRepository userRepo;
    @Autowired private OrderRepository orderRepo;
    @Autowired private InventoryRepository inventoryRepo;

    @Transactional  // Unit of Work — all or nothing
    public Order createOrder(OrderRequest request) {
        User user = userRepo.findById(request.getUserId())
            .orElseThrow(() -> new UserNotFoundException());
        Order order = orderRepo.save(new Order(user, request.getItems()));
        inventoryRepo.deductStock(request.getItems());
        return order;
    }
}
```

### DTO (Data Transfer Object)

```java
// Entity (internal)
@Entity
public class User {
    @Id private Long id;
    private String name;
    private String email;
    private String passwordHash;   // Should NOT expose
    private LocalDateTime createdAt;
}

// DTO (external API)
public record UserResponse(
    Long id,
    String name,
    String email,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getCreatedAt()
        );
    }
}
```

## Pattern Selection Guide

| Problem | Pattern | When to Use |
|---------|---------|-------------|
| Need one instance | Singleton | Connection pools, config, caches |
| Object creation varies | Factory | Different implementations of same interface |
| Complex object construction | Builder | Objects with many optional parameters |
| Incompatible interfaces | Adapter | Integrating legacy code |
| Add behavior dynamically | Decorator | Extending functionality without inheritance |
| One-to-many notification | Observer | Event handling, pub/sub |
| Interchangeable algorithms | Strategy | Different algorithms for same task |
| Abstract data access | Repository | Decoupling business logic from data layer |

## Exercises

1. Implement a Singleton for a configuration manager.
2. Create a Builder for a complex object (e.g., an Email with to, cc, bcc, subject, body, attachments).
3. Use the Strategy pattern to implement different tax calculation methods.
4. Implement the Observer pattern for a notification system.
5. Identify which pattern would solve a current problem in your codebase.
