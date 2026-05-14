# Enterprise Engineering — Lesson 14: Message Queues (Kafka & RabbitMQ)

## Why Message Queues?

```
Without MQ (Synchronous):            With MQ (Asynchronous):
┌──────────┐     ┌──────────┐       ┌──────────┐     ┌──────────┐
│Order     │────▶│Email     │       │Order     │────▶│ RabbitMQ │
│Service   │     │Service   │       │Service   │     │ / Kafka  │
└──────────┘     └──────────┘       └──────────┘     └────┬─────┘
                                                        │
                                              ┌─────────┼─────────┐
                                              ▼         ▼         ▼
                                        ┌────────┐ ┌────────┐ ┌────────┐
                                        │ Email  │ │Invoice │ │Analytics│
                                        │Service │ │Service │ │Service  │
                                        └────────┘ └────────┘ └────────┘
```

| Without MQ | With MQ |
|------------|---------|
| Order service waits for email service | Order service emits event, continues |
| If email service is down, orders fail | Email service reconnects, processes later |
| Adding a notification service = change code | Add consumer, no code changes needed |
| Services are tightly coupled | Services are decoupled |

## RabbitMQ — Message Broker

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       RABBITMQ                               │
│                                                               │
│  Producer ──▶ Exchange ──▶ Queue ──▶ Consumer               │
│                                                               │
│  Exchange Types:                                              │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐    │
│  │ Direct   │  │ Topic    │  │ Fanout   │  │ Headers  │    │
│  │ (routing │  │ (pattern │  │ (broad-  │  │ (header  │    │
│  │  key=exact│  │  match)  │  │  cast)   │  │  match)  │    │
│  └──────────┘  └──────────┘  └──────────┘  └──────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Spring Boot + RabbitMQ

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
```

```properties
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
```

### Configuration

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange("order.exchange");
    }

    @Bean
    public Queue emailQueue() {
        return new Queue("email.order.queue", true);  // durable
    }

    @Bean
    public Queue inventoryQueue() {
        return new Queue("inventory.order.queue", true);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, TopicExchange exchange) {
        return BindingBuilder.bind(emailQueue)
            .to(exchange)
            .with("order.created");  // routing key
    }

    @Bean
    public Binding inventoryBinding(Queue inventoryQueue, TopicExchange exchange) {
        return BindingBuilder.bind(inventoryQueue)
            .to(exchange)
            .with("order.*");  // pattern match
    }
}
```

### Producer

```java
@Service
public class OrderEventPublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent("ORDER_CREATED", order);
        rabbitTemplate.convertAndSend(
            "order.exchange",
            "order.created",
            event
        );
    }
}
```

### Consumer

```java
@Component
public class EmailNotificationConsumer {

    @RabbitListener(queues = "email.order.queue")
    public void handleOrderCreated(OrderEvent event) {
        log.info("Sending email for order: {}", event.getOrder().getId());
        // Send email...
    }
}
```

## Apache Kafka — Event Streaming Platform

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        KAFKA CLUSTER                         │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │   Broker 1   │  │   Broker 2   │  │   Broker 3   │       │
│  │  ┌────┐      │  │  ┌────┐      │  │  ┌────┐      │       │
│  │  │P0  │      │  │  │P1  │      │  │  │P2  │      │       │
│  │  │P3  │      │  │  │P0  │      │  │  │P1  │      │       │
│  │  └────┘      │  │  └────┘      │  │  └────┘      │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  Topics: "orders" (12 partitions, 3 replicas)                │
│  Producers write to partitions (key-based)                   │
│  Consumers read from partitions (within consumer groups)     │
└─────────────────────────────────────────────────────────────┘
```

### Key Kafka Concepts

| Concept | What It Is |
|---------|------------|
| **Topic** | A category/feed (like a table) |
| **Partition** | An ordered, immutable sequence of records |
| **Offset** | Position of a record within a partition |
| **Producer** | Writes records to topics |
| **Consumer** | Reads records from topics |
| **Consumer Group** | Group of consumers sharing a workload |
| **Broker** | A Kafka server |
| **Replication** | Copies of partitions across brokers |

### Spring Boot + Kafka

```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.group-id=order-group
spring.kafka.consumer.auto-offset-reset=earliest
```

### Producer

```java
@Service
public class OrderEventProducer {

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    public void publishOrderCreated(Order order) {
        OrderEvent event = new OrderEvent("ORDER_CREATED", order);
        kafkaTemplate.send("orders", order.getId().toString(), event);
    }
}
```

### Consumer

```java
@Component
public class OrderEventConsumer {

    @KafkaListener(topics = "orders", groupId = "email-group")
    public void handleOrderCreated(OrderEvent event,
                                    @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received order event at offset {}: {}",
            offset, event.getOrder().getId());
        // Process event
    }
}
```

### Kafka Streams (Stream Processing)

```java
@Component
public class OrderAnalyticsProcessor {

