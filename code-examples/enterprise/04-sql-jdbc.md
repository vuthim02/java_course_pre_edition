# SQL, JDBC, and Connection Pools

JDBC (Java Database Connectivity) is the low-level API for interacting with relational databases. Modern applications use DataSource with connection pools like HikariCP for performance. PreparedStatement prevents SQL injection, and transaction management ensures data integrity.

## JDBC with DriverManager (Basic)

```java
import java.sql.*;

public class JdbcBasicExample {

    public static void main(String[] args) {
        // Basic approach — not recommended for production
        String url = "jdbc:postgresql://localhost:5432/mydb";
        String user = "appuser";
        String password = "secret";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id, name, email FROM users")) {

            while (rs.next()) {
                long id = rs.getLong("id");
                String name = rs.getString("name");
                String email = rs.getString("email");
                System.out.printf("User %d: %s (%s)%n", id, name, email);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}
```

## PreparedStatement (SQL Injection Prevention)

```java
public class PreparedStatementExample {

    public User findUserByEmail(String email) {
        String sql = "SELECT id, name, email FROM users WHERE email = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(
                        rs.getLong("id"),
                        rs.getString("name"),
                        rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user", e);
        }
        return null;
    }

    public int createUser(User user) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?) RETURNING id";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.name());
            ps.setString(2, user.email());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to create user", e);
        }
        return -1;
    }
}
```

## CallableStatement (Stored Procedures)

```java
public class CallableStatementExample {

    public TaxCalculation calculateTax(BigDecimal amount, String region) {
        String sql = "{call calculate_tax(?, ?, ?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setBigDecimal(1, amount);
            cs.setString(2, region);
            cs.registerOutParameter(3, Types.NUMERIC);

            cs.execute();

            BigDecimal taxAmount = cs.getBigDecimal(3);
            return new TaxCalculation(amount, region, taxAmount);

        } catch (SQLException e) {
            throw new DataAccessException("Failed to calculate tax", e);
        }
    }

    public List<String> getAuditTrail(Long userId) {
        String sql = "{call get_audit_trail(?)}";

        try (Connection conn = dataSource.getConnection();
             CallableStatement cs = conn.prepareCall(sql)) {

            cs.setLong(1, userId);

            boolean hasResults = cs.execute();
            List<String> entries = new ArrayList<>();

            if (hasResults) {
                try (ResultSet rs = cs.getResultSet()) {
                    while (rs.next()) {
                        entries.add(rs.getString("action") + " at " + rs.getTimestamp("created_at"));
                    }
                }
            }
            return entries;

        } catch (SQLException e) {
            throw new DataAccessException("Failed to get audit trail", e);
        }
    }
}
```

## ResultSet Mapping to Objects

```java
public record User(Long id, String name, String email, LocalDateTime createdAt) {}

public record Order(Long id, Long userId, BigDecimal total, String status) {}

public class ResultSetMapper {

    public List<User> findAllUsers() {
        String sql = "SELECT id, name, email, created_at FROM users ORDER BY id";
        List<User> users = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapToUser(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users", e);
        }
        return users;
    }

    public Map<User, List<Order>> getUsersWithOrders() {
        String sql = """
            SELECT u.id AS user_id, u.name, u.email, u.created_at,
                   o.id AS order_id, o.total, o.status
            FROM users u
            LEFT JOIN orders o ON o.user_id = u.id
            ORDER BY u.id, o.id
            """;

        Map<User, List<Order>> result = new LinkedHashMap<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User(
                    rs.getLong("user_id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getTimestamp("created_at").toLocalDateTime()
                );

                Order order = null;
                long orderId = rs.getLong("order_id");
                if (!rs.wasNull()) {
                    order = new Order(
                        orderId,
                        user.id(),
                        rs.getBigDecimal("total"),
                        rs.getString("status")
                    );
                }

                result.computeIfAbsent(user, k -> new ArrayList<>());
                if (order != null) {
                    result.get(user).add(order);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to fetch users with orders", e);
        }
        return result;
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        return new User(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
```

## Transaction Management

```java
public class TransactionExample {

    public void transferFunds(Long fromAccount, Long toAccount, BigDecimal amount) {
        String debitSql = "UPDATE accounts SET balance = balance - ? WHERE id = ? AND balance >= ?";
        String creditSql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";

        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

            try (PreparedStatement debit = conn.prepareStatement(debitSql)) {
                debit.setBigDecimal(1, amount);
                debit.setLong(2, fromAccount);
                debit.setBigDecimal(3, amount);

                int rowsAffected = debit.executeUpdate();
                if (rowsAffected == 0) {
                    throw new InsufficientFundsException("Account " + fromAccount + " has insufficient funds");
                }
            }

            try (PreparedStatement credit = conn.prepareStatement(creditSql)) {
                credit.setBigDecimal(1, amount);
                credit.setLong(2, toAccount);
                credit.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            throw new DataAccessException("Transfer failed", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close connection: " + e.getMessage());
                }
            }
        }
    }

    public void processWithSavepoint(Long orderId) {
        String updateInventory = "UPDATE inventory SET quantity = quantity - 1 WHERE product_id = ? AND quantity > 0";
        String createShipment = "INSERT INTO shipments (order_id, status) VALUES (?, 'PENDING')";
        String updateOrder = "UPDATE orders SET status = 'CONFIRMED' WHERE id = ?";

        Savepoint inventorySavepoint = null;

        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(updateInventory)) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }

            inventorySavepoint = conn.setSavepoint("inventory_updated");

            try (PreparedStatement ps = conn.prepareStatement(createShipment)) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }

            try (PreparedStatement ps = conn.prepareStatement(updateOrder)) {
                ps.setLong(1, orderId);
                ps.executeUpdate();
            }

            conn.commit();

        } catch (SQLException e) {
            if (inventorySavepoint != null) {
                try {
                    conn.rollback(inventorySavepoint);
                    conn.releaseSavepoint(inventorySavepoint);
                    conn.commit();
                } catch (SQLException ex) {
                    System.err.println("Savepoint rollback failed: " + ex.getMessage());
                }
            }
            throw new DataAccessException("Order processing failed", e);
        }
    }
}
```

