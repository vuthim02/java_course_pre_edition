# Message Queues: Kafka, RabbitMQ

Message queues enable asynchronous, decoupled communication between services. Apache Kafka excels at high-throughput event streaming and persistence. RabbitMQ provides flexible routing with exchanges and bindings.

## Kafka — Native Producer and Consumer

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-clients</artifactId>
</dependency>
```

### Producer API

```java
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import java.util.Properties;
import java.util.concurrent.Future;

public class KafkaProducerExample {

    private final KafkaProducer<String, String> producer;

    public KafkaProducerExample(String bootstrapServers) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");          // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once semantics
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);         // Wait 5ms to batch

        this.producer = new KafkaProducer<>(props);
    }

    // Fire-and-forget
    public void sendFireAndForget(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value));
    }

    // Synchronous send
    public RecordMetadata sendSync(String topic, String key, String value) throws Exception {
        return producer.send(new ProducerRecord<>(topic, key, value)).get();
    }

    // Asynchronous with callback
    public void sendAsync(String topic, String key, String value) {
        producer.send(new ProducerRecord<>(topic, key, value),
            (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Failed to send: " + exception.getMessage());
                    return;
                }
                System.out.printf("Sent to topic=%s partition=%d offset=%d%n",
                    metadata.topic(), metadata.partition(), metadata.offset());
            });
    }

    // Send with headers
    public void sendWithHeaders(String topic, String key, String value,
                                 Map<String, String> headers) {
        ProducerRecord<String, String> record =
            new ProducerRecord<>(topic, null, key, value);
        headers.forEach((k, v) ->
            record.headers().add(k, v.getBytes()));
        producer.send(record);
    }

    // Transactional producer
    public void sendInTransaction(String topic, List<KeyValue> messages) {
        producer.initTransactions();
        try {
            producer.beginTransaction();
            for (var msg : messages) {
                producer.send(new ProducerRecord<>(topic, msg.key(), msg.value()));
            }
            producer.commitTransaction();
        } catch (Exception e) {
            producer.abortTransaction();
        }
    }

    public void close() {
        producer.close();
    }

    record KeyValue(String key, String value) {}
}
```

### Consumer API

```java
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import java.time.Duration;
import java.util.*;

public class KafkaConsumerExample {

    private final KafkaConsumer<String, String> consumer;

    public KafkaConsumerExample(String bootstrapServers, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300_000);

        this.consumer = new KafkaConsumer<>(props);
    }

    public void subscribeAndPoll(String topic) {
        consumer.subscribe(List.of(topic));

        try {
            while (true) {
                ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, String> record : records) {
                    processRecord(record);
                }

                // Manual offset commit after batch processing
                consumer.commitSync();
            }
        } finally {
            consumer.close();
        }
    }

    // Manual partition assignment (no group coordination)
    public void assignPartitions(String topic, int... partitions) {
        List<TopicPartition> topicPartitions = Arrays.stream(partitions)
            .mapToObj(p -> new TopicPartition(topic, p))
            .toList();

        consumer.assign(topicPartitions);

        // Seek to specific offset
        consumer.seekToBeginning(topicPartitions); // or seekToEnd()
        consumer.seek(topicPartitions.get(0), 100); // Specific offset
    }

    // Pause/Resume specific partitions
    public void pauseResume(String topic) {
        Set<TopicPartition> partitions = consumer.assignment();
        consumer.pause(partitions);  // stop reading
        consumer.resume(partitions); // resume reading
    }

    // Rebalance listener
    public void subscribeWithRebalanceListener(String topic) {
        consumer.subscribe(List.of(topic), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                // Commit offsets before partitions are taken away
                consumer.commitSync();
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.println("Assigned: " + partitions);
            }
        });
    }

    private void processRecord(ConsumerRecord<String, String> record) {
        System.out.printf("partition=%d offset=%d key=%s value=%s%n",
            record.partition(), record.offset(), record.key(), record.value());
    }

    public void close() {
        consumer.close();
    }
}
```

## Kafka with Spring

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
      acks: all
      properties:
        enable.idempotence: true
    consumer:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      group-id: order-service-group
      auto-offset-reset: earliest
      enable-auto-commit: false
      properties:
        spring.json.trusted.packages: "com.example.dto"
    listener:
      ack-mode: MANUAL_IMMEDIATE
      concurrency: 3
    template:
      default-topic: order-events
```

