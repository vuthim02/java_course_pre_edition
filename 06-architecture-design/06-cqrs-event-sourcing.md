# Architecture & Design — Lesson 6: CQRS & Event Sourcing

## Why CQRS?

Traditional CRUD uses the **same model** for reads and writes. This works for simple apps but breaks down at scale:

```
CRUD Problem:
┌─────────────────────────────────────────────────────────────┐
│                    SAME MODEL (Order)                         │
│                                                               │
│  ┌──────────────┐               ┌──────────────────────┐     │
│  │ WRITE        │               │ READ                  │     │
│  │ • createOrder│               │ • getOrderSummary     │     │
│  │ • addItem    │               │ • listUserOrders      │     │
│  │ • submit     │               │ • searchOrders        │     │
│  │ • cancel     │  SAME ENTITY  │ • getOrderDetails     │     │
│  │ • refund     │──────────────▶│ • analyticsReport     │     │
│  └──────────────┘               └──────────────────────┘     │
│                                                               │
│  ❌ Read queries need different data shapes                   │
│  ❌ Write model has complex validation, read model doesn't     │
│  ❌ Can't optimize reads without affecting writes              │
│  ❌ High read load impacts write performance                   │
└─────────────────────────────────────────────────────────────┘
```

**CQRS** separates the read model from the write model. Different data structures, different databases, different optimizations.

```
CQRS:
┌─────────────────────────────────────────────────────────────┐
│                                                               │
│  ┌──────────────────┐     ┌────────────────────────────┐     │
│  │   COMMAND SIDE   │     │       QUERY SIDE            │     │
│  │   (Write Model)  │     │      (Read Model)           │     │
│  │                  │     │                              │     │
│  │ Commands:        │     │ Queries:                     │     │
│  │ • CreateOrder    │     │ • OrderSummary              │     │
│  │ • AddItem        │     │ • OrderListDTO              │     │
│  │ • SubmitOrder    │     │ • OrderSearchResult         │     │
│  │ • CancelOrder    │     │ • OrderAnalytics            │     │
│  │                  │     │                              │     │
│  │ Write DB:        │     │ Read DB:                     │     │
│  │ PostgreSQL       │────▶│ Elasticsearch / Redis /      │     │
│  │ (normalized)     │     │ MongoDB (denormalized)       │     │
│  └──────────────────┘     └────────────────────────────┘     │
│                                                               │
│  ✅ Read model optimized for QUERIES                          │
│  ✅ Write model optimized for VALIDATION + CONSISTENCY        │
│  ✅ Scale reads and writes independently                      │
└─────────────────────────────────────────────────────────────┘
```

## CQRS Implementation

### Command Model

```java
// Command — intent to change state
public record CreateOrderCommand(UUID customerId, List<OrderItemCommand> items) {}
public record AddItemCommand(UUID orderId, UUID productId, int quantity) {}
public record SubmitOrderCommand(UUID orderId) {}

// Command Handler
@Component
public class CreateOrderHandler implements CommandHandler<CreateOrderCommand, UUID> {

    private final OrderWriteRepository orderRepository;
    private final ProductCatalog productCatalog;

    @Override
    public UUID handle(CreateOrderCommand command) {
        Order order = new Order(UUID.randomUUID(), command.customerId());

        for (var item : command.items()) {
            Product product = productCatalog.findById(item.productId());
            order.addItem(product, item.quantity());
        }

        orderRepository.save(order);
        return order.getId();
    }
}

// Write model — focused on behavior
@Entity
@Table(name = "orders_write")
public class Order {
    @Id private UUID id;
    private UUID customerId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    @OneToMany(cascade = ALL)
    private List<OrderItem> items;

    public void addItem(Product product, int quantity) {
        if (status != OrderStatus.DRAFT) {
            throw new IllegalStateException("Can only add items to DRAFT orders");
        }
        items.add(new OrderItem(product, quantity));
    }

    public void submit() {
        if (items.isEmpty()) throw new IllegalStateException("Empty order");
        this.status = OrderStatus.SUBMITTED;
    }
}
```

### Query Model

```java
// Query — request for data (no side effects)
public record GetOrderSummaryQuery(UUID orderId) {}
public record SearchOrdersQuery(String customerEmail, OrderStatus status, int page, int size) {}

// Query Handler
@Component
public class GetOrderSummaryHandler implements QueryHandler<GetOrderSummaryQuery, OrderSummary> {

    private final OrderReadRepository orderReadRepository;

    @Override
    public OrderSummary handle(GetOrderSummaryQuery query) {
        return orderReadRepository.findById(query.orderId())
            .orElseThrow(() -> new OrderNotFoundException(query.orderId()));
    }
}

// Read model — flat, denormalized, optimized for display
@Document(collection = "order_summaries")
public class OrderSummary {
    @Id private UUID id;
    private UUID customerId;
    private String customerName;
    private String customerEmail;
    private String status;
    private BigDecimal total;
    private int itemCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

### Synchronizing Read Model

```java
@Component
public class OrderEventSynchronizer {

    @EventListener
    public void on(OrderCreatedEvent event) {
        OrderSummary summary = new OrderSummary();
        summary.setId(event.orderId());
        summary.setCustomerId(event.customerId());
        summary.setStatus("DRAFT");
        summary.setTotal(BigDecimal.ZERO);
        summary.setCreatedAt(event.occurredAt());
        orderSummaryRepository.save(summary);
    }

