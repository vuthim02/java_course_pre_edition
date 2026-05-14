# JPA / Hibernate: ORM and Performance

JPA is the Jakarta EE specification for object-relational mapping; Hibernate is the most popular implementation. Understanding entity mappings, relationships, fetching strategies, caching, and the N+1 query problem is essential for building performant data access layers.

## Entity Mapping

```java
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "products", schema = "inventory",
       uniqueConstraints = @UniqueConstraint(columnNames = {"sku"}))
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_seq")
    @SequenceGenerator(name = "product_seq", sequenceName = "product_id_seq", allocationSize = 50)
    private Long id;

    @Column(name = "sku", nullable = false, length = 50)
    private String sku;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // getters / setters
}
```

## Relationships

```java
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;

    // One-to-One with shared primary key
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL,
              fetch = FetchType.LAZY, optional = false)
    private Address address;

    // One-to-Many — a customer has many orders
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL,
               fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Order> orders = new HashSet<>();

    public void addOrder(Order order) {
        orders.add(order);
        order.setCustomer(this);
    }

    public void removeOrder(Order order) {
        orders.remove(order);
        order.setCustomer(null);
    }
}

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private String street;
    private String city;
    private String zipCode;
}

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderItem> items = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "order_tags",
        joinColumns = @JoinColumn(name = "order_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();
}

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;
    private BigDecimal unitPrice;
}

@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @ManyToMany(mappedBy = "tags")
    private Set<Order> orders = new HashSet<>();
}
```

## Cascading and Fetching

```java
@Entity
public class BlogPost {

    @Id
    private Long id;

    private String title;

    // Cascade: persist, merge, remove, refresh, detach, or ALL
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    // Default fetch for OneToMany is LAZY; for ManyToOne is EAGER
    @OneToMany(fetch = FetchType.LAZY)
    private List<Comment> comments;

    // ManyToOne defaults to EAGER — usually need LAZY
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    // Override fetch at query time with JOIN FETCH or EntityGraph
}
```

**CascadeType summary:**

| CascadeType | Behavior |
|---|---|
| PERSIST | Save child when parent is saved |
| MERGE | Merge child when parent is merged |
| REMOVE | Delete child when parent is deleted |
| REFRESH | Refresh child when parent is refreshed |
| DETACH | Detach child when parent is detached |
| ALL | All of the above |

## JPQL Queries

```java
@Repository
public class OrderRepository {

    @PersistenceContext
    private EntityManager em;

    // Basic JPQL
    public List<Order> findOrdersByCustomer(Long customerId) {
        return em.createQuery(
            "SELECT o FROM Order o WHERE o.customer.id = :customerId",
            Order.class)
            .setParameter("customerId", customerId)
            .getResultList();
    }

    // JPQL with JOIN FETCH to solve N+1
    public List<Order> findOrdersWithItems(Long customerId) {
        return em.createQuery(
            "SELECT DISTINCT o FROM Order o " +
            "LEFT JOIN FETCH o.items " +
            "WHERE o.customer.id = :customerId",
            Order.class)
            .setParameter("customerId", customerId)
            .getResultList();
    }

    // Aggregation query
    public List<Object[]> getOrderSummaryByStatus() {
        return em.createQuery(
            "SELECT o.status, COUNT(o), SUM(o.totalAmount) " +
            "FROM Order o GROUP BY o.status", Object[].class)
            .getResultList();
    }

    // Paginated query
    public List<Order> findRecentOrders(int page, int size) {
        return em.createQuery(
            "SELECT o FROM Order o ORDER BY o.orderDate DESC", Order.class)
            .setFirstResult(page * size)
            .setMaxResults(size)
            .getResultList();
    }
}
```

## Criteria API

```java
@Repository
public class ProductSearchRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Product> searchProducts(String name, BigDecimal minPrice,
                                        BigDecimal maxPrice, String category) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Product> query = cb.createQuery(Product.class);
        Root<Product> root = query.from(Product.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null && !name.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                "%" + name.toLowerCase() + "%"));
        }
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }
        if (category != null) {
            predicates.add(cb.equal(root.get("category"), category));
        }

        query.select(root)
            .where(cb.and(predicates.toArray(new Predicate[0])))
            .orderBy(cb.asc(root.get("name")));

        return em.createQuery(query).getResultList();
    }

    // Criteria with JOIN for filtering by related entity
    public List<Customer> findCustomersWithHighValueOrders(BigDecimal minTotal) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Customer> query = cb.createQuery(Customer.class);
        Root<Customer> root = query.from(Customer.class);
        Join<Customer, Order> orders = root.join("orders");

        query.select(root)
            .where(cb.greaterThan(orders.get("totalAmount"), minTotal))
            .distinct(true);

        return em.createQuery(query).getResultList();
    }
}
```

## Native SQL Queries

