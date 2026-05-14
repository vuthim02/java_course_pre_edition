# Architecture & Design — Lesson 4: Domain-Driven Design (DDD)

## What is Domain-Driven Design?

DDD is an approach to software development where the **complex business domain** is the primary focus. Instead of starting with database tables or API endpoints, you start by deeply understanding the business.

```
Traditional Approach:                      DDD Approach:
START with database tables                 START with business domain
┌────────────────────┐                     ┌────────────────────┐
│ users table        │                     │ Bounded Context:   │
│ orders table       │                     │ Ordering           │
│ products table     │                     │                    │
│ CREATE TABLE ...   │                     │ Ubiquitous         │
└────────────────────┘                     │ Language:          │
                                            │ Order, Cart,       │
  END with business logic                   │ Invoice, Shipment  │
  (spread everywhere)                       └────────────────────┘
                                                  │
                                             Model the DOMAIN,
                                             then design DB/API
```

## Core Concepts

### Ubiquitous Language

The same language used by **business experts** and **developers** — in code, in meetings, in documentation.

```
Business says:                          Code says:
"An order can be cancelled"             order.cancel()
"Cancel an unpaid order"                CancelOrderService
"Order was paid successfully"           OrderStatus.PAID
"Refund the customer"                   payment.refund()

NEVER: order.kill(), order.abortTrans(), payment.returnMoney()
```

### Bounded Context

A **bounded context** is a logical boundary where a domain model applies. Different contexts may use the same word to mean different things.

```
┌─────────────────────────────────────────────────────────────┐
│                   E-COMMERCE DOMAIN                           │
│                                                               │
│  ┌──────────────────────┐  ┌──────────────────────┐          │
│  │  SALES CONTEXT       │  │  SHIPPING CONTEXT    │          │
│  │                      │  │                      │          │
│  │  Product = item being │  │  Product = physical  │          │
│  │  sold (has price,     │  │  package (has weight,│          │
│  │  description, image)  │  │  dimensions, hazmat) │          │
│  │                      │  │                      │          │
│  │  Customer = buyer     │  │  Customer = delivery │          │
│  │  (has email, prefs)   │  │  recipient (has addr)│          │
│  └──────────────────────┘  └──────────────────────┘          │
│                                                               │
│  ┌──────────────────────┐  ┌──────────────────────┐          │
│  │  INVENTORY CONTEXT   │  │  BILLING CONTEXT     │          │
│  │                      │  │                      │          │
│  │  Product = stock item │  │  Product = SKU for  │          │
│  │  (has quantity,       │  │  invoice line item  │          │
│  │  warehouse location)  │  │  (has tax category) │          │
│  └──────────────────────┘  └──────────────────────┘          │
└─────────────────────────────────────────────────────────────┘
```

## Building Blocks

### Entity

An object with a **unique identity** that persists over time. Two entities with the same attributes but different IDs are different.

```java
public class Order {
    private final OrderId id;         // Identity — stays same
    private OrderStatus status;
    private Money total;
    private List<OrderLine> lines;

    public Order(OrderId id, CustomerId customerId) {
        this.id = id;
        this.customerId = customerId;
        this.status = OrderStatus.PENDING;
        this.lines = new ArrayList<>();
    }

    public void addProduct(ProductId productId, int quantity, Money price) {
        // Business rule: validate stock
        lines.add(new OrderLine(productId, quantity, price));
        recalculateTotal();
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // Equality by identity, not fields
    @Override
    public boolean equals(Object o) {
        return o instanceof Order other && this.id.equals(other.id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }
}
```

### Value Object

An immutable object defined by its **attributes**. Two value objects with the same attributes are interchangeable.

```java
// Value Object — immutable, no identity
public record Money(BigDecimal amount, Currency currency) {

    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money multiply(int factor) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), currency);
    }
}

// Another Value Object
public record Address(String street, String city, String zipCode, String country) {
    public boolean isDomestic() {
        return "US".equals(country);
    }
}
```

### Aggregate

A cluster of domain objects treated as a single unit. The **Aggregate Root** is the only entry point — external objects can only reference the root.

