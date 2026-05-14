# Spring Boot: Auto-Configuration, Actuator, Profiles

Spring Boot auto-configures beans based on classpath dependencies, properties, and annotations. Actuator provides production-ready endpoints for monitoring. Profiles allow environment-specific configuration.

## @SpringBootApplication

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Combines: @Configuration + @EnableAutoConfiguration + @ComponentScan
@SpringBootApplication
public class EnterpriseApplication {

    public static void main(String[] args) {
        // Bootstrap the application
        SpringApplication app = new SpringApplication(EnterpriseApplication.class);

        // Configure before run
        app.setDefaultProperties(Map.of(
            "server.port", "8080",
            "spring.profiles.default", "dev"
        ));

        // Add listeners
        app.addListeners(new ApplicationStartingListener());

        // Run
        app.run(args);
    }
}
```

## application.yml with Profiles

```yaml
# application.yml — shared config
server:
  port: 8080

spring:
  application:
    name: enterprise-app
  profiles:
    active: dev          # overridden by SPRING_PROFILES_ACTIVE env var
    include:
      - common
      - security

---
# application-dev.yml — development profile
spring:
  config:
    activate:
      on-profile: dev

  datasource:
    url: jdbc:postgresql://localhost:5432/devdb
    username: dev_user
    password: dev_pass

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

logging:
  level:
    com.example: DEBUG
    org.hibernate.SQL: DEBUG

---
# application-prod.yml — production profile
spring:
  config:
    activate:
      on-profile: prod

  datasource:
    url: jdbc:postgresql://prod-db.example.com:5432/proddb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 40
      minimum-idle: 10

  jpa:
    show-sql: false
    hibernate:
      ddl-auto: validate

logging:
  level:
    com.example: INFO
    org.springframework: WARN

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus

---
# application-test.yml — test profile
spring:
  config:
    activate:
      on-profile: test

  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver

  jpa:
    hibernate:
      ddl-auto: create-drop
```

## @ConfigurationProperties (Type-Safe Properties)

```java
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import jakarta.validation.constraints.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
public class PropertiesConfig {}

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String name,
    String version,
    Duration sessionTimeout,
    @Min(1) @Max(100) int maxRetries,
    List<String> allowedOrigins,
    Map<String, String> customHeaders,
    Database database
) {
    public record Database(
        String url,
        @NotEmpty String username,
        String password,
        int poolSize
    ) {}
}
```

```yaml
# application.yml
app:
  name: Enterprise App
  version: 2.1.0
  session-timeout: 30m
  max-retries: 3
  allowed-origins:
    - https://app.example.com
    - https://admin.example.com
  custom-headers:
    X-API-Version: "2.1"
    X-Request-Id: "${random.uuid}"
  database:
    url: jdbc:postgresql://localhost:5432/mydb
    username: appuser
    password: "${DB_PASSWORD}"
    pool-size: 20
```

## Conditional Annotations

```java
import org.springframework.boot.autoconfigure.condition.*;
import org.springframework.context.annotation.*;

@Configuration
public class ConditionalConfig {

    // Bean only created when property is set to a specific value
    @Bean
    @ConditionalOnProperty(
        name = "app.caching.enabled",
        havingValue = "true",
        matchIfMissing = false)
    public CacheManager cacheManager() {
        return new CaffeineCacheManager();
    }

    // Bean only created when a class is on the classpath
    @Bean
    @ConditionalOnClass(name = "org.springframework.kafka.core.KafkaTemplate")
    public KafkaConfig kafkaConfig() {
        return new KafkaConfig();
    }

    // Bean created when a bean is missing
    @Bean
    @ConditionalOnMissingBean(NotificationService.class)
    public NotificationService defaultNotificationService() {
        return new LoggingNotificationService();
    }

