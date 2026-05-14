# Architecture & Design — Lesson 8: API Design (REST, GraphQL, gRPC)

## Why API Design Matters

Your API is your contract with the world. A well-designed API is intuitive, consistent, and hard to misuse. A bad API confuses, frustrates, and breeds bugs.

```
Bad API:                            Good API:
POST /saveUser                      POST /api/v1/users
{"name":"Alice","age":30}           {"name":"Alice","email":"a@b.com"}

GET /getUser?uid=123                GET /api/v1/users/123
→ {"n":"Alice","a":30,"e":"..."}    → {"name":"Alice","email":"a@b.com",...}

POST /deleteUser/123                DELETE /api/v1/users/123
→ "ok"                              → 204 No Content

❌ Inconsistent naming              ✅ RESTful, consistent
❌ Abbreviated fields                ✅ Clear, full names
❌ Wrong HTTP methods                ✅ Correct HTTP semantics
```

## REST API Design

### Resource Naming

```java
// ✅ GOOD — resources, not actions
GET    /api/v1/users                    // List users
POST   /api/v1/users                    // Create user
GET    /api/v1/users/{id}               // Get user by ID
PUT    /api/v1/users/{id}               // Replace user
PATCH  /api/v1/users/{id}               // Partially update user
DELETE /api/v1/users/{id}               // Delete user

GET    /api/v1/users/{id}/orders        // User's orders
GET    /api/v1/orders/{id}              // Specific order
GET    /api/v1/orders/{id}/items        // Order's items

// ❌ BAD — verbs in URL
/getAllUsers
/createUser
/deleteUserById
/userOrders?uid=123
/orders/getItems
```

### HTTP Methods

| Method | Action | Idempotent | Safe | Body | Response |
|--------|--------|------------|------|------|----------|
| GET | Retrieve | Yes | Yes | No | 200 OK |
| POST | Create | No | No | Yes | 201 Created |
| PUT | Replace | Yes | No | Yes | 200 OK |
| PATCH | Partial update | No | No | Yes | 200 OK |
| DELETE | Delete | Yes | No | No | 204 No Content |

### Status Codes

```java
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);  // 200
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)                      // 200
            .orElse(ResponseEntity.notFound().build());    // 404
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/users/" + created.id()))  // 201
            .body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();  // 204
    }
}
```

### Common Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 OK | Success | GET, PUT, PATCH |
| 201 Created | Created | POST |
| 204 No Content | Deleted | DELETE |
| 400 Bad Request | Invalid input | Validation failure |
| 401 Unauthorized | Not authenticated | Missing/invalid token |
| 403 Forbidden | Not authorized | Insufficient permissions |
| 404 Not Found | Resource doesn't exist | Wrong ID |
| 409 Conflict | State conflict | Duplicate, version conflict |
| 422 Unprocessable | Business rule violation | Order can't be cancelled |
| 429 Too Many Requests | Rate limited | Too many requests |
| 500 Internal Server Error | Server error | Unexpected exception |

### Request/Response Format

```java
// Request — use records for immutability
public record CreateUserRequest(
    @NotBlank String name,
    @Email String email,
    @Min(18) int age
) {}

// Response — separate from entity, hide internal details
public record UserResponse(
    Long id,
    String name,
    String email,
    int age,
    LocalDateTime createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(), user.getName(),
            user.getEmail(), user.getAge(),
            user.getCreatedAt()
        );
    }
}

// Error response — consistent format
public record ErrorResponse(
    int status,
    String error,
    String message,
    String path,
    LocalDateTime timestamp
) {}
```

### Pagination

```java
@GetMapping
public ResponseEntity<PageResponse<UserResponse>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String sort) {

    Pageable pageable = PageRequest.of(page, size,
        sort != null ? Sort.by(sort) : Sort.unsorted());

    Page<UserResponse> result = userService.findAll(pageable);

    return ResponseEntity.ok(new PageResponse<>(
        result.getContent(),
        result.getNumber(),
        result.getSize(),
        result.getTotalElements(),
        result.getTotalPages(),
        result.hasNext()
    ));
}

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {}
```

### HATEOAS (Optional)

```json
{
  "id": 123,
  "name": "Alice",
  "_links": {
    "self": { "href": "/api/v1/users/123" },
    "orders": { "href": "/api/v1/users/123/orders" },
    "update": { "href": "/api/v1/users/123", "method": "PUT" }
  }
}
```

## GraphQL

GraphQL lets clients request EXACTLY the data they need — no more, no less.

```
REST:                              GraphQL:
GET /api/v1/users/123              POST /graphql
→ {                                → {
    "id": 123,                        "query": "{ user(id: 123) { name email } }"
    "name": "Alice",                }
    "email": "a@b.com",           → {
    "age": 30,                        "data": { "user": { "name": "Alice", "email": "a@b.com" } }
    "address": "...",              }
    "orders": [...],
    "createdAt": "...",           ✅ Only requested fields returned
    "updatedAt": "...",           ✅ Single endpoint
  }                                ✅ No over-fetching, no under-fetching

❌ Always returns ALL fields
❌ Different endpoints for different needs
```

### Schema Definition