```
┌───────────────────────────────────────────────┐
│              ORDER (Aggregate Root)            │
│                                                │
│  OrderId id                                   │
│  OrderStatus status                           │
│  Money total                                  │
│  CustomerId customerId                        │
│                                                │
│  ┌─────────────────────────────────────────┐  │
│  │ OrderLine (Entity, inside aggregate)    │  │
│  │ OrderLine (Entity)                      │  │
│  │ OrderLine (Entity)                      │  │
│  │ ...                                     │  │
│  └─────────────────────────────────────────┘  │
│                                                │
│  Rules enforced by aggregate root:             │
│  • addProduct() → validate stock               │
│  • cancel() → check status                     │
│  • pay() → update status                       │
└───────────────────────────────────────────────┘
```

```java
public class Order {
    private OrderId id;
    private List<OrderLine> lines;

    public void addProduct(ProductId productId, int quantity, Money price) {
        // Business rule: max 10 items per order
        if (lines.size() >= 10) {
            throw new BusinessException("Order limit reached");
        }
        lines.add(new OrderLine(productId, quantity, price));
        recalculateTotal();
    }
}
```

### Domain Service

A stateless service that holds **domain logic** that doesn't fit naturally into an Entity or Value Object.

```java
// Domain Service — business logic that involves multiple aggregates
public class PricingService {
    public Money calculateFinalPrice(Order order, Customer customer) {
        Money baseTotal = order.getTotal();

        // Apply customer tier discount
        Money afterDiscount = applyLoyaltyDiscount(baseTotal, customer.getTier());

        // Apply coupon if valid
        Money afterCoupon = applyCoupon(afterDiscount, order.getCouponCode());

        // Calculate tax
        Money withTax = applyTax(afterCoupon, customer.getAddress());

        return withTax;
    }

    private Money applyLoyaltyDiscount(Money total, CustomerTier tier) {
        return switch (tier) {
            case GOLD -> total.multiply(90).divide(100);  // 10% off
            case SILVER -> total.multiply(95).divide(100); // 5% off
            case BRONZE -> total;
        };
    }
}
```

### Domain Event

Something that happened in the domain that other parts of the system should know about.

```java
public record OrderPlacedEvent(
    OrderId orderId,
    CustomerId customerId,
    Money total,
    Instant occurredAt
) {
    public OrderPlacedEvent(Order order) {
        this(order.getId(), order.getCustomerId(), order.getTotal(), Instant.now());
    }
}

// Raising domain events
public class Order {
    private final List<DomainEvent> events = new ArrayList<>();

    public void place() {
        this.status = OrderStatus.PLACED;
        events.add(new OrderPlacedEvent(this));
    }

    public List<DomainEvent> getEvents() {
        return List.copyOf(events);
    }
}

// Event handler
@Service
public class OrderPlacedHandler {
    @EventListener
    public void on(OrderPlacedEvent event) {
        emailService.sendOrderConfirmation(event);
        inventoryService.reserveStock(event);
        analyticsService.track(event);
    }
}
```

## Repository (DDD Style)

```java
// Repository — collection-like interface for aggregates
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
    void delete(OrderId id);
}

// Infrastructure implementation
@Repository
public class JpaOrderRepository implements OrderRepository {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(entityManager.find(Order.class, id));
    }

    @Override
    public void save(Order order) {
        entityManager.persist(order);
    }
}
```

## Strategic Design

```
                  ┌──────────────────────┐
                  │     PAYMENTS         │
                  │  (Core Domain)       │
                  └──────────┬───────────┘
                             │
  ┌──────────────┐          │          ┌──────────────┐
  │  CUSTOMER    │◀─────────┼─────────▶│   ORDERING   │
  │ (Supporting) │          │          │ (Core Domain)│
  └──────────────┘          │          └──────────────┘
                            │
                  ┌─────────▼───────────┐
                  │   INVENTORY         │
                  │ (Generic)           │
                  └─────────────────────┘
```

| Type | Description | Example | Build or Buy |
|------|-------------|---------|--------------|
| **Core Domain** | Competitive advantage, most complex | Payment processing | Build in-house |
| **Supporting** | Needed but not unique | Customer management | Build or simplify |
| **Generic** | Commodity, no competitive value | Email, logging, auth | Buy or use OSS |

## Exercises

1. Identify 3 bounded contexts in a project you know. Define the ubiquitous language for each.
2. Design an Entity with Value Objects for a business domain you understand.
3. Create an Aggregate with a clear aggregate root and internal entities.
4. Implement a Domain Event and a handler that processes it.
5. Map your current project's packages to DDD building blocks (Entity, VO, Aggregate, Service, Repository).
