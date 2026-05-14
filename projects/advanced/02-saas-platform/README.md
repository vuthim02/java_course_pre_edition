# Multi-Tenant SaaS Platform

Complete multi-tenant SaaS platform with schema-per-tenant isolation, feature flags, billing, rate limiting, and role-based access.

## Architecture

```
                     ┌──────────────┐
                     │  Admin API   │
                     │  (port 9090) │
                     └──────┬───────┘
                            │
                    ┌───────▼────────┐
                    │  Tenant API    │
                    │  (port 8080)   │
                    └───────┬────────┘
                            │
              ┌─────────────┼─────────────┐
              │             │             │
        ┌─────▼─────┐ ┌─────▼─────┐ ┌─────▼─────┐
        │  Tenant 1 │ │  Tenant 2 │ │  Tenant 3 │
        │  Schema   │ │  Schema   │ │  Schema   │
        └───────────┘ └───────────┘ └───────────┘
              │             │             │
              └─────────────┼─────────────┘
                      ┌─────▼─────┐
                      │  Shared   │
                      │  Database │
                      └───────────┘
```

## Project Structure

```
saas-platform/
├── pom.xml
├── src/main/java/com/saas/
│   ├── SaasApplication.java
│   ├── config/
│   │   ├── TenantContext.java
│   │   ├── TenantInterceptor.java
│   │   ├── MultiTenantDataSource.java
│   │   ├── TenantSchemaManager.java
│   │   ├── SecurityConfig.java
│   │   ├── RateLimitingConfig.java
│   │   └── WebConfig.java
│   ├── tenant/
│   │   ├── entity/Tenant.java
│   │   ├── repository/TenantRepository.java
│   │   ├── service/TenantService.java
│   │   ├── controller/TenantController.java
│   │   └── dto/TenantDTO.java
│   ├── user/
│   │   ├── entity/User.java
│   │   ├── repository/UserRepository.java
│   │   ├── service/UserService.java
│   │   ├── controller/UserController.java
│   │   └── dto/UserDTO.java
│   ├── subscription/
│   │   ├── entity/SubscriptionPlan.java
│   │   ├── entity/Subscription.java
│   │   ├── repository/SubscriptionRepository.java
│   │   ├── service/SubscriptionService.java
│   │   ├── controller/SubscriptionController.java
│   │   └── dto/SubscriptionDTO.java
│   ├── billing/
│   │   ├── entity/Invoice.java
│   │   ├── entity/PaymentMethod.java
│   │   ├── repository/InvoiceRepository.java
│   │   ├── service/BillingService.java
│   │   └── controller/BillingController.java
│   ├── featureflag/
│   │   ├── entity/FeatureFlag.java
│   │   ├── repository/FeatureFlagRepository.java
│   │   ├── service/FeatureFlagService.java
│   │   └── controller/FeatureFlagController.java
│   └── admin/
│       ├── controller/AdminController.java
│       └── dto/AdminDashboardDTO.java
└── src/main/resources/
    ├── application.yml
    └── schema-template.sql
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>
    <groupId>com.saas</groupId>
    <artifactId>saas-platform</artifactId>
    <version>1.0.0</version>
    <name>saas-platform</name>
    <properties>
        <java.version>17</java.version>
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
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
            <version>0.12.3</version>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <version>0.12.3</version>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml

```yaml
server:
  port: 8080
spring:
  application:
    name: saas-platform
  datasource:
    url: jdbc:postgresql://localhost:5432/saas_platform
    username: postgres
    password: postgres
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  jackson:
    serialization:
      write-dates-as-timestamps: false
    date-format: yyyy-MM-dd'T'HH:mm:ss

saas:
  admin:
    api-key: ${ADMIN_API_KEY:admin-secret-key-change-in-production}
  rate-limit:
    default-limit: 100
    default-window-seconds: 60
  jwt:
    secret: ${JWT_SECRET:saas-jwt-secret-key-change-in-production}
    expiration: 86400000
```

## SaasApplication.java

```java
package com.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(SaasApplication.class, args);
    }
}
```

---

## Multi-Tenancy Core

### TenantContext.java

```java
package com.saas.config;

