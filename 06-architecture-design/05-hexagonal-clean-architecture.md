# Architecture & Design — Lesson 5: Hexagonal & Clean Architecture

## Why Architecture Matters

```
No Architecture (Big Ball of Mud):      Clean Architecture:
┌──────────────────────────────┐       ┌──────────────────────────────┐
│                              │       │         DOMAIN               │
│  Controller   Service   Repo │       │    (Business Logic)          │
│    │            │        │    │       │                              │
│    │    DB Code in      │    │       │  ┌────────────────────────┐  │
│    │    Controller!     │    │       │  │ Entities & Use Cases   │  │
│    ▼            ▼        ▼    │       │  └────────────────────────┘  │
│  All mixed together        │       │            ▲    ▲              │
│                              │       │            │    │              │
│  ❌ Can't test without DB  │       │  ┌──────────┴────┴──────────┐ │
│  ❌ Can't swap DB          │       │  │     ADAPTERS (in/out)     │ │
│  ❌ Business logic         │       │  │  (Controllers,            │ │
│  ❌ scattered everywhere   │       │  │   Repositories, etc.)     │ │
└──────────────────────────────┘       └──────────────────────────────┘
```

## Hexagonal Architecture (Ports & Adapters)

The core idea: **Your application has ports (interfaces). Adapters plug into those ports.**

```
                    ┌──────────────────────────────────┐
                    │         INFRASTRUCTURE            │
                    │  ┌────────────────────────────┐   │
                    │  │        ADAPTERS             │   │
                    │  │                            │   │
  ┌────────┐       │  │  ┌──────┐  ┌──────┐       │   │       ┌────────┐
  │ HTTP   │───────┼──┼──▶Controller│  │      │       │   │       │ Domain │
  │ Client │       │  │  │(inbound │  │      │       │   │       │  Core  │
  └────────┘       │  │  │ adapter)│  │      │       │   │       └────────┘
                    │  │  └────┬───┘  │      │       │   │
  ┌────────┐       │  │       │PORT  │      │       │   │
  │ Queue  │───────┼──┼──▶Message  │      │       │   │
  │(Kafka) │       │  │  │Listener │      │       │   │
  └────────┘       │  │  └────┬───┘  │      │       │   │
                    │  │       │      │      │       │   │
                    │  │  ┌────▼───┐  │      │       │   │
                    │  │  │Service │  │      │       │   │
                    │  │  │(use    │  │      │       │   │
                    │  │  │ cases) │  │      │       │   │
                    │  │  └────┬───┘  │      │       │   │
                    │  │       │      │      │       │   │
                    │  │  ┌────▼───┐  │      │       │   │
                    │  │  │Repository│  │PORT │       │   │
                    │  │  │(outbound│  │      │       │   │
                    │  │  │ port)  │  │      │       │   │
                    │  │  └────┬───┘  │      │       │   │
                    │  │       │      │      │       │   │
                    │  │  ┌────▼───┐  │      │       │   │
                    │  │  │JPA/    │  │      │       │   │
                    │  │  │JDBC    │  │      │       │   │
                    │  │  │Adapter │  │      │       │   │
                    │  │  └────────┘  │      │       │   │
                    │  └──────────────┘      │       │   │
                    └──────────────────────────────┘   │
                                                        │
                                         ┌──────────────┴┐
                                         │  Database     │
                                         │  (PostgreSQL) │
                                         └───────────────┘
```

### The Dependency Rule

**Dependencies point INWARD.** Domain code knows NOTHING about frameworks, databases, or web servers.

```
✅ Correct:                               ❌ Wrong:

Domain ← Use Cases ← Adapters            Adapters → Use Cases → Domain
(no imports from                          (imports Spring, JPA,
 Spring, JPA, DB)                          DB in domain!)
```

## Package Structure

```
com.example.order/
├── domain/                      # INNERMOST — pure Java
│   ├── model/
│   │   ├── Order.java           # Entity
│   │   ├── OrderId.java         # Value Object
│   │   ├── OrderLine.java       # Entity
│   │   └── Money.java           # Value Object
│   ├── service/
│   │   └── OrderService.java    # Domain service
│   ├── port/
│   │   ├── inbound/
│   │   │   └── CreateOrderUseCase.java    # Inbound port
│   │   └── outbound/
│   │       ├── OrderRepository.java       # Outbound port
│   │       └── PaymentGateway.java        # Outbound port
│   └── event/
│       └── OrderPlacedEvent.java
│
├── application/                  # USE CASES
│   └── usecase/
│       └── CreateOrderService.java   # Implements inbound port
│
└── infrastructure/              # OUTERMOST — adapters
    ├── adapter/
    │   ├── inbound/
    │   │   ├── rest/
    │   │   │   └── OrderController.java       # REST adapter
    │   │   └── messaging/
    │   │       └── OrderEventConsumer.java     # Kafka adapter
    │   └── outbound/
    │       ├── persistence/
    │       │   ├── JpaOrderRepository.java    # JPA adapter
    │       │   └── OrderEntity.java           # JPA entity (separate!)
    │       └── payment/
    │           └── StripePaymentGateway.java  # Stripe adapter
    └── config/
        └── BeanConfiguration.java
```

