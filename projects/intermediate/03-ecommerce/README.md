# Project 3: E-commerce Backend

**Concepts:** Complex JPA Entity Relationships, Cart Management, Order Workflow, Mock Payment Processing, Inventory Management, Address Management

## Project Structure

```
src/main/java/com/example/ecommerce/
├── EcommerceApplication.java
├── config/
│   └── SecurityConfig.java
├── controller/
│   ├── ProductController.java
│   ├── CategoryController.java
│   ├── CartController.java
│   ├── OrderController.java
│   ├── PaymentController.java
│   └── AddressController.java
├── service/
│   ├── ProductService.java
│   ├── CategoryService.java
│   ├── CartService.java
│   ├── OrderService.java
│   ├── PaymentService.java
│   └── AddressService.java
├── repository/
│   ├── ProductRepository.java
│   ├── CategoryRepository.java
│   ├── CartRepository.java
│   ├── OrderRepository.java
│   └── AddressRepository.java
├── model/
│   ├── Product.java
│   ├── Category.java
│   ├── Cart.java
│   ├── CartItem.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── OrderStatus.java
│   ├── User.java
│   └── Address.java
├── dto/
│   ├── ProductRequest.java
│   ├── ProductResponse.java
│   ├── CategoryRequest.java
│   ├── CategoryResponse.java
│   ├── CartResponse.java
│   ├── CartItemRequest.java
│   ├── OrderRequest.java
│   ├── OrderResponse.java
│   ├── PaymentRequest.java
│   ├── PaymentResponse.java
│   └── AddressRequest.java
└── exception/
    ├── InsufficientStockException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.yml
└── application-prod.yml
```

## pom.xml

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

    <groupId>com.example</groupId>
    <artifactId>ecommerce</artifactId>
    <version>1.0.0</version>
    <name>ecommerce</name>
    <description>E-commerce Backend</description>

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
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
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
spring:
  datasource:
    url: jdbc:h2:mem:ecomdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
    open-in-view: false

server:
  port: 8080

---
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/ecomdb
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  h2:
    console:
      enabled: false
```

## EcommerceApplication.java

```java
package com.example.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}
```

## model/User.java

```java
package com.example.ecommerce.model;

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

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public User() {}

    public User(String username, String email, String password, String fullName) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.fullName = fullName;
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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/Category.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @ManyToMany(mappedBy = "categories")
    private List<Product> products = new ArrayList<>();

    public Category() {}

    public Category(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<Product> getProducts() { return products; }
    public void setProducts(List<Product> products) { this.products = products; }
}
```

## model/Product.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(name = "image_url")
    private String imageUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "product_categories",
        joinColumns = @JoinColumn(name = "product_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public Product() {}

    public Product(String name, String description, BigDecimal price, Integer stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<Category> getCategories() { return categories; }
    public void setCategories(List<Category> categories) { this.categories = categories; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/Cart.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CartItem> items = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public Cart() {}

    public Cart(User user) {
        this.user = user;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<CartItem> getItems() { return items; }
    public void setItems(Set<CartItem> items) { this.items = items; }

    public BigDecimal getTotalPrice() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }
}
```

## model/CartItem.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_items")
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Cart getCart() { return cart; }
    public void setCart(Cart cart) { this.cart = cart; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() {
        return product.getPrice().multiply(BigDecimal.valueOf(quantity));
    }
}
```

## model/OrderStatus.java

```java
package com.example.ecommerce.model;

public enum OrderStatus {
    PENDING,
    CONFIRMED,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    REFUNDED
}
```

## model/Order.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> orderItems = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipping_address_id")
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
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

    public Order() {}

    public Order(User user, Address shippingAddress) {
        this.user = user;
        this.shippingAddress = shippingAddress;
        this.status = OrderStatus.PENDING;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Set<OrderItem> getOrderItems() { return orderItems; }
    public void setOrderItems(Set<OrderItem> orderItems) { this.orderItems = orderItems; }

    public Address getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(Address shippingAddress) { this.shippingAddress = shippingAddress; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
```

