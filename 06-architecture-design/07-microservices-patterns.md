# Architecture & Design — Lesson 7: Microservices Patterns

## Microservices Challenges

Microservices solve some problems but create others:

```
Challenge:                        Solution (Pattern):
┌────────────────────────────┐   ┌───────────────────────────┐
│ How do services find each │   │ Service Discovery         │
│ other?                     │   │ (Eureka, Consul)          │
├────────────────────────────┤   ├───────────────────────────┤
│ How to handle failures?    │   │ Circuit Breaker           │
│                            │   │ (Resilience4j)            │
├────────────────────────────┤   ├───────────────────────────┤
│ How to query across       │   │ API Composition / CQRS    │
│ services?                  │   │                           │
├────────────────────────────┤   ├───────────────────────────┤
│ How to keep data          │   │ Saga (Choreography /      │
│ consistent?                │   │ Orchestration)            │
├────────────────────────────┤   ├───────────────────────────┤
│ How to deploy?             │   │ CI/CD + Containerization  │
├────────────────────────────┤   ├───────────────────────────┤
│ How to monitor?            │   │ Distributed Tracing +     │
│                            │   │ Centralized Logging       │
└────────────────────────────┘   └───────────────────────────┘
```

## Decomposition Patterns

### Decompose by Business Capability

```
WRONG (Technical):                      RIGHT (Business):
┌────────────────────┐                  ┌────────────────────┐
│ Frontend Service   │                  │ Order Service      │
│ Backend Service    │                  │ Payment Service    │
│ Database Service   │                  │ Inventory Service  │
│                    │                  │ Shipping Service   │
│ ❌ Still a monolith│                  │ Notification Svc   │
│   in disguise      │                  │                    │
└────────────────────┘                  │ Each has its OWN   │
                                         │ database and code  │
                                         └────────────────────┘
```

### Strangler Fig Pattern

Gradually replace a monolith. New functionality in microservices; old functionality stays until migrated.

```
           ┌─────────────────────────────────────────┐
           │            MONOLITH                      │
           │  old/orders, old/users, old/payments     │
           └────┬──────┬──────┬──────┬────────────────┘
                │      │      │      │
     ┌──────────┘      │      │      └──────────────┐
     ▼                 ▼      ▼                     ▼
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│New Orders│   │ New Users│   │New       │   │Monolith  │
│Service   │   │ Service  │   │Payments  │   │(shrinking)│
└──────────┘   └──────────┘   └──────────┘   └──────────┘
                                               │
                                         Eventually deleted
```

## Communication Patterns

### API Gateway

```
┌──────────┐
│  Client  │
└────┬─────┘
     │
┌────▼─────────────────────────────────────────┐
│            API GATEWAY                        │
│                                                │
│  Routing → Auth → Rate Limit → Log → Cache   │
└────┬──────────┬──────────┬──────────┬─────────┘
     │          │          │          │
     ▼          ▼          ▼          ▼
┌────────┐ ┌────────┐ ┌────────┐ ┌────────┐
│ Orders │ │ Users  │ │Payment │ │Inventory│
└────────┘ └────────┘ └────────┘ └────────┘
```

### Backend for Frontend (BFF)

Different clients get different backends:

```java
// Mobile BFF — lightweight responses, less data
@RestController
@RequestMapping("/bff/mobile/v1/orders")
public class MobileOrderController {
    @GetMapping
    public List<MobileOrderSummary> getOrders() {
        return orderService.findSummaries();  // Minimal fields
    }
}

// Web BFF — rich responses, more data
@RestController
@RequestMapping("/bff/web/v1/orders")
public class WebOrderController {
    @GetMapping
    public List<WebOrderDetail> getOrders() {
        return orderService.findFullDetails();  // Rich data
    }
}
```

## Data Patterns

### Database per Service

```
WRONG — Shared Database:            RIGHT — Database per Service:
┌────────────────────────────┐     ┌────────────────────────────┐
│ Order Service   │ User Svc│     │ Order Service               │
│                 │         │     │ ┌──────────────────────┐    │
│  ORDERS DB ──── USERS DB │     │ │ Order DB (PostgreSQL) │    │
│  (SINGLE DATABASE)        │     │ └──────────────────────┘    │
│                           │     │                              │
│ ❌ Tight coupling         │     │ User Service                 │
│ ❌ Schema changes         │     │ ┌──────────────────────┐    │
│ ❌ One team can break     │     │ │ User DB (PostgreSQL) │    │
│    everyone               │     │ └──────────────────────┘    │
└────────────────────────────┘     │                              │
                                    │ Payment Service             │
                                    │ ┌──────────────────────┐    │
                                    │ │ Payment DB (DynamoDB)│    │
                                    │ └──────────────────────┘    │
                                    │ ✅ Each team owns its data  │
                                    └────────────────────────────┘
```

### Saga Pattern — Distributed Transactions

A saga is a sequence of local transactions. Each step publishes an event. If a step fails, compensating transactions undo previous steps.

#### Choreography Saga (Event-based)

```
Order Service      Payment Service      Inventory Service     Notification
     │                   │                    │                    │
     │──OrderCreated─────▶                    │                    │
     │                   │──PaymentProcessed──▶                    │
     │                   │                    │──StockReserved─────▶
     │                   │                    │                    │──EmailSent
     │                   │                    │                    │
     │  ❌ Payment Failed                     │                    │
     │                   │                    │                    │
     │◀──PaymentFailed───│                    │                    │
     │  (Compensation)   │──StockRollback────▶│                    │
     │                   │                    │──NotificationRollback
```

