# Spring Core: DI, IoC, Bean Lifecycle

Spring's Inversion of Control (IoC) container manages object creation and wiring via Dependency Injection (DI). Constructor injection is preferred for required dependencies, and the `ApplicationContext` is the central interface to the container.

## @Configuration and @Bean

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class AppConfig {

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        ds.setUsername("appuser");
        ds.setPassword("secret");
        ds.setMaximumPoolSize(20);
        return ds;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public ServerConnector serverConnector() {
        return new ServerConnector(8080);
    }
}
```

## Stereotype Annotations

```java
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

// Generic bean — use for utility classes
@Component
public class EmailValidator {
    public boolean isValid(String email) {
        return email != null && email.contains("@");
    }
}

// Service — business logic layer
@Service
public class UserService {
    // ...
}

// Repository — data access layer (translates persistence exceptions)
@Repository
public class UserRepository {
    // ...
}

// Controller — MVC handler
@Controller
public class UserController {
    // ...
}

// REST Controller — @Controller + @ResponseBody
@RestController
public class UserApiController {
    // ...
}
```

## Dependency Injection

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

// CONSTRUCTOR INJECTION (preferred — makes dependencies explicit and enables immutability)
@Service
public class OrderService {

    private final InventoryService inventoryService;
    private final PaymentGateway paymentGateway;
    private final NotificationService notificationService;

    // No @Autowired needed for single constructor in Spring 4.3+
    public OrderService(InventoryService inventoryService,
                        PaymentGateway paymentGateway,
                        NotificationService notificationService) {
        this.inventoryService = inventoryService;
        this.paymentGateway = paymentGateway;
        this.notificationService = notificationService;
    }
}

// SETTER INJECTION (use for optional dependencies)
@Service
public class ReportService {

    private MetricsCollector metricsCollector;

    @Autowired(required = false)
    public void setMetricsCollector(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
}

// FIELD INJECTION (avoid — hard to test, breaks immutability)
@Service
public class PdfGenerationService {

    @Autowired
    private TemplateEngine templateEngine; // not recommended
}
```

## @Qualifier and @Primary

```java
import org.springframework.context.annotation.Primary;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

// Primary — default bean when multiple candidates exist
@Primary
@Service
public class StripePaymentGateway implements PaymentGateway {
    @Override
    public PaymentResponse charge(PaymentRequest request) {
        // ...
    }
}

@Service
public class PayPalPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResponse charge(PaymentRequest request) {
        // ...
    }
}

// Qualifier — select specific bean
@Service
public class CheckoutService {

    private final PaymentGateway paymentGateway;

    public CheckoutService(@Qualifier("payPalPaymentGateway") PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    // or with custom qualifier
    public void processRefund(@Qualifier("stripePaymentGateway") PaymentGateway gateway) {
        // ...
    }
}
```

**Custom @Qualifier annotation:**

```java
import org.springframework.beans.factory.annotation.Qualifier;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface StripeGateway {
}
```

## Bean Scopes

```java
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope("singleton") // Default — one instance per IoC container
public class CacheManager {
    // ...
}

@Component
@Scope("prototype") // New instance every time injected
public class WorkflowStep {
    // ...
}

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    // One instance per HTTP request
}

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserPreferences {
    // One instance per HTTP session
}

@Component
@Scope("application") // One instance per ServletContext
public class AppMetrics {
    // ...
}
```

## @Value Property Injection

```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class AppProperties {

    @Value("${app.name}")
    private String appName;

    @Value("${app.version:1.0.0}") // Default value
    private String appVersion;

    @Value("${app.servers:localhost:8080}") // Split into list
    private List<String> servers;

    @Value("#{systemProperties['user.home']}") // SpEL
    private String userHome;

    @Value("#{T(java.lang.Math).random() * 100}") // SpEL expression
    private double randomValue;

    @Value("classpath:data/seed.json") // Resource
    private org.springframework.core.io.Resource seedFile;

    public String getAppName() { return appName; }
}
```