## model/OrderItem.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    public OrderItem() {}

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal price) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public BigDecimal getSubtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
```

## model/Address.java

```java
package com.example.ecommerce.model;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String zipCode;

    @Column(nullable = false)
    private String country;

    @Column(name = "is_default")
    private boolean isDefault = false;

    @Column(name = "phone_number")
    private String phoneNumber;

    public Address() {}

    public Address(User user, String street, String city, String state, String zipCode, String country) {
        this.user = user;
        this.street = street;
        this.city = city;
        this.state = state;
        this.zipCode = zipCode;
        this.country = country;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}
```

## repository/ProductRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.id = :categoryId")
    Page<Product> findByCategoryId(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findInStock();

    List<Product> findByStockQuantityLessThan(Integer threshold);
}
```

## repository/CategoryRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
```

## repository/CartRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Cart;
import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByUser(User user);
    Optional<Cart> findByUserId(Long userId);
    void deleteByUser(User user);
}
```

## repository/OrderRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    Page<Order> findByUserAndStatus(User user, OrderStatus status, Pageable pageable);
    List<Order> findByStatusAndCreatedAtBefore(OrderStatus status, LocalDateTime dateTime);
    List<Order> findByPaymentStatus(String paymentStatus);
}
```

## repository/AddressRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
    List<Address> findByUserId(Long userId);
}
```

## service/ProductService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public ProductResponse createProduct(ProductRequest request) {
        Product product = new Product(request.name(), request.description(),
                                      request.price(), request.stockQuantity());
        product.setImageUrl(request.imageUrl());

        if (request.categoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            product.setCategories(categories);
        }

        Product saved = productRepository.save(product);
        return ProductResponse.fromEntity(saved);
    }

    public ProductResponse getProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));
        return ProductResponse.fromEntity(product);
    }

    public Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).map(ProductResponse::fromEntity);
    }

    public Page<ProductResponse> searchProducts(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByNameContainingIgnoreCase(query, pageable)
                .map(ProductResponse::fromEntity);
    }

    public Page<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(ProductResponse::fromEntity);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + id));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());

        if (request.categoryIds() != null) {
            List<Category> categories = categoryRepository.findAllById(request.categoryIds());
            product.setCategories(categories);
        }

        Product updated = productRepository.save(product);
        return ProductResponse.fromEntity(updated);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found: " + id);
        }
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(int threshold) {
        return productRepository.findByStockQuantityLessThan(threshold)
                .stream().map(ProductResponse::fromEntity).toList();
    }
}
```

## service/CategoryService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.CategoryRequest;
import com.example.ecommerce.dto.CategoryResponse;
import com.example.ecommerce.model.Category;
import com.example.ecommerce.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new IllegalArgumentException("Category already exists: " + request.name());
        }
        Category category = new Category(request.name(), request.description());
        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    public CategoryResponse getCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        return CategoryResponse.fromEntity(category);
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::fromEntity).toList();
    }

    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found: " + id));
        category.setName(request.name());
        category.setDescription(request.description());
        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
```