    // Bean only created on specific profile
    @Bean
    @Profile("!prod")
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .build();
    }

    // Bean only created in specific environment
    @Bean
    @ConditionalOnExpression("${app.feature.new-checkout:false}")
    public CheckoutService newCheckoutService() {
        return new NewCheckoutService();
    }

    // Bean only created on single instance (not cloud)
    @Bean
    @ConditionalOnCloudPlatform(CloudPlatform.NONE)
    public LocalFileStorageService fileStorage() {
        return new LocalFileStorageService();
    }
}
```

## Spring Boot DevTools

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

```yaml
spring:
  devtools:
    restart:
      enabled: true
      additional-paths: src/main/java
      exclude: static/**,public/**,templates/**
    livereload:
      enabled: true
      port: 35729
    remote:
      secret: my-secret
```

## Spring Boot Actuator

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      base-path: /actuator
      exposure:
        include: health,info,metrics,env,loggers,threaddump,heapdump,prometheus
      exclude: shutdown
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
      probes:
        enabled: true  # Kubernetes readiness/liveness probes
    env:
      show-values: when-authorized
    configprops:
      show-values: when-authorized
    shutdown:
      enabled: false
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
  info:
    env:
      enabled: true
    build:
      enabled: true
    git:
      enabled: true
      mode: full
    java:
      enabled: true
    os:
      enabled: true
```

```yaml
# Custom /info endpoint values
info:
  app:
    name: '@project.name@'     # Maven/Gradle filter
    version: '@project.version@'
    java-version: '@java.version@'
  contact:
    team: Platform Engineering
    email: platform@example.com
```

## Custom Health Indicator

```java
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {

            if (rs.next() && rs.getInt(1) == 1) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("url", conn.getMetaData().getURL())
                    .build();
            }
            return Health.down()
                .withDetail("reason", "Query returned unexpected result")
                .build();

        } catch (Exception e) {
            return Health.down(e)
                .withDetail("reason", e.getMessage())
                .build();
        }
    }
}

// Composite: individual checks grouped
@Component
public class ExternalServicesHealthIndicator implements HealthIndicator {

    @Autowired
    private List<ExternalService> services;

    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        for (ExternalService service : services) {
            try {
                service.check();
                builder.withDetail(service.getName(), "UP");
            } catch (Exception e) {
                builder.down();
                builder.withDetail(service.getName(), "DOWN: " + e.getMessage());
            }
        }
        return builder.build();
    }
}
```

```json
// GET /actuator/health
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "PostgreSQL",
        "url": "jdbc:postgresql://..."
      }
    },
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 500000000000,
        "free": 250000000000,
        "threshold": 10485760
      }
    },
    "ping": { "status": "UP" }
  }
}
```

## Banner Customization

```java
// src/main/resources/banner.txt

  ______                _                      ___
 |  ____|              | |                    |__ \
 | |__   _ __ ___  _ __| |_ ___ _ __   ___      ) |
 |  __| | '__/ _ \| '__| __/ _ \ '_ \ / _ \    / /
 | |____| | | (_) | |  | ||  __/ | | | (_) |  |_|
 |______|_|  \___/|_|   \__\___|_| |_|\___/    (_)

 :: Enterprise Application :: v${application.version}
```

```yaml
spring:
  banner:
    location: classpath:banner.txt
    charset: UTF-8
# Or disable: spring.main.banner-mode=off
```

## Auto-Configuration Explanation

Auto-configuration in Spring Boot works via `@EnableAutoConfiguration`, which scans `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` files. These list auto-configuration classes, each annotated with `@Conditional*` annotations.

```java
// Example: what happens when you add spring-boot-starter-web
// Spring Boot's auto-configuration detects:
// 1. javax.servlet.Servlet on classpath → configures DispatcherServlet
// 2. Tomcat on classpath → configures EmbeddedTomcat
// 3. Jackson on classpath → configures HttpMessageConverters
// All gated by @ConditionalOnClass and @ConditionalOnMissingBean

// To see what auto-configuration is applied:
// Add: debug=true to application.yml
// Or: --debug flag when running

// To exclude specific auto-configuration:
@SpringBootApplication(exclude = {
    DataSourceAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
public class CustomApplication {
    // ...
}

// Or via properties:
// spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
```
