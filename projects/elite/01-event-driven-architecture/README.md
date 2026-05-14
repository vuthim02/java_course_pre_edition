# Project 1: Distributed Event-Driven Microservices Architecture

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                        Event-Driven Architecture                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────────┐            │
│  │  Order   │   │ Payment  │   │Inventory │   │ Notification │            │
│  │ Service  │   │ Service  │   │ Service  │   │   Service    │            │
│  └────┬─────┘   └────┬─────┘   └────┬─────┘   └──────┬───────┘            │
│       │              │              │                 │                    │
│       └──────────────┴──────────────┴─────────────────┘                    │
│                                  │                                         │
│                         ┌────────▼────────┐                                │
│                         │    Apache       │                                │
│                         │    Kafka        │  (Event Backbone)              │
│                         │   + Schema Reg  │                                │
│                         └────────┬────────┘                                │
│                                  │                                         │
│                    ┌─────────────┼─────────────┐                           │
│                    │             │             │                           │
│              ┌─────▼────┐ ┌──────▼──────┐ ┌───▼───────┐                   │
│              │Debezium  │ │ Kafka       │ │   Dead    │                   │
│              │(CDC)     │ │ Streams     │ │  Letter   │                   │
│              │PostgreSQL│ │ (CQRS/ES)   │ │   Queue   │                   │
│              └──────────┘ └─────────────┘ └───────────┘                   │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