    @EventListener
    public void on(OrderSubmittedEvent event) {
        orderSummaryRepository.findById(event.orderId())
            .ifPresent(summary -> {
                summary.setStatus("SUBMITTED");
                summary.setTotal(event.total());
                summary.setItemCount(event.itemCount());
                orderSummaryRepository.save(summary);
            });
    }
}
```

## Event Sourcing

Instead of storing the **current state**, event sourcing stores **every state change**. Current state is derived by replaying all events.

```
Traditional:                           Event Sourcing:
┌──────────────────┐                  ┌──────────────────┐
│ orders table     │                  │ events table      │
│                  │                  │                   │
│ id: 1            │                  │ OrderCreated      │
│ status: SHIPPED  │                  │ ItemAdded         │
│ total: $150.00   │                  │ ItemAdded         │
│                  │                  │ OrderSubmitted    │
│ ❌ What changed? │                  │ PaymentProcessed  │
│ ❌ When?         │                  │ OrderShipped      │
│ ❌ Why?          │                  │                   │
│ (last update     │                  │ ✅ Full history!  │
│  overwritten)    │                  │ ✅ Audit trail    │
└──────────────────┘                  │ ✅ Replay to fix  │
                                       │   bugs            │
                                       └──────────────────┘
```

### Event Store

```java
// Event — immutable record of something that happened
public interface DomainEvent {
    UUID getAggregateId();
    Instant getOccurredAt();
}

public record OrderCreatedEvent(
    UUID aggregateId,
    UUID customerId,
    Instant occurredAt
) implements DomainEvent {}

public record ItemAddedEvent(
    UUID aggregateId,
    UUID productId,
    int quantity,
    Money price,
    Instant occurredAt
) implements DomainEvent {}

public record OrderSubmittedEvent(
    UUID aggregateId,
    Money total,
    int itemCount,
    Instant occurredAt
) implements DomainEvent {}
```

### Event-Sourced Aggregate

```java
public class Order {
    private UUID id;
    private UUID customerId;
    private OrderStatus status;
    private List<OrderItem> items = new ArrayList<>();
    private long version = 0;

    // Constructor — creates initial event
    public Order(UUID id, UUID customerId) {
        applyEvent(new OrderCreatedEvent(id, customerId, Instant.now()));
    }

    // Rebuild state from events (used when loading from event store)
    public Order(List<DomainEvent> events) {
        events.forEach(this::applyEvent);
    }

    public void addItem(Product product, int quantity) {
        applyEvent(new ItemAddedEvent(id, product.getId(), quantity,
            product.getPrice(), Instant.now()));
    }

    public void submit() {
        applyEvent(new OrderSubmittedEvent(id, getTotal(), items.size(), Instant.now()));
    }

    // Apply event = change state + record event
    private void applyEvent(DomainEvent event) {
        switch (event) {
            case OrderCreatedEvent e -> {
                this.id = e.aggregateId();
                this.customerId = e.customerId();
                this.status = OrderStatus.DRAFT;
            }
            case ItemAddedEvent e -> {
                this.items.add(new OrderItem(e.productId(), e.quantity(), e.price()));
            }
            case OrderSubmittedEvent e -> {
                this.status = OrderStatus.SUBMITTED;
            }
            default -> {}
        }
        version++;
    }

    // Events that haven't been persisted yet
    private final List<DomainEvent> newEvents = new ArrayList<>();
    public List<DomainEvent> getNewEvents() { return List.copyOf(newEvents); }
}
```

### Event Store Implementation

```java
@Component
public class EventStore {

    @Autowired
    private JdbcTemplate jdbc;

    public void saveEvents(UUID aggregateId, List<DomainEvent> events, long expectedVersion) {
        String sql = "INSERT INTO events (aggregate_id, event_type, data, version, occurred_at) " +
                     "VALUES (?, ?, ?, ?, ?)";

        for (DomainEvent event : events) {
            jdbc.update(sql,
                aggregateId,
                event.getClass().getSimpleName(),
                serialize(event),
                expectedVersion + 1,
                event.getOccurredAt()
            );
        }
    }

    public List<DomainEvent> loadEvents(UUID aggregateId) {
        return jdbc.query(
            "SELECT * FROM events WHERE aggregate_id = ? ORDER BY version",
            new EventRowMapper(),
            aggregateId
        );
    }
}
```

## Event Sourcing + CQRS Together

```
┌────────────┐     Command     ┌──────────────────┐     Event     ┌────────────┐
│  Client    │────────────────▶│  Command Handler  │──────────────▶│Event Store  │
│            │                 │  (Validate +      │              │(Source of  │
│  (Write)   │                 │   Apply)          │              │ Truth)     │
└────────────┘                 └──────────────────┘              └──────┬─────┘
                                                                        │
                                                                        │ Event
                                                                        ▼
┌────────────┐     Query      ┌──────────────────┐     Project    ┌────────────┐
│  Client    │◀───────────────│  Query Handler   │◀───────────────│ Projection │
│            │                 │  (Read from      │                │ (Denorm-   │
│  (Read)    │                 │   Projection)    │                │  alizer)   │
└────────────┘                 └──────────────────┘                └────────────┘
```

## When to Use CQRS/Event Sourcing

| Use When | Don't Use When |
|----------|----------------|
| Complex business rules with audit requirements | Simple CRUD (use standard JPA) |
| Need complete audit trail | Prototypes or MVPs |
| Multiple read models for same data | Single read-write model works fine |
| High write load with different read patterns | Small team without DDD experience |
| Collaborative domains (multiple users changing same data) | Simple reporting needs |

## Exercises

1. Separate a CRUD service into command and query handlers.
2. Implement a separate read model (e.g., MongoDB or a denormalized DB view).
3. Create an event-sourced aggregate for a Bank Account (deposit, withdraw, transfer).
4. Build a projection that rebuilds the read model from events.
5. Compare read performance between CRUD and CQRS approaches.
