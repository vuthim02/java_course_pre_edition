# NoSQL: MongoDB, Redis, Cassandra

NoSQL databases fill specialized roles: MongoDB for document storage, Redis for caching and pub/sub, Cassandra for wide-column high-throughput writes. Spring Data provides consistent repository abstractions for each.

## MongoDB with Native Driver

```xml
<dependency>
    <groupId>org.mongodb</groupId>
    <artifactId>mongodb-driver-sync</artifactId>
    <version>4.11.1</version>
</dependency>
```

```java
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.ArrayList;
import java.util.List;
import static com.mongodb.client.model.Filters.*;

public class MongoNativeExample {

    private final MongoCollection<Document> users;

    public MongoNativeExample() {
        MongoClient client = MongoClients.create("mongodb://localhost:27017");
        MongoDatabase db = client.getDatabase("enterprise_app");
        this.users = db.getCollection("users");
    }

    // Create
    public Document createUser(String name, String email, int age) {
        Document doc = new Document("name", name)
            .append("email", email)
            .append("age", age)
            .append("createdAt", new java.util.Date());
        users.insertOne(doc);
        return doc;
    }

    public void createMany(List<Document> documents) {
        users.insertMany(documents);
    }

    // Read
    public Document findByEmail(String email) {
        return users.find(eq("email", email)).first();
    }

    public List<Document> findByName(String name) {
        return users.find(eq("name", name)).into(new ArrayList<>());
    }

    public List<Document> findAdults(int minAge) {
        return users.find(gte("age", minAge))
            .sort(Sorts.descending("age"))
            .limit(10)
            .into(new ArrayList<>());
    }

    public List<Document> findWithPagination(int page, int size) {
        return users.find()
            .skip(page * size)
            .limit(size)
            .into(new ArrayList<>());
    }

    // Update
    public Document updateEmail(String oldEmail, String newEmail) {
        return users.findOneAndUpdate(
            eq("email", oldEmail),
            Updates.set("email", newEmail));
    }

    public long updateAgeByName(String name, int newAge) {
        return users.updateMany(
            eq("name", name),
            Updates.set("age", newAge)).getModifiedCount();
    }

    // Delete
    public boolean deleteByEmail(String email) {
        return users.deleteOne(eq("email", email)).getDeletedCount() > 0;
    }

    public long deleteInactiveUsers(int maxAge) {
        return users.deleteMany(lt("age", maxAge)).getDeletedCount();
    }

    // Aggregation pipeline
    public List<Document> aggregateByAge() {
        return users.aggregate(List.of(
            Aggregates.group("$age", Accumulators.sum("count", 1)),
            Aggregates.sort(Sorts.descending("count"))
        )).into(new ArrayList<>());
    }
}
```

## MongoDB with Spring Data

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-mongodb</artifactId>
</dependency>
```

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/enterprise_app
      auto-index-creation: true
```

```java
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "users")
public class MongoUser {

    @Id
    private String id;

    @Indexed(unique = true)
    private String email;

    @Field("full_name")
    private String name;

    private int age;

    private Address address;

    private List<String> roles;

    private LocalDateTime createdAt = LocalDateTime.now();

    // getters / setters
}

@Document
public class Address {
    private String street;
    private String city;
    private String zipCode;
}
```

```java
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import java.util.List;

public interface MongoUserRepository extends MongoRepository<MongoUser, String> {

    MongoUser findByEmail(String email);

    List<MongoUser> findByNameContainingIgnoreCase(String name);

    List<MongoUser> findByAgeBetween(int min, int max);

    @Query("{ 'address.city' : ?0 }")
    List<MongoUser> findByCity(String city);

    @Query(value = "{ 'age' : { $gte : ?0 } }", sort = "{ 'name' : 1 }")
    List<MongoUser> findAdultsSortedByName(int minAge);

    boolean existsByEmail(String email);

    long deleteByEmail(String email);
}
```

```java
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Service
public class MongoTemplateService {

    private final MongoTemplate mongoTemplate;

    public MongoTemplateService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public MongoUser updateUserAddress(String email, Address newAddress) {
        Query query = Query.query(Criteria.where("email").is(email));
        Update update = Update.update("address", newAddress);
        return mongoTemplate.findAndModify(query, update,
            FindAndModifyOptions.options().returnNew(true), MongoUser.class);
    }

    public List<MongoUser> complexSearch(String name, Integer minAge, String city) {
        Query query = new Query();
        if (name != null) {
            query.addCriteria(Criteria.where("name").regex(name, "i"));
        }
        if (minAge != null) {
            query.addCriteria(Criteria.where("age").gte(minAge));
        }
        if (city != null) {
            query.addCriteria(Criteria.where("address.city").is(city));
        }
        return mongoTemplate.find(query, MongoUser.class);
    }
}
```

## Redis with Jedis (Native)

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
</dependency>
```

```java
import redis.clients.jedis.*;

public class RedisJedisExample {

    private final JedisPool pool;

    public RedisJedisExample() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(20);
        config.setMaxIdle(10);
        config.setMinIdle(5);
        config.setTestOnBorrow(true);
        config.setBlockWhenExhausted(true);