## Table of Contents
1. [Event Schema Design (Avro)](#event-schema-design-avro)
2. [Order Service](#order-service)
3. [Payment Service](#payment-service)
4. [Inventory Service](#inventory-service)
5. [Notification Service](#notification-service)
6. [Kafka Streams Processor](#kafka-streams-processor)
7. [Event Sourcing & CQRS](#event-sourcing--cqrs)
8. [Saga Pattern (Choreography)](#saga-pattern-choreography)
9. [Debezium CDC](#debezium-cdc)
10. [Dead Letter Queue](#dead-letter-queue)
11. [Docker Compose](#docker-compose)
12. [Testing](#testing)

---

## Event Schema Design (Avro)

### src/main/resources/avro/order-events.avsc

```json
{
  "namespace": "com.elite.events",
  "type": "record",
  "name": "OrderCreatedEvent",
  "fields": [
    {"name": "eventId", "type": "string"},
    {"name": "eventType", "type": "string"},
    {"name": "source", "type": "string"},
    {"name": "correlationId", "type": "string"},
    {"name": "causationId", "type": "string"},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "orderId", "type": "string"},
    {"name": "customerId", "type": "string"},
    {"name": "items", "type": {"type": "array", "items": {
      "type": "record", "name": "OrderItem",
      "fields": [
        {"name": "productId", "type": "string"},
        {"name": "quantity", "type": "int"},
        {"name": "unitPrice", "type": "double"}
      ]
    }}},
    {"name": "totalAmount", "type": "double"},
    {"name": "currency", "type": "string", "default": "USD"},
    {"name": "status", "type": {"type": "enum", "name": "OrderStatus",
      "symbols": ["PENDING","VERIFIED","APPROVED","PAID","SHIPPED","DELIVERED","CANCELLED","REFUNDED"]}}
  ]
}
```

```json
{
  "namespace": "com.elite.events",
  "type": "record",
  "name": "PaymentProcessedEvent",
  "fields": [
    {"name": "eventId", "type": "string"},
    {"name": "eventType", "type": "string"},
    {"name": "source", "type": "string"},
    {"name": "correlationId", "type": "string"},
    {"name": "causationId", "type": "string"},
    {"name": "timestamp", "type": "long", "logicalType": "timestamp-millis"},
    {"name": "paymentId", "type": "string"},
    {"name": "orderId", "type": "string"},
    {"name": "customerId", "type": "string"},
    {"name": "amount", "type": "double"},
    {"name": "currency", "type": "string", "default": "USD"},
    {"name": "paymentMethod", "type": {"type": "enum", "name": "PaymentMethod",
      "symbols": ["CREDIT_CARD","DEBIT_CARD","PAYPAL","STRIPE","WIRE_TRANSFER"]}},
    {"name": "status", "type": {"type": "enum", "name": "PaymentStatus",
      "symbols": ["PENDING","PROCESSING","COMPLETED","FAILED","REFUNDED"]}},
    {"name": "failureReason", "type": ["null", "string"], "default": null}
  ]
}
```

## Order Service

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.elite</groupId>
    <artifactId>order-service</artifactId>
    <version>1.0.0</version>
    <name>Order Service</name>
    <properties>
        <java.version>17</java.version>
        <avro.version>1.11.3</avro.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.kafka</groupId>
            <artifactId>kafka-streams</artifactId>
        </dependency>
        <dependency>
            <groupId>org.apache.avro</groupId>
            <artifactId>avro</artifactId>
            <version>${avro.version}</version>
        </dependency>
        <dependency>
            <groupId>io.confluent</groupId>
            <artifactId>kafka-avro-serializer</artifactId>
            <version>7.5.1</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>kafka</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <version>1.19.3</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <repositories>
        <repository>
            <id>confluent</id>
            <url>https://packages.confluent.io/maven/</url>
        </repository>
    </repositories>
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.avro</groupId>
                <artifactId>avro-maven-plugin</artifactId>
                <version>${avro.version}</version>
                <executions>
                    <execution>
                        <phase>generate-sources</phase>
                        <goals><goal>schema</goal></goals>
                        <configuration>
                            <sourceDirectory>${project.basedir}/src/main/resources/avro</sourceDirectory>
                            <outputDirectory>${project.build.directory}/generated-sources/avro</outputDirectory>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

### application.yml

```yaml
server:
  port: 8081
  tomcat:
    threads:
      max: 50
    max-connections: 1000

spring:
  application:
    name: order-service
  datasource:
    url: jdbc:postgresql://localhost:5432/orderdb
    username: order_user
    password: order_pass
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc.batch_size: 25
        order_inserts: true
        order_updates: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  kafka:
    bootstrap-servers: localhost:9092
    properties:
      schema.registry.url: http://localhost:8085
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.KafkaAvroSerializer
      properties:
        enable.idempotence: true
        acks: all
        retries: 5
        max.in.flight.requests.per.connection: 1
        compression.type: snappy
        batch.size: 16384
        linger.ms: 5
    consumer:
      group-id: order-service-group
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.KafkaAvroDeserializer
      properties:
        specific.avro.reader: true
        auto.offset.reset: earliest
        enable.auto.commit: false
        max.poll.records: 500
    listener:
      ack-mode: manual_immediate
      concurrency: 3

logging:
  level:
    com.elite: DEBUG
    org.springframework.kafka: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  tracing:
    sampling:
      probability: 1.0

---
spring:
  config:
    activate:
      on-profile: docker
  kafka:
    bootstrap-servers: kafka:9092
    properties:
      schema.registry.url: http://schema-registry:8085
  datasource:
    url: jdbc:postgresql://postgres-order:5432/orderdb
```

### OrderServiceApplication.java

```java
package com.elite.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
```

### config/AvroSerdeConfig.java

```java
package com.elite.order.config;

import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.Map;

@Configuration
public class AvroSerdeConfig {

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Bean
    public SpecificAvroSerde<com.elite.events.OrderCreatedEvent> orderCreatedEventSerde() {
        SpecificAvroSerde<com.elite.events.OrderCreatedEvent> serde = new SpecificAvroSerde<>();
        serde.configure(Map.of(
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true
        ), false);
        return serde;
    }

    @Bean
    public SpecificAvroSerde<com.elite.events.PaymentProcessedEvent> paymentProcessedEventSerde() {
        SpecificAvroSerde<com.elite.events.PaymentProcessedEvent> serde = new SpecificAvroSerde<>();
        serde.configure(Map.of(
            KafkaAvroSerializerConfig.SCHEMA_REGISTRY_URL_CONFIG, schemaRegistryUrl,
            KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true
        ), false);
        return serde;
    }
}
```

### config/KafkaConfig.java

```java
package com.elite.order.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String ORDER_EVENTS_TOPIC = "order.events";
    public static final String PAYMENT_EVENTS_TOPIC = "payment.events";
    public static final String INVENTORY_EVENTS_TOPIC = "inventory.events";
    public static final String NOTIFICATION_EVENTS_TOPIC = "notification.events";
    public static final String ORDER_SAGA_TOPIC = "order.saga";
    public static final String DEAD_LETTER_TOPIC = "order.dlq";
    public static final String ORDER_DB_EVENTS_TOPIC = "order.db.events";

    @Bean
    public NewTopic orderEventsTopic() {
        return TopicBuilder.name(ORDER_EVENTS_TOPIC)
            .partitions(6).replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(7 * 24 * 60 * 60 * 1000L))
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
            .config(TopicConfig.MIN_INSYNC_REPLICAS_CONFIG, "2")
            .build();
    }

    @Bean
    public NewTopic paymentEventsTopic() {
        return TopicBuilder.name(PAYMENT_EVENTS_TOPIC)
            .partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic inventoryEventsTopic() {
        return TopicBuilder.name(INVENTORY_EVENTS_TOPIC)
            .partitions(6).replicas(3).build();
    }

    @Bean
    public NewTopic notificationEventsTopic() {
        return TopicBuilder.name(NOTIFICATION_EVENTS_TOPIC)
            .partitions(3).replicas(3).build();
    }

    @Bean
    public NewTopic orderSagaTopic() {
        return TopicBuilder.name(ORDER_SAGA_TOPIC)
            .partitions(6).replicas(3)
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
            .build();
    }

    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(DEAD_LETTER_TOPIC)
            .partitions(3).replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(90 * 24 * 60 * 60 * 1000L))
            .build();
    }
}
```

### model/Order.java

```java
package com.elite.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_customer", columnList = "customerId"),
    @Index(name = "idx_orders_status", columnList = "status")
})
public class Order {

    @Id @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(length = 3, nullable = false)
    private String currency;

    @Version
    private Long version;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant updatedAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderItem> items = new HashSet<>();

    @Column(length = 500)
    private String notes;

    @Column(length = 36)
    private String correlationId;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (createdAt == null) createdAt = Instant.now();
        if (status == null) status = OrderStatus.PENDING;
        if (currency == null) currency = "USD";
    }

    @PreUpdate
    protected void onUpdate() { updatedAt = Instant.now(); }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Long getVersion() { return version; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Set<OrderItem> getItems() { return items; }
    public void setItems(Set<OrderItem> items) { this.items = items; }
    public void addItem(OrderItem item) { items.add(item); item.setOrder(this); }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
```

### model/OrderItem.java

```java
package com.elite.order.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 36)
    private String productId;

    @Column(nullable = false)
    private Integer quantity;

    @Column(precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(precision = 12, scale = 2, nullable = false)
    private BigDecimal subtotal;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (subtotal == null && unitPrice != null && quantity != null) {
            subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
```

### model/OrderStatus.java

```java
package com.elite.order.model;

public enum OrderStatus {
    PENDING, VERIFIED, APPROVED, PAYMENT_PENDING, PAYMENT_COMPLETED,
    INVENTORY_RESERVED, SHIPPED, DELIVERED, CANCELLED, REFUNDED, FAILED
}
```

### model/OrderEventStore.java

```java
package com.elite.order.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_event_store", indexes = {
    @Index(name = "idx_event_store_order", columnList = "orderId"),
    @Index(name = "idx_event_store_type", columnList = "eventType")
})
public class OrderEventStore {

    @Id @Column(length = 36)
    private String id;

    @Column(nullable = false, length = 36)
    private String orderId;

    @Column(nullable = false, length = 50)
    private String eventType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String eventPayload;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(nullable = false)
    private Long version;

    @Column(length = 36)
    private String correlationId;

    @Column(length = 36)
    private String causationId;

    @Column(length = 50)
    private String source;

    @PrePersist
    protected void onCreate() {
        if (id == null) id = UUID.randomUUID().toString();
        if (timestamp == null) timestamp = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventPayload() { return eventPayload; }
    public void setEventPayload(String eventPayload) { this.eventPayload = eventPayload; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
```

### repository/OrderRepository.java

```java
package com.elite.order.repository;

import com.elite.order.model.Order;
import com.elite.order.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByCustomerId(String customerId);
    List<Order> findByStatus(OrderStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") String id);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt < :since")
    List<Order> findStaleOrders(@Param("status") OrderStatus status,
                                @Param("since") Instant since);

    long countByStatus(OrderStatus status);

    @Query("SELECT o FROM Order o WHERE o.correlationId = :correlationId")
    Optional<Order> findByCorrelationId(@Param("correlationId") String correlationId);
}
```

### repository/OrderEventStoreRepository.java

```java
package com.elite.order.repository;

import com.elite.order.model.OrderEventStore;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderEventStoreRepository extends JpaRepository<OrderEventStore, String> {
    List<OrderEventStore> findByOrderIdOrderByVersionAsc(String orderId);
    List<OrderEventStore> findByEventType(String eventType);
}
```

### dto/CreateOrderRequest.java

```java
package com.elite.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequest {

    @NotBlank
    private String customerId;

    @NotEmpty @Valid
    private List<OrderItemRequest> items;

    private String currency;
    private String notes;

    public static class OrderItemRequest {
        @NotBlank
        private String productId;
        @Positive
        private Integer quantity;
        @Positive
        private BigDecimal unitPrice;

        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
```

### dto/OrderResponse.java

```java
package com.elite.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderResponse {
    private String id;
    private String customerId;
    private String status;
    private BigDecimal totalAmount;
    private String currency;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponse> items;
    private String notes;
    private String correlationId;

    public static class OrderItemResponse {
        private String id;
        private String productId;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getProductId() { return productId; }
        public void setProductId(String productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
        public BigDecimal getSubtotal() { return subtotal; }
        public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public List<OrderItemResponse> getItems() { return items; }
    public void setItems(List<OrderItemResponse> items) { this.items = items; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
}
```

### dto/OrderEventDTO.java

```java
package com.elite.order.dto;

import java.time.Instant;

public class OrderEventDTO {
    private String id;
    private String orderId;
    private String eventType;
    private String eventPayload;
    private Instant timestamp;
    private Long version;
    private String correlationId;
    private String causationId;
    private String source;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public String getEventPayload() { return eventPayload; }
    public void setEventPayload(String eventPayload) { this.eventPayload = eventPayload; }
    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getCorrelationId() { return correlationId; }
    public void setCorrelationId(String correlationId) { this.correlationId = correlationId; }
    public String getCausationId() { return causationId; }
    public void setCausationId(String causationId) { this.causationId = causationId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