```graphql
type User {
    id: ID!
    name: String!
    email: String!
    age: Int
    orders: [Order!]!
    createdAt: String!
}

type Order {
    id: ID!
    total: Float!
    status: OrderStatus!
    items: [OrderItem!]!
    createdAt: String!
}

type OrderItem {
    product: Product!
    quantity: Int!
    price: Float!
}

enum OrderStatus {
    PENDING, PAID, SHIPPED, DELIVERED, CANCELLED
}

type Query {
    user(id: ID!): User
    users(page: Int, size: Int): [User!]!
    order(id: ID!): Order
}

type Mutation {
    createUser(input: CreateUserInput!): User!
    updateUser(id: ID!, input: UpdateUserInput!): User!
    deleteUser(id: ID!): Boolean!
}

input CreateUserInput {
    name: String!
    email: String!
    age: Int
}
```

### Spring Boot + GraphQL

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
```

```java
@Controller
public class UserGraphQLController {

    @Autowired
    private UserService userService;

    @QueryMapping
    public User user(@Argument Long id) {
        return userService.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @QueryMapping
    public List<User> users(@Argument int page, @Argument int size) {
        return userService.findAll(PageRequest.of(page, size));
    }

    @MutationMapping
    public User createUser(@Argument CreateUserInput input) {
        return userService.create(input);
    }

    @SchemaMapping  // Resolve field on User type
    public List<Order> orders(User user) {
        return orderService.findByUserId(user.getId());
    }
}
```

## gRPC

gRPC uses Protocol Buffers and HTTP/2 for high-performance, typed API calls.

```
REST:                              gRPC:
JSON (text)                        Protocol Buffers (binary)
HTTP/1.1                           HTTP/2 (multiplexed)
Request-Response                   Request-Response + Streaming
No contract enforcement            Strict schema (proto files)
Slower serialization               Fast binary serialization
```

### Proto Definition

```protobuf
syntax = "proto3";

service UserService {
    rpc GetUser (GetUserRequest) returns (User);
    rpc ListUsers (ListUsersRequest) returns (stream User);  // Server-streaming
    rpc CreateUser (CreateUserRequest) returns (User);
    rpc UpdateUsers (stream UpdateUserRequest) returns (UserSummary);  // Client-streaming
}

message GetUserRequest {
    int64 id = 1;
}

message User {
    int64 id = 1;
    string name = 2;
    string email = 3;
    int32 age = 4;
    repeated Order orders = 5;
}

message Order {
    int64 id = 1;
    double total = 2;
    string status = 3;
}
```

### gRPC Service in Java

```java
@GrpcService
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {

    @Autowired
    private UserService userService;

    @Override
    public void getUser(GetUserRequest request, StreamObserver<User> responseObserver) {
        userService.findById(request.getId())
            .ifPresentOrElse(
                user -> {
                    responseObserver.onNext(toProto(user));
                    responseObserver.onCompleted();
                },
                () -> responseObserver.onError(
                    Status.NOT_FOUND.withDescription("User not found").asRuntimeException()
                )
            );
    }

    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<User> responseObserver) {
        userService.findAll().forEach(user -> {
            responseObserver.onNext(toProto(user));
        });
        responseObserver.onCompleted();
    }

    private User toProto(com.example.User user) {
        return User.newBuilder()
            .setId(user.getId())
            .setName(user.getName())
            .setEmail(user.getEmail())
            .setAge(user.getAge())
            .build();
    }
}
```

## Comparison

| Aspect | REST | GraphQL | gRPC |
|--------|------|---------|------|
| **Data format** | JSON/XML | JSON | Protocol Buffers (binary) |
| **HTTP version** | HTTP/1.1 | HTTP/1.1 | HTTP/2 |
| **Contract** | Informal (docs) | Schema (SDL) | Strict (proto) |
| **Over-fetching** | Common | None | Avoidable |
| **Under-fetching** | Common (multiple calls) | None | Avoidable |
| **Streaming** | SSE/WebSocket | Subscriptions | Native bi-directional |
| **Performance** | Moderate | Moderate | Excellent |
| **Browser support** | Native | Native | Needs proxy (gRPC-Web) |
| **Tooling** | Excellent | Good | Good |
| **Learning curve** | Low | Medium | High |
| **Best for** | Public APIs, CRUD | Complex/mobile UIs | Internal microservices |

## API Design Checklist

- [ ] Consistent naming (nouns, plural, lowercase)
- [ ] Correct HTTP methods (GET/POST/PUT/PATCH/DELETE)
- [ ] Proper status codes (201 for create, 404 for not found, etc.)
- [ ] Meaningful error messages with consistent format
- [ ] Pagination for list endpoints
- [ ] Versioning (URL or header-based)
- [ ] Authentication & authorization on every endpoint
- [ ] Rate limiting to protect against abuse
- [ ] Input validation (all inputs are evil until proven safe)
- [ ] Idempotency for mutating operations
- [ ] Documentation (OpenAPI/Swagger for REST)
- [ ] No sensitive data in responses (passwords, tokens)

## Exercises

1. Design a REST API for a task management system (resources, methods, status codes).
2. Implement pagination, sorting, and filtering for a list endpoint.
3. Create a GraphQL schema for the same task management system.
4. Compare REST vs GraphQL — which is better for a mobile app and why?
5. Write a gRPC proto file for a simple service and generate the Java stubs.