```java
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class OrderEventService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public OrderEventService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreated(OrderCreatedEvent event) {
        kafkaTemplate.send("order-events", event.orderId().toString(), event);
    }

    public void publishWithCallback(OrderCreatedEvent event) {
        kafkaTemplate.send("order-events", event)
            .thenAccept(result -> {
                var meta = result.getRecordMetadata();
                System.out.printf("Sent to partition=%d offset=%d%n",
                    meta.partition(), meta.offset());
            })
            .exceptionally(ex -> {
                System.err.println("Failed: " + ex.getMessage());
                return null;
            });
    }

    @KafkaListener(topics = "order-events", groupId = "notification-service")
    public void handleOrderEvent(@Payload OrderCreatedEvent event,
                                  @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                                  @Header(KafkaHeaders.OFFSET) long offset,
                                  Acknowledgment ack) {
        try {
            process(event);
            ack.acknowledge();
        } catch (Exception e) {
            // Log and optionally send to DLT
            kafkaTemplate.send("order-events-dlt", event);
        }
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-service")
    public void handleOrderForInventory(@Payload OrderCreatedEvent event) {
        // Inventory service processes the same event independently
    }

    // Batch consumer
    @KafkaListener(topics = "order-events", groupId = "analytics-batch",
                   containerFactory = "batchFactory")
    public void handleBatch(List<OrderCreatedEvent> events) {
        for (OrderCreatedEvent event : events) {
            processAnalytics(event);
        }
    }

    // Retryable consumer
    @KafkaListener(topics = "order-events", groupId = "retry-service",
                   containerFactory = "retryContainerFactory")
    @RetryableTopic(attempts = "5", backoff = @Backoff(delay = 2000, multiplier = 2))
    public void handleWithRetry(OrderCreatedEvent event) {
        // Will retry on failure, then send to DLQ
    }

    private void process(OrderCreatedEvent event) {
        // Business logic
    }

    private void processAnalytics(OrderCreatedEvent event) {
        // Analytics processing
    }
}
```

## RabbitMQ — Spring AMQP

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    template:
      exchange: order.exchange
      routing-key: order.created
      default-receive-queue: order.queue
    listener:
      simple:
        concurrency: 3
        max-concurrency: 10
        prefetch: 10
        acknowledge-mode: manual
        retry:
          enabled: true
          max-attempts: 3
          initial-interval: 2000
          multiplier: 2
```

### Configuration

```java
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "order.exchange";
    public static final String QUEUE = "order.queue";
    public static final String DEAD_LETTER_QUEUE = "order.queue.dlq";
    public static final String ROUTING_KEY = "order.created";

    // Exchange
    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(EXCHANGE);
    }

    // Queues with dead-lettering
    @Bean
    public Queue orderQueue() {
        return QueueBuilder.durable(QUEUE)
            .withArgument("x-dead-letter-exchange", "")
            .withArgument("x-dead-letter-routing-key", DEAD_LETTER_QUEUE)
            .withArgument("x-message-ttl", 60000) // 60 seconds
            .withArgument("x-max-length", 10000)
            .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    // Binding
    @Bean
    public Binding orderBinding(Queue orderQueue, TopicExchange exchange) {
        return BindingBuilder.bind(orderQueue)
            .to(exchange)
            .with(ROUTING_KEY);
    }

    // JSON message converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate with JSON converter
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         MessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        template.setExchange(EXCHANGE);
        template.setRoutingKey(ROUTING_KEY);
        return template;
    }
}
```

### Publisher

```java
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(OrderCreatedEvent event) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        rabbitTemplate.convertAndSend(
            RabbitMQConfig.EXCHANGE,
            RabbitMQConfig.ROUTING_KEY,
            event,
            message -> {
                message.getMessageProperties().setMessageId(event.orderId().toString());
                message.getMessageProperties().setTimestamp(new java.util.Date());
                message.getMessageProperties().setHeader("event-type", "order-created");
                return message;
            },
            correlationData);

        // Confirm callback
        correlationData.getFuture().whenComplete((confirm, ex) -> {
            if (confirm != null && confirm.isAck()) {
                System.out.println("Message confirmed: " + correlationData.getId());
            } else {
                System.err.println("Message not confirmed: " +
                    (ex != null ? ex.getMessage() : "nack"));
            }
        });
    }

    // Direct send to queue (bypass exchange)
    public void sendToQueue(String queue, Object message) {
        rabbitTemplate.convertAndSend(queue, message);
    }
}
```

### Consumer

```java
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class OrderEventConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE, concurrency = "3-10")
    public void handleOrderCreated(OrderCreatedEvent event,
                                    Channel channel,
                                    @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            processOrder(event);
            // Manual ACK
            channel.basicAck(tag, false);
        } catch (Exception e) {
            try {
                // Reject and requeue (false = don't requeue → goes to DLQ)
                channel.basicNack(tag, false, false);
            } catch (IOException ex) {
                System.err.println("Failed to nack: " + ex.getMessage());
            }
        }
    }

    @RabbitListener(queues = RabbitMQConfig.DEAD_LETTER_QUEUE)
    public void handleDeadLetter(Message message, Channel channel,
                                  @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            System.err.println("DLQ message: " + new String(message.getBody()));
            // Log and potentially alert
            channel.basicAck(tag, false);
        } catch (IOException e) {
            System.err.println("DLQ processing failed: " + e.getMessage());
        }
    }

    private void processOrder(OrderCreatedEvent event) {
        // Business logic
    }
}
```

## Message Serialization (JSON)

```java
import java.time.LocalDateTime;

public record OrderCreatedEvent(
    String orderId,
    Long customerId,
    String customerEmail,
    java.math.BigDecimal totalAmount,
    List<OrderItemEvent> items,
    LocalDateTime createdAt
) {
    public record OrderItemEvent(
        String sku,
        String productName,
        int quantity,
        java.math.BigDecimal unitPrice
    ) {}
}
```

```java
// For Kafka JSON serialization, ensure trusted packages:
// spring.kafka.consumer.properties.spring.json.trusted.packages: "*"

// Custom serializer if needed:
import org.springframework.kafka.support.serializer.JsonSerializer;

// For RabbitMQ, Jackson2JsonMessageConverter handles it automatically.
```
