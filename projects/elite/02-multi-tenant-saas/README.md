# Project 2: Multi-Tenant Enterprise SaaS Platform

**Concepts:** Multi-tenancy (schema-per-tenant), Subscription Management, Billing & Invoicing, Tenant Isolation, Rate Limiting, Feature Flags, Admin Dashboard

## Architecture

```
                         ┌─────────────────────┐
                         │   API Gateway        │
                         │  (Rate Limit, Auth)  │
                         └──────┬──────────────┘
                                │
          ┌─────────────────────┼─────────────────────┐
          │                     │                     │
    ┌─────▼──────┐       ┌──────▼──────┐       ┌─────▼──────┐
    │  Tenant    │       │  Subscription│       │   Billing  │
    │  Service   │       │  Service     │       │   Service  │
    └─────┬──────┘       └──────┬──────┘       └─────┬──────┘
          │                     │                     │
    ┌─────▼─────────────────────▼─────────────────────▼──────┐
    │                   PostgreSQL (per-tenant schema)        │
    │  tenants_public.tenants  │  tenant_abc.users           │
    │  tenants_public.plans    │  tenant_abc.orders          │
    └────────────────────────────────────────────────────────┘
          │
    ┌─────▼──────────────────────────────────────────────────┐
    │   Redis (Rate Limiting, Session Cache, Feature Flags)  │
    └────────────────────────────────────────────────────────┘
```

