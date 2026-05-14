# Enterprise Engineering — Lesson 4: SQL & JDBC

## SQL Basics

### Creating Tables

```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    age INTEGER CHECK (age >= 0),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    total DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### CRUD Operations

```sql
-- CREATE
INSERT INTO users (name, email, age) VALUES ('Alice', 'alice@email.com', 30);
INSERT INTO users (name, email, age) VALUES ('Bob', 'bob@email.com', 25);

-- READ
SELECT * FROM users;
SELECT name, email FROM users WHERE age > 18;
SELECT * FROM users ORDER BY created_at DESC LIMIT 10;
SELECT COUNT(*) as total, AVG(age) as avg_age FROM users;

-- UPDATE
UPDATE users SET age = 31 WHERE name = 'Alice';

-- DELETE
DELETE FROM users WHERE age < 18;
```

### Joins

```sql
-- INNER JOIN — only matching records
SELECT u.name, o.total, o.status
FROM users u
JOIN orders o ON u.id = o.user_id;

-- LEFT JOIN — all users, even without orders
SELECT u.name, COUNT(o.id) as order_count
FROM users u
LEFT JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name;

-- Aggregation
SELECT u.name, SUM(o.total) as total_spent
FROM users u
JOIN orders o ON u.id = o.user_id
GROUP BY u.id, u.name
HAVING SUM(o.total) > 100
ORDER BY total_spent DESC;
```

### Indexes

```sql
-- Speed up queries on frequently-filtered columns
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_created ON orders(created_at DESC);

-- Composite index for multi-column queries
CREATE INDEX idx_orders_user_status ON orders(user_id, status);

-- Check query plan
EXPLAIN ANALYZE SELECT * FROM users WHERE email = 'alice@email.com';
```

## JDBC — Java Database Connectivity

### Setup

```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.0</version>
</dependency>
```

### Basic JDBC

```java
public class UserRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String USER = "myuser";
    private static final String PASSWORD = "mypassword";

    public User findById(Long id) {
        String sql = "SELECT id, name, email, age FROM users WHERE id = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getInt("age")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error", e);
        }
        return null;
    }

    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY name";
        List<User> users = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return users;
    }

    public void save(User user) {
        String sql = "INSERT INTO users (name, email, age) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql,
                 Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setInt(3, user.getAge());
            stmt.executeUpdate();

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Connection Pooling with HikariCP

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabasePool {
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("myuser");
        config.setPassword("mypassword");
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);     // 30 seconds
        config.setIdleTimeout(600000);           // 10 minutes
        config.setMaxLifetime(1800000);          // 30 minutes
        config.setPoolName("MyPool");

        // Performance settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
```

### Transactions

```java
public void transferMoney(Long fromId, Long toId, BigDecimal amount) {
    String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

    try (Connection conn = DatabasePool.getConnection()) {
        conn.setAutoCommit(false);  // Start transaction

        try {
            // Subtract from source
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, amount.negate());
                stmt.setLong(2, fromId);
                stmt.executeUpdate();
            }

            // Add to destination
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setBigDecimal(1, amount);
                stmt.setLong(2, toId);
                stmt.executeUpdate();
            }

            conn.commit();  // Both succeed
        } catch (SQLException e) {
            conn.rollback();  // Both fail
            throw new RuntimeException("Transfer failed", e);
        }
    } catch (SQLException e) {
        throw new RuntimeException("Database error", e);
    }
}
```

---

### Exercises

1. Create a PostgreSQL database with a `products` table (id, name, price, quantity). Insert 10 products.
2. Write a JDBC program that performs CRUD on the products table.
3. Add HikariCP connection pooling to your application.
4. Implement a transaction that transfers stock between two products.
5. Use `EXPLAIN ANALYZE` to compare query performance with and without indexes.