    @Autowired
    private StreamsBuilder streamsBuilder;

    @PostConstruct
    public void process() {
        KStream<String, OrderEvent> stream = streamsBuilder
            .stream("orders", Consumed.with(Serdes.String(), new JsonSerde<>(OrderEvent.class)));

        stream
            .groupBy((key, event) -> event.getOrder().getCategory())
            .aggregate(
                () -> new CategoryStats(),
                (key, event, stats) -> stats.addOrder(event.getOrder()),
                Materialized.with(Serdes.String(), new JsonSerde<>(CategoryStats.class))
            )
            .toStream()
            .to("order-stats", Produced.with(Serdes.String(), new JsonSerde<>(CategoryStats.class)));
    }
}
```

## RabbitMQ vs Kafka

| Aspect | RabbitMQ | Kafka |
|--------|----------|-------|
| **Model** | Message broker (queue-based) | Event streaming (log-based) |
| **Message retention** | Deleted after consumed | Retained for configurable period |
| **Message order** | Per queue | Per partition (ordered) |
| **Throughput** | Thousands/sec | Millions/sec |
| **Latency** | Microseconds | Milliseconds |
| **Routing** | Complex (exchanges, bindings) | Simple (topics, partitions) |
| **Use case** | Task distribution, RPC | Event streaming, data pipelines |
| **Replay** | No (consumed = gone) | Yes (by offset/timestamp) |

## When to Use Which

| Scenario | Choose |
|----------|--------|
| Send email when order is placed | RabbitMQ (simple task queue) |
| Stream millions of click events per second | Kafka (high throughput) |
| Process events in order with replay capability | Kafka (log-based) |
| Need complex routing (topic, headers) | RabbitMQ (flexible exchanges) |
| Build an event-sourced system | Kafka (event store) |
| Microservices need RPC-style communication | RabbitMQ (reply queues) |

## Dead Letter Queues

Messages that fail processing go to a **dead letter queue** for analysis:

```java
@Bean
public Queue emailQueue() {
    return QueueBuilder.durable("email.order.queue")
        .deadLetterExchange("order.dlx")
        .deadLetterRoutingKey("order.dead")
        .build();
}

@Bean
public Queue deadLetterQueue() {
    return new Queue("order.dead.queue");
}

// Process dead letters
@RabbitListener(queues = "order.dead.queue")
public void handleDeadLetter(Message message) {
    log.error("Dead letter: {}", new String(message.getBody()));
    // Manual intervention or alert
}
```

## Exercises

1. Set up RabbitMQ with an exchange, queue, producer, and consumer.
2. Implement Kafka producer-consumer with JSON serialization.
3. Create a fanout exchange in RabbitMQ and connect two consumers.
4. Replay old events from Kafka using `auto-offset-reset=earliest`.
5. Set up a dead letter queue in RabbitMQ for failed messages.