## Batch Updates

```java
public class BatchUpdateExample {

    public int[] bulkInsertUsers(List<User> users) {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (User user : users) {
                ps.setString(1, user.name());
                ps.setString(2, user.email());
                ps.addBatch();
            }

            int[] results = ps.executeBatch();
            conn.commit();
            return results;

        } catch (SQLException e) {
            throw new DataAccessException("Batch insert failed", e);
        }
    }

    public void batchUpdateWithBatching() {
        String sql = "UPDATE products SET price = price * ? WHERE category = ?";

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            // Electronics: 10% discount
            ps.setBigDecimal(1, new BigDecimal("0.90"));
            ps.setString(2, "ELECTRONICS");
            ps.addBatch();

            // Clothing: 20% discount
            ps.setBigDecimal(1, new BigDecimal("0.80"));
            ps.setString(2, "CLOTHING");
            ps.addBatch();

            // Books: 5% discount
            ps.setBigDecimal(1, new BigDecimal("0.95"));
            ps.setString(2, "BOOKS");
            ps.addBatch();

            int[] results = ps.executeBatch();
            conn.commit();

            int totalAffected = Arrays.stream(results).sum();
            System.out.println("Updated " + totalAffected + " products");

        } catch (SQLException e) {
            throw new DataAccessException("Batch update failed", e);
        }
    }
}
```

## HikariCP Connection Pool Configuration

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;

public class HikariCpConfig {

    public static DataSource createDataSource() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("appuser");
        config.setPassword("secret");
        config.setDriverClassName("org.postgresql.Driver");

        // Pool sizing
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(300_000);        // 5 minutes
        config.setConnectionTimeout(10_000);    // 10 seconds
        config.setMaxLifetime(1_800_000);       // 30 minutes

        // Performance and reliability
        config.setConnectionTestQuery("SELECT 1");
        config.setValidationTimeout(5_000);
        config.setLeakDetectionThreshold(60_000); // 1 minute
        config.setPoolName("AppPool");

        // Prepared statement cache
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Connection init SQL
        config.setConnectionInitSql("SET TIME ZONE 'UTC'");

        return new HikariDataSource(config);
    }

    // Spring Boot-style configuration bean
    // @Bean
    public DataSource dataSource() {
        return createDataSource();
    }
}
```

**application.yml** with HikariCP:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/mydb
    username: appuser
    password: secret
    driver-class-name: org.postgresql.Driver
    hikari:
      pool-name: SpringPool
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 10000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
      connection-test-query: SELECT 1
      data-source-properties:
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        cachePrepStmts: true
        useServerPrepStmts: true
```

## Database Metadata

```java
public class DatabaseMetadataExample {

    public void printDatabaseInfo(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();

            System.out.println("=== Database Info ===");
            System.out.println("Product: " + meta.getDatabaseProductName());
            System.out.println("Version: " + meta.getDatabaseProductVersion());
            System.out.println("Driver: " + meta.getDriverName() + " v" + meta.getDriverVersion());
            System.out.println("JDBC URL: " + meta.getURL());
            System.out.println("Username: " + meta.getUserName());

            System.out.println("\n=== Features ===");
            System.out.println("Supports transactions: " + meta.supportsTransactions());
            System.out.println("Supports savepoints: " + meta.supportsSavepoints());
            System.out.println("Supports batch updates: " + meta.supportsBatchUpdates());
            System.out.println("Max connections: " + meta.getMaxConnections());
            System.out.println("Max row size: " + meta.getMaxRowSize() + " bytes");

            System.out.println("\n=== Tables ===");
            try (ResultSet tables = meta.getTables(null, "public", "%", new String[]{"TABLE"})) {
                while (tables.next()) {
                    System.out.println("  - " + tables.getString("TABLE_NAME")
                        + " (" + tables.getString("TABLE_TYPE") + ")");
                }
            }

            System.out.println("\n=== Columns (users table) ===");
            try (ResultSet columns = meta.getColumns(null, "public", "users", "%")) {
                while (columns.next()) {
                    System.out.printf("  %s: %s(%d) nullable=%s%n",
                        columns.getString("COLUMN_NAME"),
                        columns.getString("TYPE_NAME"),
                        columns.getInt("COLUMN_SIZE"),
                        columns.getString("IS_NULLABLE"));
                }
            }

            System.out.println("\n=== Primary Keys (users table) ===");
            try (ResultSet pk = meta.getPrimaryKeys(null, "public", "users")) {
                while (pk.next()) {
                    System.out.println("  PK: " + pk.getString("COLUMN_NAME")
                        + " (seq: " + pk.getInt("KEY_SEQ") + ")");
                }
            }

            System.out.println("\n=== Foreign Keys ===");
            try (ResultSet fk = meta.getImportedKeys(null, "public", "orders")) {
                while (fk.next()) {
                    System.out.printf("  %s -> %s.%s%n",
                        fk.getString("FKCOLUMN_NAME"),
                        fk.getString("PKTABLE_NAME"),
                        fk.getString("PKCOLUMN_NAME"));
                }
            }
        }
    }
}
```