```java
@Repository
public class ReportRepository {

    @PersistenceContext
    private EntityManager em;

    public List<Object[]> monthlyRevenueReport(int year) {
        Query query = em.createNativeQuery("""
            SELECT
                EXTRACT(MONTH FROM o.order_date) AS month,
                COUNT(o.id) AS order_count,
                SUM(o.total_amount) AS revenue
            FROM orders o
            WHERE EXTRACT(YEAR FROM o.order_date) = :year
              AND o.status NOT IN ('CANCELLED', 'REFUNDED')
            GROUP BY EXTRACT(MONTH FROM o.order_date)
            ORDER BY month
            """);
        query.setParameter("year", year);
        return query.getResultList();
    }

    // Native query mapping to entity
    public List<Product> searchFullText(String keyword) {
        Query query = em.createNativeQuery(
            "SELECT * FROM products WHERE to_tsvector('english', name || ' ' || description) @@ plainto_tsquery('english', :keyword)",
            Product.class);
        query.setParameter("keyword", keyword);
        return query.getResultList();
    }

    // Native query with pagination
    public List<Order> findLargeOrders(int minAmount, int page, int size) {
        Query query = em.createNativeQuery(
            "SELECT * FROM orders WHERE total_amount >= :min ORDER BY total_amount DESC",
            Order.class);
        query.setParameter("min", minAmount);
        query.setFirstResult(page * size);
        query.setMaxResults(size);
        return query.getResultList();
    }
}
```

## First-Level Cache (Persistence Context)

```java
@Service
@Transactional
public class CacheDemoService {

    @PersistenceContext
    private EntityManager em;

    public void demonstrateFirstLevelCache(Long productId) {
        // First query — goes to database
        Product p1 = em.find(Product.class, productId);
        System.out.println("First fetch from DB: " + p1.getName());

        // Second query — served from persistence context (no SQL)
        Product p2 = em.find(Product.class, productId);
        System.out.println("Second fetch from cache: " + p2.getName());

        System.out.println("Same instance? " + (p1 == p2)); // true

        // Changes tracked automatically — no explicit update needed
        p1.setPrice(BigDecimal.valueOf(49.99));

        // Flush sends changes to DB
        em.flush();
    }

    // Clear the persistence context
    public void clearCache() {
        em.flush();
        em.clear(); // detaches ALL managed entities
    }
}
```

## N+1 Query Problem and Solutions

```java
// PROBLEM: N+1 queries
@Service
public class NPlusOneDemo {

    @PersistenceContext
    private EntityManager em;

    // BAD: N+1 — one query for orders, then N queries for each customer
    public List<Order> getOrdersWithCustomersBad() {
        List<Order> orders = em.createQuery("SELECT o FROM Order o", Order.class)
            .getResultList();
        // Hibernate will fire N additional SELECTs when accessing o.getCustomer()
        for (Order order : orders) {
            System.out.println(order.getCustomer().getName()); // triggers lazy load!
        }
        return orders;
    }

    // SOLUTION 1: JOIN FETCH
    public List<Order> getOrdersWithCustomersJoinFetch() {
        return em.createQuery(
            "SELECT DISTINCT o FROM Order o JOIN FETCH o.customer", Order.class)
            .getResultList();
    }

    // SOLUTION 2: Entity Graph (named)
    @EntityGraph(attributePaths = {"customer", "items.product"})
    public List<Order> getOrdersWithCustomerAndItems() {
        return em.createQuery("SELECT o FROM Order o", Order.class)
            .getResultList();
    }

    // SOLUTION 3: Entity Graph (dynamic)
    public List<Order> getOrdersWithDynamicGraph() {
        EntityGraph<Order> graph = em.createEntityGraph(Order.class);
        graph.addAttributeNodes("customer");
        graph.addSubgraph("items").addAttributeNodes("product");

        return em.createQuery("SELECT o FROM Order o", Order.class)
            .setHint("jakarta.persistence.fetchgraph", graph)
            .getResultList();
    }

    // SOLUTION 4: Batch fetching (@BatchSize)
    // On entity: @BatchSize(size = 10) — loads 10 related entities at a time
}
```

**BatchSize annotation:**

```java
@Entity
@BatchSize(size = 25)
public class Order {
    // ...
}
```

## Second-Level Cache

```java
// Requires Hibernate second-level cache provider (e.g., Ehcache, Caffeine)
// See caching.md for CacheManager configuration

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    // Frequently read, rarely changed entities benefit from 2LC
}

@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Entity
public class Country {
    // Read-only / infrequently updated reference data
}

// Cache regions can be configured per entity
// application.yml:
// spring.jpa.properties.hibernate.cache.region.factory_class: jcache
// spring.jpa.properties.hibernate.cache.use_second_level_cache: true
// spring.jpa.properties.hibernate.cache.use_query_cache: true
```

## Schema Generation / DDL Auto

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: validate   # Options: none, validate, update, create, create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        default_batch_fetch_size: 25
        jdbc:
          batch_size: 30
          order_inserts: true
          order_updates: true
```

| Setting | Behavior |
|---|---|
| `none` | No schema management |
| `validate` | Validates schema matches entities — fails on mismatch |
| `update` | Updates schema to match entities (dangerous in prod) |
| `create` | Drops and recreates schema on startup |
| `create-drop` | Same as create, also drops on shutdown (ideal for tests) |

**Programmatic schema generation:**

```java
@Configuration
public class SchemaConfig {

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean emf =
            new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.example.domain");
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties props = new Properties();
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        props.put("hibernate.show_sql", true);
        props.put("hibernate.format_sql", true);
        emf.setJpaProperties(props);

        return emf;
    }
}
```