```java
// Order Service — publishes event
@Service
public class OrderSaga {

    @Autowired
    private EventPublisher publisher;

    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        publisher.publish(new OrderCreatedEvent(order.getId(), order.getTotal()));
    }

    @EventListener
    public void on(PaymentFailedEvent event) {
        orderRepository.findById(event.orderId())
            .ifPresent(order -> {
                order.setStatus(OrderStatus.FAILED);
                orderRepository.save(order);
            });
    }
}

// Payment Service — listens and compensates
@Service
public class PaymentSaga {

    @EventListener
    public void on(OrderCreatedEvent event) {
        try {
            paymentService.processPayment(event.orderId(), event.total());
            publisher.publish(new PaymentProcessedEvent(event.orderId()));
        } catch (PaymentException e) {
            publisher.publish(new PaymentFailedEvent(event.orderId()));
        }
    }
}
```

#### Orchestration Saga (Central coordinator)

```
                     ┌─────────────────────────────────┐
                     │        ORDER SAGA ORCHESTRATOR  │
                     │                                  │
                     │  1. Create Order                 │
                     │  2. Process Payment              │
                     │  3. Reserve Inventory            │
                     │  4. Send Notification            │
                     │  5. If any fails → compensate    │
                     └────┬──────────┬──────────┬───────┘
                          │          │          │
                          ▼          ▼          ▼
                   ┌────────┐ ┌────────┐ ┌────────┐
                   │ Order  │ │Payment │ │Inventory│
                   │ Service│ │Service │ │Service  │
                   └────────┘ └────────┘ └────────┘
```

```java
@Component
public class OrderSagaOrchestrator {

    public void execute(Order order) {
        try {
            // Step 1: Create order
            orderService.create(order);

            // Step 2: Process payment
            paymentService.process(order.getPayment());

            // Step 3: Reserve inventory
            inventoryService.reserve(order.getItems());

            // Step 4: Notify
            notificationService.sendConfirmation(order);

        } catch (PaymentException e) {
            // Compensate: cancel order
            orderService.cancel(order.getId());
        } catch (InventoryException e) {
            // Compensate: refund payment + cancel order
            paymentService.refund(order.getPayment().getId());
            orderService.cancel(order.getId());
        }
    }
}
```

## Deployment Patterns

### Blue-Green Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                     LOAD BALANCER                            │
│                                                               │
│                    ┌─────────────────┐                        │
│                    │  Route to BLUE  │                        │
│                    └────────┬────────┘                        │
│                             │                                 │
│              ┌──────────────┼──────────────┐                  │
│              ▼              ▼              ▼                  │
│        ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│        │BLUE v2.0 │  │BLUE v2.0 │  │BLUE v2.0 │  LIVE       │
│        └──────────┘  └──────────┘  └──────────┘              │
│        ┌──────────┐  ┌──────────┐  ┌──────────┐              │
│        │GREEN v1.0│  │GREEN v1.0│  │GREEN v1.0│  STANDBY    │
│        └──────────┘  └──────────┘  └──────────┘              │
│                                                               │
│ 1. Deploy new version to GREEN                               │
│ 2. Test GREEN                                                │
│ 3. Switch load balancer to GREEN                             │
│ 4. Keep BLUE as rollback target for 24 hours                 │
└─────────────────────────────────────────────────────────────┘
```

### Canary Deployment

```
┌─────────────────────────────────────────────────────────────┐
│                     LOAD BALANCER                            │
│                                                               │
│        90% traffic                    10% traffic            │
│              │                              │                │
│              ▼                              ▼                │
│        ┌──────────┐                  ┌──────────┐           │
│        │OLD v1.0  │                  │NEW v2.0  │           │
│        │(stable)  │                  │(canary)  │           │
│        └──────────┘                  └──────────┘           │
│                                                               │
│ Monitor: error rate, latency, CPU                            │
│ If canary is STABLE → increase to 50%, then 100%             │
│ If canary FAILS → route all traffic back to old version       │
└─────────────────────────────────────────────────────────────┘
```

## Observability Patterns

### Health Check API

```java
@RestController
@RequestMapping("/actuator/health")
public class HealthController {

    @GetMapping
    public Map<String, Object> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("database", checkDatabase());
        status.put("redis", checkRedis());
        status.put("kafka", checkKafka());
        return status;
    }

    private String checkDatabase() {
        try { database.query("SELECT 1"); return "UP"; }
        catch (Exception e) { return "DOWN"; }
    }
}
```

### Log Aggregation Pattern

```
┌──────────┐  ┌──────────┐  ┌──────────┐
│Service A │  │Service B │  │Service C │
│  logs    │  │  logs    │  │  logs    │
└────┬─────┘  └────┬─────┘  └────┬─────┘
     │             │             │
     └─────────────┼─────────────┘
                   │
                   ▼
            ┌──────────────┐
            │  Log Aggregator│
            │  (ELK/Loki)   │
            └──────────────┘
```

## Anti-Patterns to Avoid

| Anti-Pattern | Why It's Bad | Fix |
|-------------|--------------|-----|
| **Distributed monolith** | Services tightly coupled, need coordinated deploys | Properly decouple with async communication |
| **Shared database** | Services coupled through schema | Database per service |
| **Too fine-grained** | Too many services, overhead > value | Merge related functionality |
| **No monitoring** | Can't debug production issues | Add tracing, metrics, centralized logs |
| **Chatty communication** | Services call each other in loops | Aggregate data, use async events |

## Exercises

1. Identify two services in your system that should communicate via events instead of HTTP.
2. Implement a choreography saga for order → payment → inventory.
3. Set up blue-green deployment using your CI/CD platform.
4. Create a health check endpoint that verifies all service dependencies.
5. Add distributed tracing to a service-to-service call chain.
