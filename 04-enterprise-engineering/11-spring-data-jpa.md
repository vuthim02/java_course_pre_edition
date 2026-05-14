# Enterprise Engineering — Lesson 11: Spring Data JPA & Hibernate

## What is JPA?

**JPA** (Jakarta Persistence API) is the standard for ORM (Object-Relational Mapping) in Java — mapping Java objects to database tables.

```
Java Object (Entity)          Database Table
┌─────────────────────┐       ┌─────────────────────┐
│ User                │       │ users               │
│ - id: Long          │──────▶│ id BIGINT PK        │
│ - name: String      │       │ name VARCHAR(100)   │
│ - email: String     │       │ email VARCHAR(255)  │
│ - age: int          │       │ age INT             │
└─────────────────────┘       └─────────────────────┘
```

## Entity Mapping

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "age_value")
    private Integer age;

    @Transient  // Not persisted to DB
    private String temporaryData;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

## Relationships

### One-to-Many / Many-to-One

```java
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();
}

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY = load on demand (preferred)
    @JoinColumn(name = "user_id")
    private User user;

    private BigDecimal total;
}
```

### Many-to-Many

```java
@Entity
public class Student {
    @Id @GeneratedValue private Long id;

    @ManyToMany
    @JoinTable(
        name = "student_courses",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();
}

@Entity
public class Course {
    @Id @GeneratedValue private Long id;
    private String name;

    @ManyToMany(mappedBy = "courses")
    private Set<Student> students = new HashSet<>();
}
```

### One-to-One

```java
@Entity
public class User {
    @Id @GeneratedValue private Long id;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
}

@Entity
public class Profile {
    @Id @GeneratedValue private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String bio;
    private String avatarUrl;
}
```

## Spring Data JPA Repositories

```java
public interface UserRepository extends JpaRepository<User, Long> {
    // Query methods — Spring Data generates the implementation!
    Optional<User> findByEmail(String email);
    List<User> findByNameContainingIgnoreCase(String name);
    List<User> findByAgeGreaterThanEqual(int age);

    // Multiple conditions
    List<User> findByAgeBetween(int min, int max);
    List<User> findByAgeAndName(int age, String name);
    List<User> findByNameIn(List<String> names);

    // Sorting
    List<User> findByAgeOrderByNameDesc(int age);

    // Pagination
    Page<User> findAll(Pageable pageable);
    Page<User> findByAge(int age, Pageable pageable);

    // Count
    long countByAgeGreaterThan(int age);

    // Delete
    void deleteByEmail(String email);
}
```

### Custom Queries

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // JPQL
    @Query("SELECT u FROM User u WHERE u.email = ?1")
    Optional<User> findUserByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword%")
    List<User> searchByName(@Param("keyword") String keyword);

    // Native SQL
    @Query(value = "SELECT * FROM users WHERE age > :minAge", nativeQuery = true)
    List<User> findOlderThan(@Param("minAge") int minAge);

    // Modifying
    @Modifying
    @Query("UPDATE User u SET u.age = :age WHERE u.id = :id")
    int updateAge(@Param("id") Long id, @Param("age") int age);
}
```

### Specifications (Dynamic Queries)

```java
public interface UserRepository extends JpaRepository<User, Long>,
                                        JpaSpecificationExecutor<User> {}

// Create dynamic queries
public class UserSpecifications {
    public static Specification<User> hasName(String name) {
        return (root, query, cb) ->
            name == null ? null : cb.equal(root.get("name"), name);
    }

    public static Specification<User> hasMinAge(int minAge) {
        return (root, query, cb) ->
            cb.greaterThanOrEqualTo(root.get("age"), minAge);
    }
}

// Usage:
List<User> users = userRepository.findAll(
    Specification.where(hasName("Alice"))
        .and(hasMinAge(18))
);
```

## N+1 Query Problem

```java
// PROBLEM: Each order triggers a separate query
List<User> users = userRepository.findAll();  // 1 query
for (User user : users) {
    System.out.println(user.getOrders().size());  // N queries!
}
// Total: 1 + N queries

// SOLUTION 1: JOIN FETCH
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders")
List<User> findAllWithOrders();

// SOLUTION 2: @EntityGraph
@EntityGraph(attributePaths = "orders")
@Query("SELECT u FROM User u")
List<User> findAllWithOrders();
```

## Transaction Management

```java
@Service
public class OrderService {

    @Transactional  // All DB operations in ONE transaction
    public Order createOrder(Long userId, List<Item> items) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setItems(items);
        order.setTotal(calculateTotal(items));

        return orderRepository.save(order);
    }

    @Transactional(readOnly = true)  // Performance optimization
    public Order findById(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    @Transactional(rollbackFor = InsufficientStockException.class)
    public void processPayment(Long orderId) {
        // If this fails, the ENTIRE transaction rolls back
    }
}
```

---

### Exercises

1. Create `Book` and `Author` entities with a Many-to-Many relationship.
2. Create Spring Data JPA repositories with 5 custom query methods.
3. Implement pagination and sorting for a `findAll` endpoint.
4. Add `@EntityGraph` to solve an N+1 problem.
5. Use `@Query` with both JPQL and native SQL.