## service/CartService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public CartService(CartRepository cartRepository, ProductRepository productRepository,
                       UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return CartResponse.fromEntity(cart);
    }

    public CartResponse addItem(Long userId, CartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createCart(userId));

        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + request.productId()));

        if (product.getStockQuantity() < request.quantity()) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(request.productId()))
            .findFirst()
            .ifPresentOrElse(
                item -> item.setQuantity(item.getQuantity() + request.quantity()),
                () -> cart.getItems().add(new CartItem(cart, product, request.quantity()))
            );

        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse updateItemQuantity(Long userId, Long productId, int quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Product not in cart: " + productId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new IllegalArgumentException("Insufficient stock");
            }
            item.setQuantity(quantity);
        }

        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));

        cart.getItems().removeIf(item -> item.getProduct().getId().equals(productId));
        Cart saved = cartRepository.save(cart);
        return CartResponse.fromEntity(saved);
    }

    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart not found for user: " + userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private Cart createCart(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        return cartRepository.save(new Cart(user));
    }
}
```

## service/OrderService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.exception.InsufficientStockException;
import com.example.ecommerce.model.*;
import com.example.ecommerce.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, CartRepository cartRepository,
                        AddressRepository addressRepository, ProductRepository productRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.addressRepository = addressRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    public OrderResponse placeOrder(Long userId, OrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Address address = addressRepository.findById(request.addressId())
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + request.addressId()));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot place order with empty cart");
        }

        Order order = new Order(user, address);

        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for: " + product.getName()
                        + ". Available: " + product.getStockQuantity());
            }

            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem(order, product, cartItem.getQuantity(), product.getPrice());
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);
        order.setTotalAmount(cart.getTotalPrice());

        if (request.paymentMethod() != null) {
            order.setPaymentMethod(request.paymentMethod());
        }

        Order saved = orderRepository.save(order);

        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        return OrderResponse.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(OrderResponse::fromEntity);
    }

    public OrderResponse cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Cannot cancel order in status: " + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        return OrderResponse.fromEntity(saved);
    }
}
```

## service/PaymentService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final OrderRepository orderRepository;

    public PaymentService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public PaymentResponse processPayment(PaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + request.orderId()));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order is not in pending status");
        }

        String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        order.setPaymentMethod(request.paymentMethod());
        order.setPaymentStatus("COMPLETED");
        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);

        return new PaymentResponse(
            transactionId,
            order.getId(),
            order.getTotalAmount(),
            "COMPLETED",
            request.paymentMethod()
        );
    }

    public PaymentResponse refundPayment(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Order not found: " + orderId));

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new IllegalStateException("Only delivered orders can be refunded");
        }

        order.setPaymentStatus("REFUNDED");
        order.setStatus(OrderStatus.REFUNDED);
        orderRepository.save(order);

        return new PaymentResponse(
            "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            order.getId(),
            order.getTotalAmount(),
            "REFUNDED",
            order.getPaymentMethod()
        );
    }
}
```

## service/AddressService.java

```java
package com.example.ecommerce.service;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.model.User;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public Address createAddress(Long userId, AddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));

        Address address = new Address(user, request.street(), request.city(),
                                      request.state(), request.zipCode(), request.country());
        address.setPhoneNumber(request.phoneNumber());

        if (request.isDefault() || addressRepository.findByUser(user).isEmpty()) {
            address.setDefault(true);
        }

        return addressRepository.save(address);
    }

    public List<Address> getUserAddresses(Long userId) {
        return addressRepository.findByUserId(userId);
    }

    public Address updateAddress(Long addressId, Long userId, AddressRequest request) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        address.setStreet(request.street());
        address.setCity(request.city());
        address.setState(request.state());
        address.setZipCode(request.zipCode());
        address.setCountry(request.country());
        address.setPhoneNumber(request.phoneNumber());

        return addressRepository.save(address);
    }

    public void deleteAddress(Long addressId, Long userId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new EntityNotFoundException("Address not found: " + addressId));

        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Address does not belong to user");
        }

        addressRepository.delete(address);
    }
}
```

## controller/ProductController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.ProductRequest;
import com.example.ecommerce.dto.ProductResponse;
import com.example.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(productService.getAllProducts(page, size, sortBy, sortDir));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.searchProducts(q, page, size));
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<ProductResponse>> getByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId, page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/CategoryController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CategoryRequest;
import com.example.ecommerce.dto.CategoryResponse;
import com.example.ecommerce.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategory(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/CartController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CartItemRequest;
import com.example.ecommerce.dto.CartResponse;
import com.example.ecommerce.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody CartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, productId, quantity));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
```