        this.pool = new JedisPool(config, "localhost", 6379);
    }

    // String operations
    public void setGetExample() {
        try (Jedis jedis = pool.getResource()) {
            jedis.set("user:100:session", "abc123");
            jedis.expire("user:100:session", 3600); // TTL 1 hour

            String session = jedis.get("user:100:session");
            System.out.println("Session: " + session);

            // Set with TTL in one call
            jedis.setex("otp:user:100", 300, "123456");

            // Atomic increment
            jedis.incr("page:counter");
            jedis.incrBy("page:counter", 5);
        }
    }

    // Data structures
    public void dataStructures() {
        try (Jedis jedis = pool.getResource()) {
            // Lists
            jedis.lpush("queue:notifications", "msg1", "msg2", "msg3");
            String msg = jedis.rpop("queue:notifications");

            // Sets
            jedis.sadd("user:100:permissions", "READ", "WRITE");
            boolean hasWrite = jedis.sismember("user:100:permissions", "WRITE");
            Set<String> permissions = jedis.smembers("user:100:permissions");

            // Sorted Sets (leaderboards)
            jedis.zadd("leaderboard", 100, "player1");
            jedis.zadd("leaderboard", 200, "player2");
            Set<String> topPlayers = jedis.zrevrange("leaderboard", 0, 9);

            // Hashes
            jedis.hset("user:100:profile", "name", "Alice");
            jedis.hset("user:100:profile", "email", "alice@example.com");
            String name = jedis.hget("user:100:profile", "name");
            Map<String, String> profile = jedis.hgetAll("user:100:profile");
        }
    }

    // Pub/Sub
    public void publishSubscribe() {
        // Subscriber (runs in separate thread)
        new Thread(() -> {
            try (Jedis jedis = pool.getResource()) {
                jedis.subscribe(new JedisPubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        System.out.println("Received: " + message);
                    }
                }, "events");
            }
        }).start();

        // Publisher
        try (Jedis jedis = pool.getResource()) {
            jedis.publish("events", "User logged in");
            jedis.publish("events", "Order placed");
        }
    }

    public void close() {
        pool.close();
    }
}
```

## Redis with Spring Data

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 16
          max-idle: 8
          min-idle: 4
```

```java
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.concurrent.TimeUnit;

@Service
public class RedisTemplateService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ValueOperations<String, Object> valueOps;
    private final ListOperations<String, Object> listOps;
    private final SetOperations<String, Object> setOps;
    private final ZSetOperations<String, Object> zSetOps;

    public RedisTemplateService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.valueOps = redisTemplate.opsForValue();
        this.listOps = redisTemplate.opsForList();
        this.setOps = redisTemplate.opsForSet();
        this.zSetOps = redisTemplate.opsForZSet();
    }

    public void cacheObject(String key, Object value, long ttlSeconds) {
        valueOps.set(key, value, ttlSeconds, TimeUnit.SECONDS);
    }

    public Object getObject(String key) {
        return valueOps.get(key);
    }

    public void addToQueue(String queue, Object item) {
        listOps.leftPush(queue, item);
    }

    public Object popFromQueue(String queue) {
        return listOps.rightPop(queue);
    }

    public void addToSet(String key, Object... members) {
        setOps.add(key, members);
    }

    public boolean isMember(String key, Object member) {
        return setOps.isMember(key, member);
    }
}
```

```java
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;
import javax.persistence.Id;

@RedisHash("sessions")
public class UserSession {

    @Id
    private String id; // will be used as key: sessions:<id>

    @Indexed
    private Long userId;

    private String token;

    private long expiresAt;

    // getters / setters
}
```

```java
import org.springframework.data.repository.CrudRepository;

public interface SessionRepository extends CrudRepository<UserSession, String> {
    UserSession findByUserId(Long userId);
}
```

## Cassandra with Spring Data

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-cassandra</artifactId>
</dependency>
```

```yaml
spring:
  cassandra:
    contact-points: localhost:9042
    local-datacenter: datacenter1
    keyspace-name: enterprise_app
    schema-action: CREATE_IF_NOT_EXISTS
```

```java
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.data.cassandra.core.mapping.Column;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("events")
public class AppEvent {

    @PrimaryKey
    private UUID id = UUID.randomUUID();

    @Column("event_type")
    private String eventType;

    @Column("user_id")
    private String userId;

    @Column("payload")
    private String payload;

    @Column("created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // getters / setters
}
```

```java
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends CassandraRepository<AppEvent, UUID> {

    List<AppEvent> findByEventType(String eventType);

    @Query("SELECT * FROM events WHERE event_type = ?0 AND created_at > ?1 ALLOW FILTERING")
    List<AppEvent> findRecentByType(String eventType, LocalDateTime since);
}
```

**Native CQL session:**

```java
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.*;

@Service
public class CassandraNativeService {

    private final CqlSession session;

    public CassandraNativeService(CqlSession session) {
        this.session = session;
    }

    public void insertEvent(String type, String userId, String payload) {
        PreparedStatement ps = session.prepare(
            "INSERT INTO events (id, event_type, user_id, payload, created_at) " +
            "VALUES (uuid(), ?, ?, ?, toTimestamp(now()))");

        BoundStatement bound = ps.bind(type, userId, payload);
        session.execute(bound);
    }

    public List<Row> findByType(String type) {
        ResultSet rs = session.execute(
            SimpleStatement.newInstance(
                "SELECT * FROM events WHERE event_type = ? ALLOW FILTERING", type));
        return rs.all();
    }

    public long countByType(String type) {
        ResultSet rs = session.execute(
            SimpleStatement.newInstance(
                "SELECT COUNT(*) FROM events WHERE event_type = ? ALLOW FILTERING", type));
        Row row = rs.one();
        return row != null ? row.getLong(0) : 0;
    }
}
```