## Clean Architecture in Java

### Domain Layer — Pure Business Logic

```java
// domain/model/Order.java
public class Order {
    private final OrderId id;
    private OrderStatus status;
    private final List<OrderLine> lines;
    private Money total;

    public Order(OrderId id) {
        this.id = id;
        this.status = OrderStatus.PENDING;
        this.lines = new ArrayList<>();
        this.total = Money.zero(Currency.USD);
    }

    public void addProduct(ProductId productId, int quantity, Money price) {
        lines.add(new OrderLine(productId, quantity, price));
        this.total = this.total.add(price.multiply(quantity));
    }

    public void submit() {
        if (lines.isEmpty()) throw new IllegalStateException("Empty order");
        this.status = OrderStatus.SUBMITTED;
    }
}

// domain/port/inbound/CreateOrderUseCase.java
public interface CreateOrderUseCase {
    Order createOrder(CreateOrderCommand command);
}

// domain/port/outbound/OrderRepository.java
public interface OrderRepository {
    Optional<Order> findById(OrderId id);
    void save(Order order);
}
```

### Application Layer — Use Cases

```java
// application/usecase/CreateOrderService.java
public class CreateOrderService implements CreateOrderUseCase {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;

    public CreateOrderService(OrderRepository orderRepository,
                               ProductCatalog productCatalog) {
        this.orderRepository = orderRepository;
        this.productCatalog = productCatalog;
    }

    @Override
    public Order createOrder(CreateOrderCommand command) {
        OrderId orderId = OrderId.generate();
        Order order = new Order(orderId);

        for (var item : command.items()) {
            Product product = productCatalog.findById(item.productId());
            order.addProduct(product.getId(), item.quantity(), product.getPrice());
        }

        order.submit();
        orderRepository.save(order);
        return order;
    }
}
```

### Infrastructure Layer — Adapters

```java
// infrastructure/adapter/inbound/rest/OrderController.java
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrder;

    public OrderController(CreateOrderUseCase createOrder) {
        this.createOrder = createOrder;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest request) {
        var command = new CreateOrderCommand(request.userId(), request.items());
        Order order = createOrder.createOrder(command);
        return ResponseEntity.ok(OrderResponse.from(order));
    }
}

// infrastructure/adapter/outbound/persistence/JpaOrderRepository.java
@Repository
public class JpaOrderRepository implements OrderRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void save(Order order) {
        OrderEntity entity = OrderEntity.fromDomain(order);
        entityManager.persist(entity);
    }

    @Override
    public Optional<Order> findById(OrderId id) {
        return Optional.ofNullable(entityManager.find(OrderEntity.class, id))
            .map(OrderEntity::toDomain);
    }
}
```

## Testing in Clean Architecture

```java
// Domain test — NO SPRING, NO DATABASE, PURE JAVA
class OrderTest {
    @Test
    void shouldNotSubmitEmptyOrder() {
        Order order = new Order(OrderId.generate());
        assertThrows(IllegalStateException.class, order::submit);
    }
}

// Use case test — mock ports
class CreateOrderServiceTest {
    @Test
    void shouldCreateOrder() {
        var repo = mock(OrderRepository.class);
        var catalog = mock(ProductCatalog.class);
        var service = new CreateOrderService(repo, catalog);

        when(catalog.findById(any())).thenReturn(new Product(/*...*/));

        Order order = service.createOrder(new CreateOrderCommand(/*...*/));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.SUBMITTED);
        verify(repo).save(any());
    }
}

// Integration test — real adapter
@SpringBootTest
class JpaOrderRepositoryTest {
    @Autowired private JpaOrderRepository repository;

    @Test
    void shouldSaveAndRetrieveOrder() {
        Order order = new Order(OrderId.generate());
        order.addProduct(/*...*/);
        repository.save(order);
        assertThat(repository.findById(order.getId())).isPresent();
    }
}
```

## Benefits

| Benefit | Why |
|---------|-----|
| **Testable domain** | Pure Java, no framework dependencies |
| **Swap infrastructure** | DB, messaging, UI — all pluggable |
| **Independent frameworks** | Spring is a detail, not a core dependency |
| **Independent UI** | REST today, GraphQL tomorrow, gRPC later |
| **Independent database** | PostgreSQL ↔ MongoDB ↔ file system |
| **Late decisions** | Defer infrastructure choices |

## When to Use

| Architecture | Best For |
|-------------|----------|
| Clean/Hexagonal | Complex business logic, microservices, long-lived projects |
| Layered (Controller → Service → Repo) | Simple CRUD apps, small teams |
| No architecture | Prototypes, throwaway code, scripts |

## Exercises

1. Identify the current architecture of your project. Is it clean or a "big ball of mud"?
2. Extract the domain layer of a service — no framework imports allowed.
3. Create a port interface for a repository and implement it with both JPA and an in-memory map.
4. Write a pure unit test for a domain entity (no Spring, no DB).
5. Refactor a controller that contains business logic into the hexagonal pattern.