```yaml
# application.yml
app:
  name: Enterprise App
  version: 2.1.0
  servers: app1.example.com,app2.example.com,app3.example.com
```

## @PropertySource and Environment

```java
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Configuration
@PropertySource("classpath:config/database.properties")
@PropertySource("file:${user.home}/.app/config.properties")
public class DatabaseConfig {

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(env.getProperty("db.url"));
        ds.setUsername(env.getProperty("db.username"));
        ds.setPassword(env.getProperty("db.password"));
        ds.setMaximumPoolSize(env.getProperty("db.pool.size", Integer.class, 10));
        return ds;
    }
}
```

```java
// Programmatic access
@Service
public class ConfigInspector {

    private final Environment env;

    public ConfigInspector(Environment env) {
        this.env = env;
    }

    public void printActiveProfiles() {
        System.out.println("Active profiles: " +
            Arrays.toString(env.getActiveProfiles()));
        System.out.println("Default profiles: " +
            Arrays.toString(env.getDefaultProfiles()));
        System.out.println("App name: " + env.getRequiredProperty("app.name"));
        System.out.println("Has 'database.url'? " +
            env.containsProperty("database.url"));
    }
}
```

## Bean Lifecycle

```java
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class LifecycleDemoBean implements InitializingBean, DisposableBean {

    public LifecycleDemoBean() {
        System.out.println("1. Constructor");
    }

    @PostConstruct
    public void init() {
        System.out.println("2. @PostConstruct — dependency injection done");
        // Initialize resources, start connections, etc.
    }

    @Override
    public void afterPropertiesSet() {
        System.out.println("3. InitializingBean.afterPropertiesSet — all properties set");
    }

    @Bean(initMethod = "customInit")
    // Custom init method (via @Bean annotation) runs after @PostConstruct

    public void businessMethod() {
        System.out.println("Bean is ready to use");
    }

    @PreDestroy
    public void shutdown() {
        System.out.println("4. @PreDestroy — container shutting down");
        // Release resources, close connections
    }

    @Override
    public void destroy() {
        System.out.println("5. DisposableBean.destroy — after @PreDestroy");
    }
}
```

**Lifecycle order:**

1. Constructor
2. Dependencies injected
3. `@PostConstruct`
4. `InitializingBean.afterPropertiesSet()`
5. Custom `init-method` (from `@Bean(initMethod=...)`)
6. Bean is ready for use
7. Container shuts down → `@PreDestroy`
8. `DisposableBean.destroy()`
9. Custom `destroy-method`

## ApplicationContext

```java
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class ApplicationContextDemo {

    public static void main(String[] args) {
        // Create context from @Configuration class
        try (AnnotationConfigApplicationContext ctx =
                 new AnnotationConfigApplicationContext(AppConfig.class)) {

            // Retrieve beans
            OrderService orderService = ctx.getBean(OrderService.class);
            orderService.placeOrder(new Order("SKU-001", 1, BigDecimal.TEN));

            // Get bean by name
            PaymentGateway gateway = (PaymentGateway) ctx.getBean("stripePaymentGateway");

            // Check bean existence
            boolean hasCache = ctx.containsBean("cacheManager");

            // Get all beans of a type
            Map<String, PaymentGateway> gateways = ctx.getBeansOfType(PaymentGateway.class);

            // Environment info
            Environment env = ctx.getEnvironment();
            System.out.println("Active profiles: " +
                Arrays.toString(env.getActiveProfiles()));

            // Publish events
            ctx.publishEvent(new OrderPlacedEvent(this, "ORDER-123"));
        }
    }
}

// Event handling
@Component
public class OrderEventListener {

    @EventListener
    public void handleOrderPlaced(OrderPlacedEvent event) {
        System.out.println("Order placed: " + event.getOrderId());
    }
}
```
