# Spring Data JPA: Repositories, Specifications, Auditing

Spring Data JPA eliminates boilerplate data access code by generating implementations from repository interfaces. Derived query methods, `@Query`, Specifications, projections, and auditing cover most persistence needs.

## JpaRepository, CrudRepository, PagingAndSortingRepository

```java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

// CrudRepository: basic CRUD (save, findById, findAll, count, delete, existsById)
// PagingAndSortingRepository: adds findAll(Pageable), findAll(Sort)
// JpaRepository: adds flush, saveAndFlush, deleteInBatch, findAll with Example

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Inherits:
    // - Optional<User> findById(Long id)
    // - List<User> findAll()
    // - Page<User> findAll(Pageable pageable)
    // - List<User> findAll(Sort sort)
    // - User save(User entity)
    // - void delete(User entity)
    // - void deleteById(Long id)
    // - long count()
    // - boolean existsById(Long id)
    // - void flush()
    // - <S extends T> List<S> saveAll(Iterable<S> entities)
    // - void deleteAllInBatch(Iterable<User> entities)
}
```

## Derived Query Methods

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Exact match
    Optional<User> findByEmail(String email);

    // Multiple fields
    Optional<User> findByEmailAndActiveTrue(String email);

    List<User> findByNameAndAge(String name, int age);

    // Like / Containing
    List<User> findByNameContainingIgnoreCase(String namePart);

    List<User> findByNameLike(String pattern);

    // StartsWith / EndsWith
    List<User> findByNameStartsWith(String prefix);

    List<User> findByNameEndsWith(String suffix);

    // Comparison operators
    List<User> findByAgeGreaterThanEqual(int minAge);

    List<User> findByAgeBetween(int min, int max);

    List<User> findByCreatedAtAfter(LocalDateTime date);

    List<User> findByCreatedAtBefore(LocalDateTime date);

    // Null checks
    List<User> findByEmailIsNull();

    List<User> findByEmailIsNotNull();

    // IN clause
    List<User> findByAgeIn(Collection<Integer> ages);

    List<User> findByNameIn(Collection<String> names);

    // Boolean conditions
    List<User> findByActiveTrue();
    List<User> findByActiveFalse();

    // Order
    List<User> findByNameContainingOrderByNameAsc(String name);

    List<User> findByNameContainingOrderByAgeDescNameAsc(String name);

    // Pagination and sorting
    Page<User> findByAgeGreaterThan(int age, Pageable pageable);

    List<User> findByActiveTrue(Sort sort);

    // First / Top
    Optional<User> findFirstByOrderByCreatedAtDesc();

    List<User> findTop10ByActiveTrueOrderByCreatedAtDesc();

    // Count / Exists
    long countByActiveTrue();
    long countByAgeGreaterThan(int age);
    boolean existsByEmail(String email);

    // Delete derived queries
    void deleteByEmail(String email);
    long deleteByActiveFalse();

    // Distinct
    List<User> findDistinctByName(String name);

    // Query hints (e.g., for caching)
    @QueryHints(@QueryHint(name = "org.hibernate.cacheable", value = "true"))
    Optional<User> findById(Long id);
}
```

## @Query (JPQL and Native)

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // JPQL query
    @Query("SELECT o FROM Order o WHERE o.customer.email = :email")
    List<Order> findByCustomerEmail(@Param("email") String email);

    // JPQL with JOIN FETCH to avoid N+1
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items " +
           "LEFT JOIN FETCH o.customer " +
           "WHERE o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // JPQL with pagination
    @Query("SELECT o FROM Order o WHERE o.status = :status")
    Page<Order> findByStatus(@Param("status") OrderStatus status, Pageable pageable);

    // Projection with constructor expression
    @Query("SELECT new com.example.dto.OrderSummary(o.id, o.customer.name, " +
           "o.totalAmount, o.status) FROM Order o WHERE o.customer.id = :customerId")
    List<OrderSummary> findOrderSummariesByCustomer(@Param("customerId") Long customerId);

    // Native SQL query
    @Query(value = "SELECT * FROM orders WHERE total_amount >= :minAmount " +
                   "ORDER BY total_amount DESC LIMIT :limit",
           nativeQuery = true)
    List<Order> findTopOrders(@Param("minAmount") java.math.BigDecimal minAmount,
                              @Param("limit") int limit);

    // Native query with pagination
    @Query(value = "SELECT * FROM orders WHERE status = :status",
           countQuery = "SELECT COUNT(*) FROM orders WHERE status = :status",
           nativeQuery = true)
    Page<Order> findByStatusNative(@Param("status") String status, Pageable pageable);

    // Dynamic sorting with JPQL
    @Query("SELECT o FROM Order o WHERE o.totalAmount > :min")
    List<Order> findOrdersAbove(@Param("min") java.math.BigDecimal min, Sort sort);
}
```

