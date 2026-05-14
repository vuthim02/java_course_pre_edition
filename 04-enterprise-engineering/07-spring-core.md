# Enterprise Engineering — Lesson 7: Spring Core (DI & IoC)

## What is Spring?

**Spring** is a framework that manages your objects (beans) and their dependencies. The core concept is **Inversion of Control (IoC)** — instead of YOU creating objects, Spring creates them and wires them together.

```
Without Spring (Tight Coupling):    With Spring (Loose Coupling):
┌──────────────────────────────┐   ┌──────────────────────────────┐
│ class EmailService {         │   │ class EmailService {         │
│     private SmtpServer srv;  │   │     private SmtpServer srv;  │
│     public EmailService() {  │   │                              │
│         srv = new SmtpServer(│   │     @Autowired                │
│             "smtp.gmail.com");│  │     public EmailService(      │
│     }                         │   │         SmtpServer srv) {   │
│ }                              │   │         this.srv = srv;     │
│                                │   │     }                       │
│ Problem: EmailService is       │   │ }                            │
│ TIGHTLY coupled to SmtpServer  │   │                              │
│ Can't easily change or test!  │   │ Spring creates SmtpServer    │
└──────────────────────────────┘   │ and INJECTS it (hence DI)     │
                                    └──────────────────────────────┘
```

## Core Concepts

### Inversion of Control (IoC) Container

```
┌─────────────────────────────────────────────────────┐
│              SPRING IoC CONTAINER                     │
│                                                       │
│  Reads configuration → Creates beans → Wires deps   │
│                                                       │
│  ┌─────────────────────────────────────────────────┐ │
│  │ Bean Factory                                    │ │
│  │   [UserService] ──▶ [UserRepository]            │ │
│  │   [OrderService] ──▶ [PaymentGateway]           │ │
│  │   [EmailService] ──▶ [SmtpServer, TemplateEngine]│ │
│  └─────────────────────────────────────────────────┘ │
│                                                       │
│  Beans are: Created, Wired, Managed (lifecycle)      │
└─────────────────────────────────────────────────────┘
```

### Dependency Injection (DI)

Three ways to inject dependencies:

```java
// 1. Constructor Injection (PREFERRED)
@Service
public class UserService {
    private final UserRepository repository;

    public UserService(UserRepository repository) {  // Spring auto-injects
        this.repository = repository;
    }
}

// 2. Setter Injection
@Service
public class UserService {
    private UserRepository repository;

    @Autowired
    public void setRepository(UserRepository repository) {
        this.repository = repository;
    }
}

// 3. Field Injection (Avoid — hard to test)
@Service
public class UserService {
    @Autowired
    private UserRepository repository;
}
```

**Constructor injection is preferred because:**
- Immutable fields (final)
- Clear dependencies (required)
- Easy to test (just pass mocks)
- No reflection needed

## Creating Beans

### @Component — Generic Steretype

```java
@Component
public class EmailValidator {
    public boolean isValid(String email) {
        return email != null && email.contains("@");
    }
}
```

### Specialized Stereotypes

```java
@Repository          // Data access layer (Database)
public class UserRepository { ... }

@Service             // Business logic layer
public class UserService { ... }

@Controller          // Web layer (MVC)
public class UserController { ... }

@RestController      // REST API (web layer)
public class UserApiController { ... }
```

### @Bean — Explicit Bean Declaration

```java
@Configuration
public class AppConfig {

    @Bean
    public SmtpServer smtpServer() {
        return new SmtpServer("smtp.gmail.com", 587);
    }

    @Bean
    public EmailService emailService(SmtpServer smtpServer) {
        return new EmailService(smtpServer);
    }
}
```

## Bean Scopes

```java
@Component
@Scope("singleton")    // Default — ONE instance per container
public class AppCache { ... }

@Component
@Scope("prototype")    // NEW instance every time requested
public class ShoppingCart { ... }

@Component
@Scope("request")      // ONE instance per HTTP request (web only)
public class RequestContext { ... }

@Component
@Scope("session")      // ONE instance per HTTP session (web only)
public class UserSession { ... }
```

## Bean Lifecycle

```
Instantiate → Populate Properties → BeanNameAware → BeanFactoryAware
→ @PostConstruct / InitializingBean → READY TO USE
→ @PreDestroy / DisposableBean → Destroy
```

```java
@Component
public class DatabaseConnection {
    @PostConstruct
    public void init() {
        System.out.println("Connecting to database...");
    }

    @PreDestroy
    public void cleanup() {
        System.out.println("Closing database connection...");
    }
}
```

## Profiles

Different configurations for different environments:

```java
@Configuration
@Profile("dev")
public class DevConfig {
    @Bean
    public DataSource dataSource() {
        return new H2DataSource();  // In-memory for dev
    }
}

@Configuration
@Profile("prod")
public class ProdConfig {
    @Bean
    public DataSource dataSource() {
        return new PostgresDataSource();  // Real DB for prod
    }
}

// Activate: --spring.profiles.active=dev
```

## Property Injection

```java
@Component
public class AppProperties {
    @Value("${app.name}")
    private String appName;

    @Value("${app.version:1.0.0}")  // Default value
    private String appVersion;

    @Value("${database.url}")
    private String databaseUrl;
}

// application.properties:
app.name=MyApplication
database.url=jdbc:postgresql://localhost:5432/mydb
```

---

### Exercises

1. Create a Spring Boot app with `@Service`, `@Repository`, and `@RestController`. Wire them with constructor injection.
2. Create a `@Configuration` class with a `@Bean` that creates a custom object.
3. Use `@Value` to inject configuration properties.
4. Create a `@Component` with `@PostConstruct` and `@PreDestroy`. Observe the lifecycle.
5. Create `dev` and `prod` profiles with different bean configurations.