## controller/OrderController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.OrderRequest;
import com.example.ecommerce.dto.OrderResponse;
import com.example.ecommerce.model.OrderStatus;
import com.example.ecommerce.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.placeOrder(request.userId(), request));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getOrder(orderId, userId));
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, page, size));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(
            @PathVariable Long orderId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId));
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable Long orderId,
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, status));
    }
}
```

## controller/PaymentController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.PaymentRequest;
import com.example.ecommerce.dto.PaymentResponse;
import com.example.ecommerce.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/process")
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.processPayment(request));
    }

    @PostMapping("/{orderId}/refund")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.refundPayment(orderId));
    }
}
```

## controller/AddressController.java

```java
package com.example.ecommerce.controller;

import com.example.ecommerce.dto.AddressRequest;
import com.example.ecommerce.model.Address;
import com.example.ecommerce.service.AddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/{userId}")
    public ResponseEntity<Address> createAddress(
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(addressService.createAddress(userId, request));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<List<Address>> getUserAddresses(@PathVariable Long userId) {
        return ResponseEntity.ok(addressService.getUserAddresses(userId));
    }

    @PutMapping("/{addressId}/{userId}")
    public ResponseEntity<Address> updateAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId,
            @Valid @RequestBody AddressRequest request) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, userId, request));
    }

    @DeleteMapping("/{addressId}/{userId}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long addressId,
            @PathVariable Long userId) {
        addressService.deleteAddress(addressId, userId);
        return ResponseEntity.noContent().build();
    }
}
```

## DTO Records

```java
// dto/ProductRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(
    @NotBlank String name,
    String description,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @NotNull @Min(0) Integer stockQuantity,
    String imageUrl,
    List<Long> categoryIds
) {}

// dto/ProductResponse.java
package com.example.ecommerce.dto;

import com.example.ecommerce.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
    Long id, String name, String description, BigDecimal price,
    Integer stockQuantity, String imageUrl, List<String> categories,
    LocalDateTime createdAt, LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product product) {
        return new ProductResponse(
            product.getId(), product.getName(), product.getDescription(),
            product.getPrice(), product.getStockQuantity(), product.getImageUrl(),
            product.getCategories().stream().map(c -> c.getName()).toList(),
            product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}

// dto/CategoryRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequest(
    @NotBlank String name,
    String description
) {}

// dto/CategoryResponse.java
package com.example.ecommerce.dto;

import com.example.ecommerce.model.Category;

public record CategoryResponse(Long id, String name, String description) {
    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}

// dto/CartItemRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
    @NotNull Long productId,
    @NotNull @Min(1) Integer quantity
) {}

// dto/CartResponse.java
package com.example.ecommerce.dto;

import com.example.ecommerce.model.Cart;
import java.math.BigDecimal;
import java.util.Set;
import java.util.stream.Collectors;

public record CartResponse(
    Long id, Long userId, Set<CartItemResponse> items,
    BigDecimal totalPrice, int totalItems
) {
    public static CartResponse fromEntity(Cart cart) {
        return new CartResponse(
            cart.getId(), cart.getUser().getId(),
            cart.getItems().stream().map(CartItemResponse::fromEntity).collect(Collectors.toSet()),
            cart.getTotalPrice(), cart.getTotalItems()
        );
    }

    public record CartItemResponse(Long id, Long productId, String productName,
                                   BigDecimal price, int quantity, BigDecimal subtotal) {
        public static CartItemResponse fromEntity(com.example.ecommerce.model.CartItem item) {
            return new CartItemResponse(
                item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                item.getProduct().getPrice(), item.getQuantity(), item.getSubtotal()
            );
        }
    }
}

// dto/OrderRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotNull;

public record OrderRequest(
    @NotNull Long userId,
    @NotNull Long addressId,
    String paymentMethod
) {}

// dto/OrderResponse.java
package com.example.ecommerce.dto;

import com.example.ecommerce.model.Order;
import com.example.ecommerce.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

public record OrderResponse(
    Long id, Long userId, Set<OrderItemResponse> items,
    OrderStatus status, BigDecimal totalAmount,
    String paymentMethod, String paymentStatus,
    AddressInfo shippingAddress,
    LocalDateTime createdAt
) {
    public static OrderResponse fromEntity(Order order) {
        return new OrderResponse(
            order.getId(), order.getUser().getId(),
            order.getOrderItems().stream().map(OrderItemResponse::fromEntity).collect(Collectors.toSet()),
            order.getStatus(), order.getTotalAmount(),
            order.getPaymentMethod(), order.getPaymentStatus(),
            order.getShippingAddress() != null
                ? new AddressInfo(order.getShippingAddress().getId(),
                    order.getShippingAddress().getStreet(),
                    order.getShippingAddress().getCity(),
                    order.getShippingAddress().getState(),
                    order.getShippingAddress().getZipCode(),
                    order.getShippingAddress().getCountry())
                : null,
            order.getCreatedAt()
        );
    }

    public record OrderItemResponse(Long id, Long productId, String productName,
                                    int quantity, BigDecimal price, BigDecimal subtotal) {
        public static OrderItemResponse fromEntity(com.example.ecommerce.model.OrderItem item) {
            return new OrderItemResponse(
                item.getId(), item.getProduct().getId(), item.getProduct().getName(),
                item.getQuantity(), item.getPrice(), item.getSubtotal()
            );
        }
    }

    public record AddressInfo(Long id, String street, String city,
                              String state, String zipCode, String country) {}
}

// dto/PaymentRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
    @NotNull Long orderId,
    @NotBlank String paymentMethod
) {}

// dto/PaymentResponse.java
package com.example.ecommerce.dto;

import java.math.BigDecimal;

public record PaymentResponse(
    String transactionId, Long orderId, BigDecimal amount,
    String status, String paymentMethod
) {}

// dto/AddressRequest.java
package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;

public record AddressRequest(
    @NotBlank String street,
    @NotBlank String city,
    @NotBlank String state,
    @NotBlank String zipCode,
    @NotBlank String country,
    String phoneNumber,
    boolean isDefault
) {}
```