## @Modifying for UPDATE/DELETE

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.price = p.price * :multiplier " +
           "WHERE p.category = :category")
    int bulkUpdatePrice(@Param("category") String category,
                        @Param("multiplier") java.math.BigDecimal multiplier);

    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.active = false AND p.createdAt < :before")
    int deleteInactiveProductsOlderThan(@Param("before") LocalDateTime before);

    @Modifying
    @Transactional
    @Query(value = "UPDATE products SET quantity = quantity + :delta " +
                   "WHERE id = :productId AND quantity + :delta >= 0",
           nativeQuery = true)
    int adjustStock(@Param("productId") Long productId, @Param("delta") int delta);
}
```

## Specifications (JpaSpecificationExecutor)

```java
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>,
                                           JpaSpecificationExecutor<Product> {
}

// Specification builders
public class ProductSpecifications {

    public static Specification<Product> hasCategory(String category) {
        return (root, query, cb) ->
            category == null ? null : cb.equal(root.get("category"), category);
    }

    public static Specification<Product> priceBetween(java.math.BigDecimal min,
                                                       java.math.BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (min != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), min));
            }
            if (max != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), max));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Product> nameContains(String namePart) {
        return (root, query, cb) ->
            namePart == null ? null :
                cb.like(cb.lower(root.get("name")), "%" + namePart.toLowerCase() + "%");
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> createdAfter(LocalDateTime date) {
        return (root, query, cb) ->
            date == null ? null : cb.greaterThan(root.get("createdAt"), date);
    }

    // Dynamic filtering with joined entities
    public static Specification<Product> hasTag(String tagName) {
        return (root, query, cb) -> {
            if (tagName == null) return null;
            Join<Product, Tag> tags = root.join("tags");
            return cb.equal(tags.get("name"), tagName);
        };
    }
}
```

```java
@Service
public class ProductSearchService {

    private final ProductRepository productRepository;

    public ProductSearchService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> search(String category, java.math.BigDecimal minPrice,
                                 java.math.BigDecimal maxPrice, String name,
                                 Boolean active, String tag) {
        Specification<Product> spec = Specification.where(null);

        if (category != null) spec = spec.and(ProductSpecifications.hasCategory(category));
        if (minPrice != null || maxPrice != null)
            spec = spec.and(ProductSpecifications.priceBetween(minPrice, maxPrice));
        if (name != null) spec = spec.and(ProductSpecifications.nameContains(name));
        if (active != null && active) spec = spec.and(ProductSpecifications.isActive());
        if (tag != null) spec = spec.and(ProductSpecifications.hasTag(tag));

        return productRepository.findAll(spec, Sort.by("name").ascending());
    }

    public Page<Product> searchPaged(String category, java.math.BigDecimal minPrice,
                                      java.math.BigDecimal maxPrice, Pageable pageable) {
        Specification<Product> spec = Specification
            .where(ProductSpecifications.hasCategory(category))
            .and(ProductSpecifications.priceBetween(minPrice, maxPrice));

        return productRepository.findAll(spec, pageable);
    }
}
```

## Auditing

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
}

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Get current user from SecurityContext
        return Optional.ofNullable(SecurityContextHolder.getContext())
            .map(ctx -> ctx.getAuthentication())
            .filter(auth -> auth != null && auth.isAuthenticated())
            .map(auth -> auth.getName())
            .or(() -> Optional.of("system"));
    }
}
```

```java
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class AuditableEntity {

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    // getters
}
```

```java
@Entity
public class BlogPost extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // AuditableEntity provides createdAt, updatedAt, createdBy, updatedBy
}
```

## Projections (Interface-Based)

```java
// Interface-based projection — only selected fields are loaded
public interface UserProjection {
    Long getId();
    String getName();
    String getEmail();
}

// Closed projection — all fields are exact matches
public interface UserNameEmail {
    String getName();
    String getEmail();
}

// Open projection — uses SpEL for computed values
public interface UserDetailProjection {
    Long getId();
    String getName();
    String getEmail();

    @Value("#{target.name + ' <' + target.email + '>'}")
    String getDisplayName();

    @Value("#{T(com.example.util.DateUtils).format(target.createdAt)}")
    String getCreatedAtFormatted();
}

// Dynamic projection — same method can return different shapes
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    <T> List<T> findByActiveTrue(Class<T> type);

    <T> Optional<T> findById(Long id, Class<T> type);

    <T> Page<T> findByAgeGreaterThan(int age, Pageable pageable, Class<T> type);
}
```

```java
@Service
public class UserReportingService {

    private final UserRepository userRepository;

    public List<UserNameEmail> getBasicUserList() {
        return userRepository.findByActiveTrue(UserNameEmail.class);
    }

    public List<UserDetailProjection> getDetailedUserList() {
        return userRepository.findByActiveTrue(UserDetailProjection.class);
    }

    public Page<UserProjection> getPagedUsers(Pageable pageable) {
        return userRepository.findByAgeGreaterThan(18, pageable, UserProjection.class);
    }
}
```

**DTO projection (alternative):**

```java
// Also works with DTO records if constructor matches
public record UserDto(Long id, String name, String email) {}

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // JPQL constructor expression
    @Query("SELECT new com.example.dto.UserDto(u.id, u.name, u.email) FROM User u")
    List<UserDto> findAllDto();
}
```
