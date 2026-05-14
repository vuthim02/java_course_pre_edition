# Enterprise Engineering — Lesson 3: Testcontainers & Integration Testing

## Why Integration Testing?

Unit tests mock dependencies — but mocks lie. Your code works with mocks but fails with real databases, queues, and APIs. Integration tests with **real infrastructure** catch these failures.

```
Unit Test:                          Integration Test:
┌────────────────────┐              ┌────────────────────┐
│ UserService        │              │ UserService        │
│   ↓ (mock)         │              │   ↓ (real)         │
│ UserRepository     │              │ UserRepository     │
│   (returns fake)   │              │   ↓ (real)         │
└────────────────────┘              │ PostgreSQL         │
  Fast, but may lie                 └────────────────────┘
                                      Slower, but truthful
```

## What is Testcontainers?

**Testcontainers** spins up real Docker containers for your tests. Your tests run against actual PostgreSQL, Redis, Kafka, etc.

```
┌───────────────────────────────────────────┐
│            TEST CONTAINER                  │
│                                            │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐ │
│  │PostgreSQL│  │  Redis   │  │  Kafka   │ │
│  │ port:5432│  │ port:6379│  │port:9092 │ │
│  └──────────┘  └──────────┘  └──────────┘ │
│         │            │            │        │
│         ▼            ▼            ▼        │
│       Your tests run against REAL infra   │
└───────────────────────────────────────────┘
```

## Setup

```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>testcontainers</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>1.19.0</version>
    <scope>test</scope>
</dependency>
```

## Basic Usage

```java
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        // Use postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword()
        DataSource dataSource = createDataSource(postgres);
        userRepository = new UserRepository(dataSource);
    }

    @Test
    void shouldSaveAndFindUser() {
        User user = new User("Alice", "alice@email.com");
        userRepository.save(user);

        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("Alice", found.get().getName());
    }
}
```

## Dynamic Properties with Spring Boot

```java
@Testcontainers
@SpringBootTest
class UserRepositorySpringTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldWorkWithRealDatabase() {
        userRepository.save(new User("Bob", "bob@email.com"));
        assertThat(userRepository.findByName("Bob")).isPresent();
    }
}
```

## Multiple Containers

```java
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7")
        .withExposedPorts(6379);

    @Container
    static KafkaContainer kafka = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.4.0")
    );

    @Test
    void shouldProcessOrder() {
        // All 3 services are real — postgres, redis, kafka
        // Test the full flow
    }
}
```

## Module-Specific Containers

| Container | Module | Usage |
|-----------|--------|-------|
| `PostgreSQLContainer` | `postgresql` | Relational DB tests |
| `MySQLContainer` | `mysql` | MySQL tests |
| `MongoDBContainer` | `mongodb` | Document DB tests |
| `KafkaContainer` | `kafka` | Message broker tests |
| `RedisContainer` | `redis` | Cache tests |
| `LocalStackContainer` | `localstack` | AWS service emulation |
| `GenericContainer` | `core` | Any Docker image |

## Testcontainers Patterns

### Singleton Container (Performance)

```java
@Testcontainers
abstract class AbstractIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16");

    static {
        postgres.start();
        System.setProperty("spring.datasource.url", postgres.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgres.getUsername());
        System.setProperty("spring.datasource.password", postgres.getPassword());
    }
}

class UserServiceTest extends AbstractIntegrationTest { /* ... */ }
class OrderServiceTest extends AbstractIntegrationTest { /* ... */ }
```

### Reusable Containers

```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
    .withReuse(true);  // Container stays running between test runs
```

## Integration Test Best Practices

| Practice | Why |
|----------|-----|
| Use `@Testcontainers` | Automatic container lifecycle management |
| Keep containers static | Shared across tests (don't restart per test) |
| Use fixed ports cautiously | Prefer random ports (avoid conflicts) |
| Test real database behavior | Constraints, transactions, locks |
| Clean data between tests | Use `@BeforeEach` truncation or rollbacks |
| Don't test what you mocked | If a mock works, still test with real infra |

## Exercises

1. Set up a PostgreSQL Testcontainer for an existing repository test.
2. Add a Redis container to test caching behavior.
3. Write an integration test that uses both PostgreSQL and Kafka containers.
4. Create an abstract base class for all integration tests in your project.
5. Use `@DynamicPropertySource` to configure Spring Boot for containerized tests.