public class TenantContext {

    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getCurrentTenant() {
        return CURRENT_TENANT.get();
    }

    public static void setCurrentUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static Long getCurrentUserId() {
        return CURRENT_USER_ID.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER_ID.remove();
    }
}
```

### TenantInterceptor.java

```java
package com.saas.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = request.getHeader("X-Tenant-Id");
        String userId = request.getHeader("X-User-Id");

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
            log.debug("Set tenant context: {}", tenantId);
        }
        if (userId != null) {
            try {
                TenantContext.setCurrentUserId(Long.parseLong(userId));
            } catch (NumberFormatException e) {
                log.warn("Invalid X-User-Id header: {}", userId);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        TenantContext.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        TenantContext.clear();
    }
}
```

### MultiTenantDataSource.java

```java
package com.saas.config;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class MultiTenantDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            return "default";
        }
        return tenantId;
    }
}
```

### TenantSchemaManager.java

```java
package com.saas.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class TenantSchemaManager {

    private static final Logger log = LoggerFactory.getLogger(TenantSchemaManager.class);
    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaManager(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void createSchemaForTenant(String tenantId) {
        String schemaName = "tenant_" + tenantId;
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            jdbcTemplate.execute("SET search_path TO " + schemaName);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS """ + schemaName + """.users (
                    id BIGSERIAL PRIMARY KEY,
                    username VARCHAR(100) NOT NULL UNIQUE,
                    email VARCHAR(255) NOT NULL UNIQUE,
                    password VARCHAR(255) NOT NULL,
                    full_name VARCHAR(255),
                    role VARCHAR(50) DEFAULT 'USER',
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS """ + schemaName + """.feature_flags (
                    id BIGSERIAL PRIMARY KEY,
                    flag_key VARCHAR(100) NOT NULL UNIQUE,
                    flag_name VARCHAR(255) NOT NULL,
                    enabled BOOLEAN DEFAULT false,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS """ + schemaName + """.usage_metrics (
                    id BIGSERIAL PRIMARY KEY,
                    metric_key VARCHAR(100) NOT NULL,
                    metric_value BIGINT NOT NULL DEFAULT 0,
                    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS """ + schemaName + """.subscriptions (
                    id BIGSERIAL PRIMARY KEY,
                    plan_name VARCHAR(100) NOT NULL,
                    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
                    start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    end_date TIMESTAMP,
                    auto_renew BOOLEAN DEFAULT true,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS """ + schemaName + """.invoices (
                    id BIGSERIAL PRIMARY KEY,
                    invoice_number VARCHAR(50) NOT NULL UNIQUE,
                    amount DECIMAL(10,2) NOT NULL,
                    currency VARCHAR(3) DEFAULT 'USD',
                    status VARCHAR(50) DEFAULT 'PENDING',
                    due_date TIMESTAMP,
                    paid_at TIMESTAMP,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )""");

            log.info("Created schema for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("Failed to create schema for tenant: {}", tenantId, e);
            throw new RuntimeException("Failed to initialize tenant schema", e);
        }
    }

    public void dropSchemaForTenant(String tenantId) {
        String schemaName = "tenant_" + tenantId;
        try {
            jdbcTemplate.execute("DROP SCHEMA IF EXISTS " + schemaName + " CASCADE");
            log.info("Dropped schema for tenant: {}", tenantId);
        } catch (Exception e) {
            log.error("Failed to drop schema for tenant: {}", tenantId, e);
        }
    }
}
```

### WebConfig.java

```java
package com.saas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final TenantInterceptor tenantInterceptor;

    public WebConfig(TenantInterceptor tenantInterceptor) {
        this.tenantInterceptor = tenantInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(tenantInterceptor);
    }
}
```

### DataSourceConfig.java

```java
package com.saas.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource defaultDataSource() {
        return DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url("jdbc:postgresql://localhost:5432/saas_platform")
            .username("postgres")
            .password("postgres")
            .driverClassName("org.postgresql.Driver")
            .build();
    }

    @Bean
    @Primary
    public DataSource multiTenantDataSource() {
        Map<Object, Object> targetDataSources = new HashMap<>();
        DataSource defaultDs = defaultDataSource();
        targetDataSources.put("default", defaultDs);

        MultiTenantDataSource routingDataSource = new MultiTenantDataSource();
        routingDataSource.setDefaultTargetDataSource(defaultDs);
        routingDataSource.setTargetDataSources(targetDataSources);
        return routingDataSource;
    }
}
```

### RateLimitingConfig.java

```java
package com.saas.config;

import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitingConfig {

    private final Map<String, AtomicInteger> tenantRequestCounts = new ConcurrentHashMap<>();
    private final int defaultLimit = 100;
    private final long windowMillis = 60000;
    private long windowStart = System.currentTimeMillis();

    public boolean allowRequest(String tenantId) {
        long now = System.currentTimeMillis();
        if (now - windowStart > windowMillis) {
            tenantRequestCounts.clear();
            windowStart = now;
        }
        AtomicInteger count = tenantRequestCounts.computeIfAbsent(tenantId, k -> new AtomicInteger(0));
        return count.incrementAndGet() <= defaultLimit;
    }

    public int getRemainingRequests(String tenantId) {
        AtomicInteger count = tenantRequestCounts.get(tenantId);
        if (count == null) return defaultLimit;
        return Math.max(0, defaultLimit - count.get());
    }

    public void setTenantLimit(String tenantId, int limit) {
    }
}
```

### SecurityConfig.java

```java
package com.saas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

## Tenant Management

### Tenant.java
```java
package com.saas.tenant.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tenants")
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tenantId;

    @Column(nullable = false)
    private String name;

    @Column
    private String domain;

    @Column
    private String contactEmail;

    @Column
    private String plan;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "max_users")
    private int maxUsers = 10;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### TenantRepository.java
```java
package com.saas.tenant.repository;

import com.saas.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {
    Optional<Tenant> findByTenantId(String tenantId);
    boolean existsByTenantId(String tenantId);
    boolean existsByDomain(String domain);
}
```

### TenantService.java
```java
package com.saas.tenant.service;

import com.saas.config.TenantSchemaManager;
import com.saas.tenant.dto.TenantDTO;
import com.saas.tenant.entity.Tenant;
import com.saas.tenant.repository.TenantRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantService {

    private static final Logger log = LoggerFactory.getLogger(TenantService.class);
    private final TenantRepository tenantRepository;
    private final TenantSchemaManager schemaManager;

    public TenantService(TenantRepository tenantRepository, TenantSchemaManager schemaManager) {
        this.tenantRepository = tenantRepository;
        this.schemaManager = schemaManager;
    }

    @Transactional
    public Tenant createTenant(TenantDTO dto) {
        if (tenantRepository.existsByTenantId(dto.getTenantId())) {
            throw new RuntimeException("Tenant ID already exists: " + dto.getTenantId());
        }
        Tenant tenant = new Tenant();
        tenant.setTenantId(dto.getTenantId());
        tenant.setName(dto.getName());
        tenant.setDomain(dto.getDomain());
        tenant.setContactEmail(dto.getContactEmail());
        tenant.setPlan(dto.getPlan() != null ? dto.getPlan() : "FREE");
        tenant.setActive(true);
        tenant.setMaxUsers(dto.getMaxUsers() > 0 ? dto.getMaxUsers() : 10);

        Tenant saved = tenantRepository.save(tenant);
        schemaManager.createSchemaForTenant(dto.getTenantId());
        log.info("Created tenant: {}", dto.getTenantId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Tenant getTenantByTenantId(String tenantId) {
        return tenantRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("Tenant not found: " + tenantId));
    }

    @Transactional(readOnly = true)
    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    @Transactional
    public Tenant updateTenant(String tenantId, TenantDTO dto) {
        Tenant tenant = getTenantByTenantId(tenantId);
        tenant.setName(dto.getName());
        tenant.setDomain(dto.getDomain());
        tenant.setContactEmail(dto.getContactEmail());
        if (dto.getPlan() != null) tenant.setPlan(dto.getPlan());
        if (dto.getMaxUsers() > 0) tenant.setMaxUsers(dto.getMaxUsers());
        tenant.setActive(dto.isActive());
        return tenantRepository.save(tenant);
    }

    @Transactional
    public void deactivateTenant(String tenantId) {
        Tenant tenant = getTenantByTenantId(tenantId);
        tenant.setActive(false);
        tenantRepository.save(tenant);
        log.info("Deactivated tenant: {}", tenantId);
    }

    @Transactional
    public void deleteTenant(String tenantId) {
        Tenant tenant = getTenantByTenantId(tenantId);
        schemaManager.dropSchemaForTenant(tenantId);
        tenantRepository.delete(tenant);
        log.info("Deleted tenant: {}", tenantId);
    }
}
```

### TenantDTO.java
```java
package com.saas.tenant.dto;

public class TenantDTO {
    private String tenantId;
    private String name;
    private String domain;
    private String contactEmail;
    private String plan;
    private int maxUsers;
    private boolean active;

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

### TenantController.java
```java
package com.saas.tenant.controller;

import com.saas.tenant.dto.TenantDTO;
import com.saas.tenant.entity.Tenant;
import com.saas.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<Tenant> createTenant(@RequestBody TenantDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.createTenant(dto));
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<Tenant> getTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(tenantService.getTenantByTenantId(tenantId));
    }

    @GetMapping
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @PutMapping("/{tenantId}")
    public ResponseEntity<Tenant> updateTenant(@PathVariable String tenantId, @RequestBody TenantDTO dto) {
        return ResponseEntity.ok(tenantService.updateTenant(tenantId, dto));
    }

    @DeleteMapping("/{tenantId}")
    public ResponseEntity<Void> deleteTenant(@PathVariable String tenantId) {
        tenantService.deleteTenant(tenantId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{tenantId}/deactivate")
    public ResponseEntity<Void> deactivateTenant(@PathVariable String tenantId) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## User Management

### User.java
```java
package com.saas.user.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column
    private String role;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### UserRepository.java
```java
package com.saas.user.repository;

import com.saas.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameAndTenantId(String username, String tenantId);
    Optional<User> findByEmailAndTenantId(String email, String tenantId);
    List<User> findByTenantId(String tenantId);
    boolean existsByUsernameAndTenantId(String username, String tenantId);
    boolean existsByEmailAndTenantId(String email, String tenantId);
    long countByTenantId(String tenantId);
}
```

### UserService.java
```java
package com.saas.user.service;

import com.saas.config.TenantContext;
import com.saas.user.dto.UserDTO;
import com.saas.user.entity.User;
import com.saas.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User createUser(UserDTO dto) {
        String tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new RuntimeException("Tenant context not set");
        }
        if (userRepository.existsByUsernameAndTenantId(dto.getUsername(), tenantId)) {
            throw new RuntimeException("Username already exists in this tenant");
        }
        if (userRepository.existsByEmailAndTenantId(dto.getEmail(), tenantId)) {
            throw new RuntimeException("Email already exists in this tenant");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setTenantId(tenantId);
        user.setRole(dto.getRole() != null ? dto.getRole() : "USER");
        user.setActive(true);
        User saved = userRepository.save(user);
        log.info("Created user {} in tenant {}", saved.getUsername(), tenantId);
        return saved;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByTenant(String tenantId) {
        return userRepository.findByTenantId(tenantId);
    }

    @Transactional
    public User updateUser(Long id, UserDTO dto) {
        User user = getUserById(id);
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRole(dto.getRole());
        user.setActive(dto.isActive());
        return userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);
    }
}
```

### UserDTO.java
```java
package com.saas.user.dto;

public class UserDTO {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;
    private boolean active;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
```

### UserController.java
```java
package com.saas.user.controller;

import com.saas.user.dto.UserDTO;
import com.saas.user.entity.User;
import com.saas.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody UserDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<User>> getUsersByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody UserDTO dto) {
        return ResponseEntity.ok(userService.updateUser(id, dto));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Feature Flags

### FeatureFlag.java
```java
package com.saas.featureflag.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "feature_flags")
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "flag_key", nullable = false, unique = true)
    private String flagKey;

    @Column(name = "flag_name", nullable = false)
    private String flagName;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean enabled = false;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getFlagKey() { return flagKey; }
    public void setFlagKey(String flagKey) { this.flagKey = flagKey; }
    public String getFlagName() { return flagName; }
    public void setFlagName(String flagName) { this.flagName = flagName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### FeatureFlagRepository.java
```java
package com.saas.featureflag.repository;

import com.saas.featureflag.entity.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, Long> {
    Optional<FeatureFlag> findByFlagKeyAndTenantId(String flagKey, String tenantId);
    List<FeatureFlag> findByTenantId(String tenantId);
    List<FeatureFlag> findByTenantIdAndEnabled(String tenantId, boolean enabled);
}
```

### FeatureFlagService.java
```java
package com.saas.featureflag.service;

import com.saas.featureflag.entity.FeatureFlag;
import com.saas.featureflag.repository.FeatureFlagRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FeatureFlagService {

    private final FeatureFlagRepository featureFlagRepository;

    public FeatureFlagService(FeatureFlagRepository featureFlagRepository) {
        this.featureFlagRepository = featureFlagRepository;
    }

    @Transactional
    public FeatureFlag createFlag(FeatureFlag flag) {
        return featureFlagRepository.save(flag);
    }

    @Transactional(readOnly = true)
    public List<FeatureFlag> getFlagsByTenant(String tenantId) {
        return featureFlagRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public boolean isFeatureEnabled(String flagKey, String tenantId) {
        return featureFlagRepository.findByFlagKeyAndTenantId(flagKey, tenantId)
            .map(FeatureFlag::isEnabled)
            .orElse(false);
    }

    @Transactional
    public FeatureFlag toggleFlag(Long id, boolean enabled) {
        FeatureFlag flag = featureFlagRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Feature flag not found: " + id));
        flag.setEnabled(enabled);
        return featureFlagRepository.save(flag);
    }

    @Transactional
    public void deleteFlag(Long id) {
        featureFlagRepository.deleteById(id);
    }
}
```

### FeatureFlagController.java
```java
package com.saas.featureflag.controller;

import com.saas.featureflag.entity.FeatureFlag;
import com.saas.featureflag.service.FeatureFlagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feature-flags")
public class FeatureFlagController {

    private final FeatureFlagService featureFlagService;

    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }

    @PostMapping
    public ResponseEntity<FeatureFlag> createFlag(@RequestBody FeatureFlag flag) {
        return ResponseEntity.status(HttpStatus.CREATED).body(featureFlagService.createFlag(flag));
    }

    @GetMapping("/tenant/{tenantId}")
    public ResponseEntity<List<FeatureFlag>> getFlagsByTenant(@PathVariable String tenantId) {
        return ResponseEntity.ok(featureFlagService.getFlagsByTenant(tenantId));
    }

    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFlag(
            @RequestParam String flagKey, @RequestParam String tenantId) {
        return ResponseEntity.ok(featureFlagService.isFeatureEnabled(flagKey, tenantId));
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<FeatureFlag> toggleFlag(
            @PathVariable Long id, @RequestParam boolean enabled) {
        return ResponseEntity.ok(featureFlagService.toggleFlag(id, enabled));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFlag(@PathVariable Long id) {
        featureFlagService.deleteFlag(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Subscription / Billing

### SubscriptionPlan.java
```java
package com.saas.subscription.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column
    private String currency = "USD";

    @Column(name = "billing_cycle")
    private String billingCycle = "MONTHLY";

    @Column(name = "max_users")
    private int maxUsers;

    @Column(name = "max_storage_gb")
    private int maxStorageGb;

    @Column(name = "features")
    private String features;

    @Column(name = "is_active")
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getBillingCycle() { return billingCycle; }
    public void setBillingCycle(String billingCycle) { this.billingCycle = billingCycle; }
    public int getMaxUsers() { return maxUsers; }
    public void setMaxUsers(int maxUsers) { this.maxUsers = maxUsers; }
    public int getMaxStorageGb() { return maxStorageGb; }
    public void setMaxStorageGb(int maxStorageGb) { this.maxStorageGb = maxStorageGb; }
    public String getFeatures() { return features; }
    public void setFeatures(String features) { this.features = features; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### Subscription.java
```java
package com.saas.subscription.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "plan_name", nullable = false)
    private String planName;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "auto_renew")
    private boolean autoRenew = true;

    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (startDate == null) startDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getPlanName() { return planName; }
    public void setPlanName(String planName) { this.planName = planName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public boolean isAutoRenew() { return autoRenew; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
    public LocalDateTime getTrialEnd() { return trialEnd; }
    public void setTrialEnd(LocalDateTime trialEnd) { this.trialEnd = trialEnd; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

### SubscriptionRepository.java
```java
package com.saas.subscription.repository;

import com.saas.subscription.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByTenantId(String tenantId);
}
```

### SubscriptionService.java
```java
package com.saas.subscription.service;

import com.saas.subscription.entity.Subscription;
import com.saas.subscription.repository.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionService.class);
    private final SubscriptionRepository subscriptionRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    @Transactional
    public Subscription createSubscription(String tenantId, String planName) {
        Subscription sub = new Subscription();
        sub.setTenantId(tenantId);
        sub.setPlanName(planName);
        sub.setStatus("ACTIVE");
        sub.setStartDate(LocalDateTime.now());
        sub.setAutoRenew(true);
        Subscription saved = subscriptionRepository.save(sub);
        log.info("Created subscription for tenant {}: {}", tenantId, planName);
        return saved;
    }

    @Transactional(readOnly = true)
    public Subscription getSubscriptionByTenant(String tenantId) {
        return subscriptionRepository.findByTenantId(tenantId)
            .orElseThrow(() -> new RuntimeException("No subscription found for tenant: " + tenantId));
    }

    @Transactional
    public Subscription upgradePlan(String tenantId, String newPlan) {
        Subscription sub = getSubscriptionByTenant(tenantId);
        sub.setPlanName(newPlan);
        log.info("Upgraded tenant {} to plan {}", tenantId, newPlan);
        return subscriptionRepository.save(sub);
    }

    @Transactional
    public void cancelSubscription(String tenantId) {
        Subscription sub = getSubscriptionByTenant(tenantId);
        sub.setStatus("CANCELLED");
        sub.setCancelledAt(LocalDateTime.now());
        sub.setAutoRenew(false);
        subscriptionRepository.save(sub);
        log.info("Cancelled subscription for tenant {}", tenantId);
    }
}
```

### SubscriptionController.java
```java
package com.saas.subscription.controller;

import com.saas.subscription.entity.Subscription;
import com.saas.subscription.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(
            @RequestParam String tenantId, @RequestParam String planName) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(subscriptionService.createSubscription(tenantId, planName));
    }

    @GetMapping("/{tenantId}")
    public ResponseEntity<Subscription> getSubscription(@PathVariable String tenantId) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionByTenant(tenantId));
    }

    @PutMapping("/{tenantId}/upgrade")
    public ResponseEntity<Subscription> upgradePlan(
            @PathVariable String tenantId, @RequestParam String planName) {
        return ResponseEntity.ok(subscriptionService.upgradePlan(tenantId, planName));
    }

    @PostMapping("/{tenantId}/cancel")
    public ResponseEntity<Void> cancelSubscription(@PathVariable String tenantId) {
        subscriptionService.cancelSubscription(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

### Invoice.java
```java
package com.saas.billing.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id", nullable = false)
    private String tenantId;

    @Column(name = "invoice_number", nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column
    private String currency = "USD";

    @Column
    private String description;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
```

### InvoiceRepository.java
```java
package com.saas.billing.repository;

import com.saas.billing.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findByTenantId(String tenantId);
    List<Invoice> findByTenantIdAndStatus(String tenantId, String status);
}
```

### BillingService.java
```java
package com.saas.billing.service;

import com.saas.billing.entity.Invoice;
import com.saas.billing.repository.InvoiceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class BillingService {

    private static final Logger log = LoggerFactory.getLogger(BillingService.class);
    private final InvoiceRepository invoiceRepository;

    public BillingService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public Invoice generateInvoice(String tenantId, BigDecimal amount, String description) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setAmount(amount);
        invoice.setDescription(description);
        invoice.setStatus("PENDING");
        invoice.setDueDate(LocalDateTime.now().plusDays(30));
        Invoice saved = invoiceRepository.save(invoice);
        log.info("Generated invoice {} for tenant {}", saved.getInvoiceNumber(), tenantId);
        return saved;
    }

    @Transactional
    public void markPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new RuntimeException("Invoice not found: " + invoiceId));
        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);
        log.info("Invoice {} marked as paid", invoice.getInvoiceNumber());
    }

    @Transactional(readOnly = true)
    public List<Invoice> getInvoicesByTenant(String tenantId) {
        return invoiceRepository.findByTenantId(tenantId);
    }

    @Transactional(readOnly = true)
    public List<Invoice> getPendingInvoices(String tenantId) {
        return invoiceRepository.findByTenantIdAndStatus(tenantId, "PENDING");
    }
}
```

### BillingController.java
```java
package com.saas.billing.controller;

import com.saas.billing.entity.Invoice;
import com.saas.billing.service.BillingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @PostMapping("/invoices")
    public ResponseEntity<Invoice> generateInvoice(
            @RequestParam String tenantId,
            @RequestParam BigDecimal amount,
            @RequestParam String description) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(billingService.generateInvoice(tenantId, amount, description));
    }

    @GetMapping("/invoices/{tenantId}")
    public ResponseEntity<List<Invoice>> getInvoices(@PathVariable String tenantId) {
        return ResponseEntity.ok(billingService.getInvoicesByTenant(tenantId));
    }

    @GetMapping("/invoices/{tenantId}/pending")
    public ResponseEntity<List<Invoice>> getPendingInvoices(@PathVariable String tenantId) {
        return ResponseEntity.ok(billingService.getPendingInvoices(tenantId));
    }

    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<Void> markPaid(@PathVariable Long id) {
        billingService.markPaid(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## Admin API

### AdminController.java
```java
package com.saas.admin.controller;

import com.saas.tenant.entity.Tenant;
import com.saas.tenant.service.TenantService;
import com.saas.user.entity.User;
import com.saas.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final TenantService tenantService;
    private final UserService userService;

    public AdminController(TenantService tenantService, UserService userService) {
        this.tenantService = tenantService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        List<Tenant> tenants = tenantService.getAllTenants();
        long activeTenants = tenants.stream().filter(Tenant::isActive).count();
        long totalUsers = 0;
        for (Tenant t : tenants) {
            totalUsers += userService.getUsersByTenant(t.getTenantId()).size();
        }

        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalTenants", tenants.size());
        dashboard.put("activeTenants", activeTenants);
        dashboard.put("inactiveTenants", tenants.size() - activeTenants);
        dashboard.put("totalUsers", totalUsers);
        dashboard.put("tenants", tenants);
        return ResponseEntity.ok(dashboard);
    }

    @GetMapping("/tenants")
    public ResponseEntity<List<Tenant>> getAllTenants() {
        return ResponseEntity.ok(tenantService.getAllTenants());
    }

    @GetMapping("/tenants/{tenantId}/users")
    public ResponseEntity<List<User>> getTenantUsers(@PathVariable String tenantId) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId));
    }

    @PostMapping("/tenants/{tenantId}/deactivate")
    public ResponseEntity<Void> deactivateTenant(@PathVariable String tenantId) {
        tenantService.deactivateTenant(tenantId);
        return ResponseEntity.noContent().build();
    }
}
```

### AdminDashboardDTO.java
```java
package com.saas.admin.dto;

import com.saas.tenant.entity.Tenant;

import java.util.List;

public class AdminDashboardDTO {
    private long totalTenants;
    private long activeTenants;
    private long totalUsers;
    private List<Tenant> tenants;

    public long getTotalTenants() { return totalTenants; }
    public void setTotalTenants(long totalTenants) { this.totalTenants = totalTenants; }
    public long getActiveTenants() { return activeTenants; }
    public void setActiveTenants(long activeTenants) { this.activeTenants = activeTenants; }
    public long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(long totalUsers) { this.totalUsers = totalUsers; }
    public List<Tenant> getTenants() { return tenants; }
    public void setTenants(List<Tenant> tenants) { this.tenants = tenants; }
}
```

---

## Rate Limiting Filter

### RateLimitingFilter.java
```java
package com.saas.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);
    private final RateLimitingConfig rateLimitingConfig;

    public RateLimitingFilter(RateLimitingConfig rateLimitingConfig) {
        this.rateLimitingConfig = rateLimitingConfig;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String tenantId = httpRequest.getHeader("X-Tenant-Id");
        if (tenantId != null) {
            if (!rateLimitingConfig.allowRequest(tenantId)) {
                httpResponse.setStatus(429);
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"error\":\"Rate limit exceeded\",\"message\":\"Too many requests. Please try again later.\"}");
                log.warn("Rate limit exceeded for tenant: {}", tenantId);
                return;
            }
            int remaining = rateLimitingConfig.getRemainingRequests(tenantId);
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        }
        chain.doFilter(request, response);
    }
}
```

---

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/tenants` | Create a new tenant |
| GET | `/api/tenants/{tenantId}` | Get tenant by ID |
| GET | `/api/tenants` | List all tenants |
| PUT | `/api/tenants/{tenantId}` | Update tenant |
| DELETE | `/api/tenants/{tenantId}` | Delete tenant |
| POST | `/api/users` | Create user (requires X-Tenant-Id) |
| GET | `/api/users/{id}` | Get user |
| GET | `/api/users/by-tenant/{tenantId}` | List users by tenant |
| PUT | `/api/users/{id}` | Update user |
| POST | `/api/feature-flags` | Create feature flag |
| GET | `/api/feature-flags/tenant/{tenantId}` | Get flags for tenant |
| GET | `/api/feature-flags/check` | Check if flag is enabled |
| PUT | `/api/feature-flags/{id}/toggle` | Toggle feature flag |
| POST | `/api/subscriptions` | Create subscription |
| GET | `/api/subscriptions/{tenantId}` | Get subscription |
| PUT | `/api/subscriptions/{tenantId}/upgrade` | Upgrade plan |
| POST | `/api/subscriptions/{tenantId}/cancel` | Cancel subscription |
| POST | `/api/billing/invoices` | Generate invoice |
| GET | `/api/billing/invoices/{tenantId}` | List invoices |
| POST | `/api/billing/invoices/{id}/pay` | Mark invoice as paid |
| GET | `/api/admin/dashboard` | Admin dashboard |
| GET | `/api/admin/tenants` | Admin list tenants |
| GET | `/api/admin/tenants/{tenantId}/users` | Admin view tenant users |

## Running the Platform

```bash
# Start PostgreSQL
docker run -d --name saas-postgres \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=saas_platform \
  -p 5432:5432 \
  postgres:15

# Build and run
mvn clean install -DskipTests
mvn spring-boot:run

# Create a tenant
curl -X POST http://localhost:8080/api/tenants \
  -H "Content-Type: application/json" \
  -d '{"tenantId":"acme-corp","name":"Acme Corp","domain":"acme.com","contactEmail":"admin@acme.com","plan":"PRO","maxUsers":50}'

# Create a user in a tenant
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: acme-corp" \
  -d '{"username":"john","email":"john@acme.com","password":"password123","fullName":"John Doe","role":"ADMIN"}'

# Create a feature flag
curl -X POST http://localhost:8080/api/feature-flags \
  -H "Content-Type: application/json" \
  -H "X-Tenant-Id: acme-corp" \
  -d '{"flagKey":"dark-mode","flagName":"Dark Mode","description":"Enable dark mode UI","tenantId":"acme-corp","enabled":true}'

# Get admin dashboard
curl http://localhost:8080/api/admin/dashboard

# Generate invoice
curl -X POST "http://localhost:8080/api/billing/invoices?tenantId=acme-corp&amount=99.99&description=Monthly%20subscription"
```