## Table of Contents
1. [Multi-Tenancy Strategy](#1-multi-tenancy-strategy)
2. [Tenant Context Filter](#2-tenant-context-filter)
3. [Tenant Service](#3-tenant-service)
4. [Subscription Service](#4-subscription-service)
5. [Billing Service](#5-billing-service)
6. [Rate Limiting](#6-rate-limiting)
7. [Feature Flags](#7-feature-flags)
8. [Docker Compose](#8-docker-compose)
9. [Testing](#9-testing)

---

## 1. Multi-Tenancy Strategy

We use **schema-per-tenant** with a shared public schema for tenant metadata.

```
Database: saas_db
├── public schema  →  tenants, plans, feature_flags (shared)
├── tenant_abc     →  users, orders, products (isolated)
├── tenant_xyz     →  users, orders, products (isolated)
└── tenant_123     →  users, orders, products (isolated)
```

### TenantContext — ThreadLocal holder

```java
package com.saas.tenant;

public class TenantContext {
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    public static void setTenantId(String tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static String getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
```

### TenantIdentifierResolver — JPA schema resolver

```java
package com.saas.tenant;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.stereotype.Component;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        String tenantId = TenantContext.getTenantId();
        return tenantId != null ? tenantId : "public";
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
```

### TenantConnectionProvider — Multi-tenant data source

```java
package com.saas.tenant;

import org.hibernate.engine.jdbc.connections.spi.AbstractDataSourceBasedMultiTenantConnectionProviderImpl;
import org.springframework.stereotype.Component;
import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TenantConnectionProvider
        extends AbstractDataSourceBasedMultiTenantConnectionProviderImpl {

    private final DataSource defaultDataSource;
    private final Map<String, DataSource> tenantDataSources = new ConcurrentHashMap<>();

    public TenantConnectionProvider(DataSource dataSource) {
        this.defaultDataSource = dataSource;
    }

    @Override
    protected DataSource selectAnyDataSource() {
        return defaultDataSource;
    }

    @Override
    protected DataSource selectDataSource(String tenantIdentifier) {
        return tenantDataSources.computeIfAbsent(tenantIdentifier, id -> {
            // In production, create schema-specific DataSource or use same with search_path
            return defaultDataSource;
        });
    }
}
```

### MultiTenantConfig — Hibernate configuration

```java
package com.saas.config;

import com.saas.tenant.TenantConnectionProvider;
import com.saas.tenant.TenantIdentifierResolver;
import org.hibernate.cfg.Environment;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
public class MultiTenantConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            TenantConnectionProvider connectionProvider,
            TenantIdentifierResolver resolver) {

        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.saas.model");

        HibernateJpaVendorAdapter adapter = new HibernateJpaVendorAdapter();
        emf.setJpaVendorAdapter(adapter);

        Properties props = new Properties();
        props.put(Environment.MULTI_TENANT, "SCHEMA");
        props.put(Environment.MULTI_TENANT_CONNECTION_PROVIDER, connectionProvider);
        props.put(Environment.MULTI_TENANT_IDENTIFIER_RESOLVER, resolver);
        props.put(Environment.FORMAT_SQL, true);
        props.put(Environment.SHOW_SQL, false);
        emf.setJpaProperties(props);

        return emf;
    }
}
```

---

## 2. Tenant Context Filter

Extracts tenant ID from HTTP header `X-Tenant-Id` and sets it in `TenantContext`.

```java
package com.saas.tenant;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
@Order(1)
public class TenantContextFilter implements Filter {

    static final String TENANT_HEADER = "X-Tenant-Id";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String tenantId = req.getHeader(TENANT_HEADER);

        if (tenantId == null || tenantId.isBlank()) {
            // For public endpoints (registration, login), use "public"
            String path = req.getRequestURI();
            if (isPublicPath(path)) {
                tenantId = "public";
            } else {
                res.sendError(400, "X-Tenant-Id header is required");
                return;
            }
        }

        try {
            TenantContext.setTenantId(tenantId);
            MDC.put("tenantId", tenantId);
            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            MDC.remove("tenantId");
        }
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
            || path.startsWith("/api/v1/tenants/register")
            || path.startsWith("/actuator")
            || path.startsWith("/swagger-ui");
    }
}
```

---

## 3. Tenant Service

### Tenant Model

```java
package com.saas.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants", schema = "public")
public class Tenant {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String slug;  // e.g. "acme-corp"

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TenantStatus status;

    private String schemaName;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant activatedAt;
    private Instant suspendedAt;

    @Column(nullable = false)
    private int maxUsers;

    @Column(nullable = false)
    private int maxStorageGb;

    @Column(nullable = false)
    private boolean featuresEnabled;

    // --- lifecycle ---
    @PrePersist
    void onCreate() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        status = TenantStatus.PENDING;
        schemaName = "tenant_" + slug.replace("-", "_");
    }

    // getters/setters omitted for brevity
    public UUID getId() { return id; }
    public String getSlug() { return slug; }
    public String getName() { return name; }
    public SubscriptionTier getTier() { return tier; }
    public TenantStatus getStatus() { return status; }
    public String getSchemaName() { return schemaName; }
    public int getMaxUsers() { return maxUsers; }
    public int getMaxStorageGb() { return maxStorageGb; }
}

enum SubscriptionTier { FREE, STARTER, PROFESSIONAL, ENTERPRISE }
enum TenantStatus { PENDING, ACTIVE, SUSPENDED, CANCELLED }
```

### Registration Request

```java
package com.saas.dto;

import jakarta.validation.constraints.*;
import java.util.UUID;

public record TenantRegistrationRequest(
    @NotBlank String name,
    @NotBlank @Pattern(regexp = "^[a-z0-9-]+$") String slug,
    @Email @NotBlank String adminEmail,
    @NotBlank String adminPassword,
    SubscriptionTier requestedTier
) {}

public record TenantResponse(
    UUID id, String slug, String name,
    SubscriptionTier tier, TenantStatus status,
    Instant createdAt, int maxUsers, int maxStorageGb
) {}
```

### Tenant Service

```java
package com.saas.service;

import com.saas.dto.*;
import com.saas.exception.TenantLimitExceededException;
import com.saas.model.*;
import com.saas.repository.TenantRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TenantService {

    private final TenantRepository tenantRepo;
    private final JdbcTemplate jdbc;

    public TenantService(TenantRepository tenantRepo, JdbcTemplate jdbc) {
        this.tenantRepo = tenantRepo;
        this.jdbc = jdbc;
    }

    @Transactional
    public TenantResponse register(TenantRegistrationRequest req) {
        if (tenantRepo.findBySlug(req.slug()).isPresent()) {
            throw new IllegalArgumentException("Tenant slug already taken: " + req.slug());
        }

        TierLimits limits = TierLimits.forTier(req.requestedTier());

        Tenant tenant = new Tenant();
        tenant.setName(req.name());
        tenant.setSlug(req.slug());
        tenant.setTier(req.requestedTier());
        tenant.setMaxUsers(limits.maxUsers());
        tenant.setMaxStorageGb(limits.maxStorageGb());

        tenant = tenantRepo.save(tenant);

        // Create tenant schema and grant permissions
        String schema = tenant.getSchemaName();
        jdbc.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
        jdbc.execute("GRANT USAGE ON SCHEMA " + schema + " TO saas_app");

        return toResponse(tenant);
    }

    @Transactional
    public void activateTenant(UUID tenantId) {
        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setActivatedAt(java.time.Instant.now());
        tenantRepo.save(tenant);
    }

    @Transactional
    public void suspendTenant(UUID tenantId) {
        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        tenant.setStatus(TenantStatus.SUSPENDED);
        tenant.setSuspendedAt(java.time.Instant.now());
        tenantRepo.save(tenant);
    }

    public List<TenantResponse> listAll() {
        return tenantRepo.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public TenantResponse getById(UUID id) {
        return toResponse(tenantRepo.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found")));
    }

    @Transactional
    public TenantResponse upgradeTier(UUID tenantId, SubscriptionTier newTier) {
        Tenant tenant = tenantRepo.findById(tenantId)
            .orElseThrow(() -> new IllegalArgumentException("Tenant not found"));
        TierLimits limits = TierLimits.forTier(newTier);
        tenant.setTier(newTier);
        tenant.setMaxUsers(limits.maxUsers());
        tenant.setMaxStorageGb(limits.maxStorageGb());
        tenant = tenantRepo.save(tenant);
        return toResponse(tenant);
    }

    private TenantResponse toResponse(Tenant t) {
        return new TenantResponse(t.getId(), t.getSlug(), t.getName(),
            t.getTier(), t.getStatus(), t.getCreatedAt(),
            t.getMaxUsers(), t.getMaxStorageGb());
    }
}

record TierLimits(int maxUsers, int maxStorageGb) {
    static TierLimits forTier(SubscriptionTier tier) {
        return switch (tier) {
            case FREE -> new TierLimits(5, 1);
            case STARTER -> new TierLimits(25, 10);
            case PROFESSIONAL -> new TierLimits(100, 100);
            case ENTERPRISE -> new TierLimits(10000, 10000);
        };
    }
}
```

### Tenant Controller

```java
package com.saas.controller;

import com.saas.dto.*;
import com.saas.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/register")
    public ResponseEntity<TenantResponse> register(@Valid @RequestBody TenantRegistrationRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tenantService.register(req));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TenantResponse>> listAll() {
        return ResponseEntity.ok(tenantService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tenantService.getById(id));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable UUID id) {
        tenantService.activateTenant(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> suspend(@PathVariable UUID id) {
        tenantService.suspendTenant(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/tier")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TenantResponse> upgradeTier(
            @PathVariable UUID id,
            @RequestParam SubscriptionTier tier) {
        return ResponseEntity.ok(tenantService.upgradeTier(id, tier));
    }
}
```

### Tenant Repository

```java
package com.saas.repository;

import com.saas.model.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    Optional<Tenant> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
```

---

## 4. Subscription Service

### Subscription Model

```java
package com.saas.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    private BillingCycle billingCycle;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status;

    private Instant currentPeriodStart;
    private Instant currentPeriodEnd;

    private Instant cancelledAt;
    private Instant createdAt;

    @Column(nullable = false)
    private boolean autoRenew;

    @PrePersist
    void onCreate() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        status = SubscriptionStatus.ACTIVE;
    }

    // getters/setters
    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public SubscriptionPlan getPlan() { return plan; }
    public BillingCycle getBillingCycle() { return billingCycle; }
    public SubscriptionStatus getStatus() { return status; }
    public Instant getCurrentPeriodEnd() { return currentPeriodEnd; }
    public boolean isAutoRenew() { return autoRenew; }

    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public void setPlan(SubscriptionPlan plan) { this.plan = plan; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }
    public void setCurrentPeriodEnd(Instant currentPeriodEnd) { this.currentPeriodEnd = currentPeriodEnd; }
    public void setAutoRenew(boolean autoRenew) { this.autoRenew = autoRenew; }
    public void setStatus(SubscriptionStatus status) { this.status = status; }
    public void setCancelledAt(Instant cancelledAt) { this.cancelledAt = cancelledAt; }
}

enum SubscriptionPlan {
    FREE(0), STARTER(29), PROFESSIONAL(99), ENTERPRISE(499);

    final int monthlyPriceUsd;
    SubscriptionPlan(int monthlyPriceUsd) { this.monthlyPriceUsd = monthlyPriceUsd; }
    public int getMonthlyPriceUsd() { return monthlyPriceUsd; }
}

enum BillingCycle { MONTHLY, ANNUAL }
enum SubscriptionStatus { ACTIVE, PAST_DUE, CANCELED, EXPIRED }
```

### Subscription Service

```java
package com.saas.subscription;

import com.saas.dto.SubscriptionResponse;
import com.saas.exception.SubscriptionException;
import com.saas.model.*;
import com.saas.repository.SubscriptionRepository;
import com.saas.billing.InvoiceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subRepo;
    private final InvoiceService invoiceService;

    public SubscriptionService(SubscriptionRepository subRepo, InvoiceService invoiceService) {
        this.subRepo = subRepo;
        this.invoiceService = invoiceService;
    }

    @Transactional
    public SubscriptionResponse createSubscription(UUID tenantId, SubscriptionPlan plan,
                                                    BillingCycle cycle) {
        if (subRepo.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE).isPresent()) {
            throw new SubscriptionException("Tenant already has an active subscription");
        }

        Subscription sub = new Subscription();
        sub.setTenantId(tenantId);
        sub.setPlan(plan);
        sub.setBillingCycle(cycle);
        sub.setAutoRenew(true);

        Instant now = Instant.now();
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(cycle == BillingCycle.MONTHLY
            ? now.plus(30, ChronoUnit.DAYS)
            : now.plus(365, ChronoUnit.DAYS));

        sub = subRepo.save(sub);

        // Generate initial invoice
        invoiceService.createInvoice(tenantId, sub.getId(), plan.getMonthlyPriceUsd());

        return toResponse(sub);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID tenantId) {
        Subscription sub = subRepo.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new SubscriptionException("No active subscription found"));
        sub.setStatus(SubscriptionStatus.CANCELED);
        sub.setAutoRenew(false);
        sub.setCancelledAt(Instant.now());
        sub = subRepo.save(sub);
        return toResponse(sub);
    }

    @Transactional
    public SubscriptionResponse changePlan(UUID tenantId, SubscriptionPlan newPlan) {
        Subscription sub = subRepo.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
            .orElseThrow(() -> new SubscriptionException("No active subscription found"));
        sub.setPlan(newPlan);
        sub = subRepo.save(sub);
        return toResponse(sub);
    }

    @Transactional
    public void renewSubscription(UUID subscriptionId) {
        Subscription sub = subRepo.findById(subscriptionId)
            .orElseThrow(() -> new SubscriptionException("Subscription not found"));

        if (!sub.isAutoRenew()) return;

        Instant now = Instant.now();
        sub.setCurrentPeriodStart(now);
        sub.setCurrentPeriodEnd(sub.getBillingCycle() == BillingCycle.MONTHLY
            ? now.plus(30, ChronoUnit.DAYS)
            : now.plus(365, ChronoUnit.DAYS));

        subRepo.save(sub);
        invoiceService.createInvoice(sub.getTenantId(), sub.getId(),
            sub.getPlan().getMonthlyPriceUsd());
    }

    public SubscriptionResponse getSubscription(UUID tenantId) {
        return subRepo.findByTenantIdAndStatus(tenantId, SubscriptionStatus.ACTIVE)
            .map(this::toResponse)
            .orElseThrow(() -> new SubscriptionException("No active subscription"));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getTenantId(),
            s.getPlan(), s.getBillingCycle(), s.getStatus(),
            s.getCurrentPeriodStart(), s.getCurrentPeriodEnd(),
            s.isAutoRenew());
    }
}
```

### DTO & Repository

```java
package com.saas.dto;

import com.saas.model.*;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id, UUID tenantId, SubscriptionPlan plan,
    BillingCycle billingCycle, SubscriptionStatus status,
    Instant currentPeriodStart, Instant currentPeriodEnd,
    boolean autoRenew
) {}
```

```java
package com.saas.repository;

import com.saas.model.Subscription;
import com.saas.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByTenantIdAndStatus(UUID tenantId, SubscriptionStatus status);
}
```

---

## 5. Billing & Invoice Service

### Invoice Model

```java
package com.saas.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false)
    private UUID subscriptionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    @Column(nullable = false)
    private Instant dueDate;

    private Instant paidAt;
    private Instant createdAt;

    @PrePersist
    void onCreate() {
        id = UUID.randomUUID();
        createdAt = Instant.now();
        if (status == null) status = InvoiceStatus.PENDING;
    }

    public UUID getId() { return id; }
    public UUID getTenantId() { return tenantId; }
    public BigDecimal getAmount() { return amount; }
    public InvoiceStatus getStatus() { return status; }
    public Instant getDueDate() { return dueDate; }
    public Instant getCreatedAt() { return createdAt; }

    public void setTenantId(UUID tenantId) { this.tenantId = tenantId; }
    public void setSubscriptionId(UUID subscriptionId) { this.subscriptionId = subscriptionId; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setDueDate(Instant dueDate) { this.dueDate = dueDate; }
    public void setStatus(InvoiceStatus status) { this.status = status; }
    public void setPaidAt(Instant paidAt) { this.paidAt = paidAt; }
}

enum InvoiceStatus { PENDING, PAID, OVERDUE, CANCELLED, REFUNDED }
```

### Invoice Service

```java
package com.saas.billing;

import com.saas.dto.InvoiceResponse;
import com.saas.model.*;
import com.saas.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepo;

    public InvoiceService(InvoiceRepository invoiceRepo) {
        this.invoiceRepo = invoiceRepo;
    }

    @Transactional
    public InvoiceResponse createInvoice(UUID tenantId, UUID subscriptionId, int amountUsd) {
        Invoice invoice = new Invoice();
        invoice.setTenantId(tenantId);
        invoice.setSubscriptionId(subscriptionId);
        invoice.setAmount(BigDecimal.valueOf(amountUsd));
        invoice.setDueDate(Instant.now().plus(7, ChronoUnit.DAYS));
        invoice = invoiceRepo.save(invoice);
        return toResponse(invoice);
    }

    @Transactional
    public void markAsPaid(UUID invoiceId) {
        Invoice invoice = invoiceRepo.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found"));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(Instant.now());
        invoiceRepo.save(invoice);
    }

    @Transactional
    public void processOverdueInvoices() {
        List<Invoice> overdue = invoiceRepo
            .findByStatusAndDueDateBefore(InvoiceStatus.PENDING, Instant.now());
        for (Invoice inv : overdue) {
            inv.setStatus(InvoiceStatus.OVERDUE);
            invoiceRepo.save(inv);
        }
    }

    public List<InvoiceResponse> getInvoicesForTenant(UUID tenantId) {
        return invoiceRepo.findByTenantIdOrderByCreatedAtDesc(tenantId)
            .stream().map(this::toResponse).toList();
    }

    private InvoiceResponse toResponse(Invoice i) {
        return new InvoiceResponse(i.getId(), i.getTenantId(), i.getSubscriptionId(),
            i.getAmount(), i.getStatus(), i.getDueDate(), i.getPaidAt(), i.getCreatedAt());
    }
}
```

### DTO & Repository

```java
package com.saas.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record InvoiceResponse(
    UUID id, UUID tenantId, UUID subscriptionId,
    BigDecimal amount, InvoiceStatus status,
    Instant dueDate, Instant paidAt, Instant createdAt
) {}
```

```java
package com.saas.repository;

import com.saas.model.Invoice;
import com.saas.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);
    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, Instant date);
}
```

---

## 6. Rate Limiting per Tenant

Uses Redis + Bucket4j for distributed rate limiting.

```java
package com.saas.config;

import com.saas.tenant.TenantContext;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(2)
public class RateLimitingFilter implements Filter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final StringRedisTemplate redis;

    public RateLimitingFilter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || "public".equals(tenantId)) {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(tenantId, this::createBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            res.setStatus(429);
            res.setHeader("Retry-After", "60");
            res.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
        }
    }

    private Bucket createBucket(String tenantId) {
        // Default: 100 requests per minute per tenant
        Bandwidth limit = Bandwidth.classic(100, Refill.greedy(100, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}
```

---

## 7. Feature Flags

```java
package com.saas.config;

import com.saas.model.SubscriptionTier;
import com.saas.tenant.TenantContext;
import org.springframework.stereotype.Component;
import java.util.Set;

@Component
public class FeatureFlagService {

    public boolean isFeatureEnabled(String featureKey) {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null) return false;
        // In production, fetch from DB/Redis per tenant
        return getFeaturesForTenant(tenantId).contains(featureKey);
    }

    private Set<String> getFeaturesForTenant(String tenantId) {
        // Mock: based on tenant tier
        return switch (tenantId) {
            case "enterprise-tenant" -> Set.of("analytics", "sso", "audit-log",
                "custom-domain", "api-webhooks", "team-management");
            case "pro-tenant" -> Set.of("analytics", "sso", "audit-log", "team-management");
            default -> Set.of("basic-analytics", "team-management");
        };
    }
}
```

---

## 8. Docker Compose

```yaml
# docker-compose.yml
version: '3.9'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: saas_db
      POSTGRES_USER: saas_app
      POSTGRES_PASSWORD: saas_secret
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-db.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  saas-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/saas_db
      SPRING_DATASOURCE_USERNAME: saas_app
      SPRING_DATASOURCE_PASSWORD: saas_secret
      SPRING_REDIS_HOST: redis
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
```

### application.yml

```yaml
spring:
  application:
    name: saas-platform
  datasource:
    url: jdbc:postgresql://localhost:5432/saas_db
    username: saas_app
    password: saas_secret
    hikari:
      maximum-pool-size: 30
      minimum-idle: 5
  jpa:
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        ddl-auto: update
        multi_tenant: SCHEMA
  redis:
    host: localhost
    port: 6379

server:
  port: 8080

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
```

### pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.saas</groupId>
    <artifactId>saas-platform</artifactId>
    <version>1.0.0</version>
    <name>saas-platform</name>

    <properties>
        <java.version>21</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
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
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>

        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>com.bucket4j</groupId>
            <artifactId>bucket4j-core</artifactId>
            <version>8.7.0</version>
        </dependency>

        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
            <version>2.3.0</version>
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
            </plugin>
        </plugins>
    </build>
</project>
```

---

## 9. Testing

### Unit Test — Tenant Service

```java
package com.saas;

import com.saas.dto.TenantRegistrationRequest;
import com.saas.model.*;
import com.saas.repository.TenantRepository;
import com.saas.service.TenantService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TenantServiceTest {

    @Mock TenantRepository tenantRepo;
    @Mock JdbcTemplate jdbc;
    @InjectMocks TenantService tenantService;

    @Test
    void register_shouldCreateTenantAndSchema() {
        when(tenantRepo.findBySlug("acme")).thenReturn(Optional.empty());
        when(tenantRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        var req = new TenantRegistrationRequest("Acme Corp", "acme",
            "admin@acme.com", "password", SubscriptionTier.STARTER);
        var res = tenantService.register(req);

        assertNotNull(res.id());
        assertEquals("acme", res.slug());
        assertEquals(SubscriptionTier.STARTER, res.tier());
        verify(jdbc).execute("CREATE SCHEMA IF NOT EXISTS tenant_acme");
    }

    @Test
    void register_shouldRejectDuplicateSlug() {
        when(tenantRepo.findBySlug("acme")).thenReturn(Optional.of(new Tenant()));
        var req = new TenantRegistrationRequest("Acme Corp", "acme",
            "admin@acme.com", "password", SubscriptionTier.FREE);
        assertThrows(IllegalArgumentException.class, () -> tenantService.register(req));
    }
}
```

### Integration Test — Full Flow

```java
package com.saas;

import com.saas.dto.TenantRegistrationRequest;
import com.saas.model.SubscriptionTier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class TenantIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
        .withDatabaseName("saas_test")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry reg) {
        reg.add("spring.datasource.url", postgres::getJdbcUrl);
        reg.add("spring.datasource.username", postgres::getUsername);
        reg.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    TestRestTemplate rest;

    @Test
    void fullTenantRegistrationFlow() {
        var req = new TenantRegistrationRequest("Test Corp", "test-corp",
            "admin@test.com", "pass123", SubscriptionTier.PROFESSIONAL);

        ResponseEntity<?> res = rest.postForEntity("/api/v1/tenants/register", req, Object.class);

        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNotNull(res.getBody());
    }
}
```

---

## API Endpoints Summary

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/v1/tenants/register` | Register new tenant |
| GET | `/api/v1/tenants` | List all tenants (ADMIN) |
| GET | `/api/v1/tenants/{id}` | Get tenant details |
| POST | `/api/v1/tenants/{id}/activate` | Activate tenant |
| POST | `/api/v1/tenants/{id}/suspend` | Suspend tenant |
| PATCH | `/api/v1/tenants/{id}/tier` | Upgrade/downgrade tier |
| POST | `/api/v1/subscriptions` | Create subscription |
| GET | `/api/v1/subscriptions` | Get active subscription |
| POST | `/api/v1/subscriptions/cancel` | Cancel subscription |
| PATCH | `/api/v1/subscriptions/plan` | Change plan |
| GET | `/api/v1/invoices` | List invoices for tenant |
| POST | `/api/v1/invoices/{id}/pay` | Mark invoice as paid |