## exception/InsufficientStockException.java

```java
package com.example.ecommerce.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
```

## exception/GlobalExceptionHandler.java

```java
package com.example.ecommerce.exception;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Insufficient Stock");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
```

## config/SecurityConfig.java

```java
package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
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
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().denyAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
```

## repository/UserRepository.java

```java
package com.example.ecommerce.repository;

import com.example.ecommerce.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
```

## Entity Relationships

```
User 1──N Order 1──N OrderItem N──1 Product
User 1──1 Cart 1──N CartItem N──1 Product
Product N──M Category
User 1──N Address
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/products` | Create product |
| GET | `/api/products` | List products (paginated) |
| GET | `/api/products/{id}` | Get product |
| GET | `/api/products/search?q=` | Search products |
| GET | `/api/products/category/{id}` | Products by category |
| PUT | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Delete product |
| POST | `/api/categories` | Create category |
| GET | `/api/categories` | List categories |
| GET | `/api/cart/{userId}` | Get cart |
| POST | `/api/cart/{userId}/items` | Add item to cart |
| PUT | `/api/cart/{userId}/items/{productId}?quantity=` | Update quantity |
| DELETE | `/api/cart/{userId}/items/{productId}` | Remove item |
| DELETE | `/api/cart/{userId}` | Clear cart |
| POST | `/api/orders` | Place order |
| GET | `/api/orders/{id}?userId=` | Get order |
| GET | `/api/orders?userId=` | List user orders |
| POST | `/api/orders/{id}/cancel?userId=` | Cancel order |
| POST | `/api/payments/process` | Process payment |
| POST | `/api/payments/{orderId}/refund` | Refund payment |
| POST | `/api/addresses/{userId}` | Create address |
| GET | `/api/addresses/{userId}` | List user addresses |
| PUT | `/api/addresses/{addressId}/{userId}` | Update address |
| DELETE | `/api/addresses/{addressId}/{userId}` | Delete address |
