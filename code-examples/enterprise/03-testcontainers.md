# Testcontainers: Integration Testing

Testcontainers provides lightweight, disposable Docker containers for integration testing. It spins up real databases, message brokers, and other services in tests, ensuring your code works against real infrastructure, not mocks.

## Dependency

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers-bom</artifactId>
    <version>1.19.3</version>
    <type>pom</type>
    <scope>import</scope>
</dependency>

<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>kafka</artifactId>
    <scope>test</scope>
</dependency>
```

## PostgreSQL Container

```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DataSource dataSource;

    @Test
    void databaseIsReachable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void saveAndFindUser() {
        User user = new User(null, "alice@example.com", "Alice");
        User saved = userRepository.save(user);

        assertNotNull(saved.id());
        Optional<User> found = userRepository.findById(saved.id());
        assertTrue(found.isPresent());
        assertEquals("alice@example.com", found.get().email());
    }
}
```

## Module-Specific JDBC URL

```java
@Testcontainers
class JdbcUrlIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withUrlParam("currentSchema", "myapp")
        .withUrlParam("ApplicationName", "integration-test");

    @Test
    void jdbcUrlContainsParams() {
        String url = postgres.getJdbcUrl();
        assertTrue(url.contains("currentSchema=myapp"));
        assertTrue(url.contains("ApplicationName=integration-test"));
        // jdbc:postgresql://localhost:5432/testdb?currentSchema=myapp&ApplicationName=integration-test
    }
}
```

## Redis Container

```java
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

@Testcontainers
class RedisIntegrationTest {

    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:7-alpine"))
        .withExposedPorts(6379);

    private static Jedis jedis;

    @BeforeAll
    static void setupJedis() {
        String host = redis.getHost();
        Integer port = redis.getMappedPort(6379);
        jedis = new Jedis(host, port);
    }

    @AfterAll
    static void tearDown() {
        if (jedis != null) jedis.close();
    }

    @Test
    void setAndGetValue() {
        jedis.set("test:key", "hello redis");
        assertEquals("hello redis", jedis.get("test:key"));
    }

    @Test
    void expireKey() throws Exception {
        jedis.setex("temp:key", 1, "gone soon");
        TimeUnit.MILLISECONDS.sleep(1100);
        assertNull(jedis.get("temp:key"));
    }
}
```

## Kafka Container

```java
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.utility.DockerImageName;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Testcontainers
class KafkaIntegrationTest {

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    private static KafkaProducer<String, String> producer;
    private static KafkaConsumer<String, String> consumer;

    @BeforeAll
    static void setupClients() {
        String bootstrapServers = kafka.getBootstrapServers();

        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(producerProps);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        consumer = new KafkaConsumer<>(consumerProps);
    }

    @AfterAll
    static void closeClients() {
        if (producer != null) producer.close();
        if (consumer != null) consumer.close();
    }

    @Test
    void produceAndConsumeMessage() throws Exception {
        String topic = "test-topic-" + System.currentTimeMillis();

        producer.send(new ProducerRecord<>(topic, "test-key", "hello kafka")).get();

        consumer.subscribe(List.of(topic));
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));

        assertEquals(1, records.count());
        var record = records.iterator().next();
        assertEquals("test-key", record.key());
        assertEquals("hello kafka", record.value());
    }
}
```

## Reusable Containers

```java
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class ReusableContainerTest {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:16-alpine"))
        .withDatabaseName("shared")
        .withUsername("shared")
        .withPassword("shared")
        .withReuse(true);

    @Container
    static PostgreSQLContainer<?> container = postgres;

    @Test
    void firstTest() {
        // Container is reused across test classes when --testcontainers.reuse.enable=true
        // is set in ~/.testcontainers.properties
        assertTrue(container.isRunning());
    }
}
```

**Configuration** (`~/.testcontainers.properties`):

```properties
testcontainers.reuse.enable=true
```

## Spring Boot with Testcontainers (Abstract Base Class)

```java
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }
}
```

```java
class OrderRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void persistOrder() {
        Order order = new Order(null, "CUST-001", BigDecimal.valueOf(99.99));
        Order saved = orderRepository.save(order);
        assertNotNull(saved.id());
    }
}
```
