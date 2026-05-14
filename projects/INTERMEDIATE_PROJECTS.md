# Intermediate Projects

## Project 1: REST API (Spring Boot)

**Concepts:** Spring Boot, JPA, REST, Validation, Error Handling

```
POST /api/users        → Create user
GET  /api/users        → List users (paginated)
GET  /api/users/{id}   → Get user by ID
PUT  /api/users/{id}   → Update user
DELETE /api/users/{id} → Delete user
```

**Architecture:**
```
src/main/java/com/example/api/
├── controller/UserController.java
├── service/UserService.java
├── repository/UserRepository.java
├── model/User.java
├── dto/UserRequest.java, UserResponse.java
├── exception/
│   ├── ResourceNotFoundException.java
│   └── GlobalExceptionHandler.java
└── config/SecurityConfig.java
```

## Project 2: Authentication System

**Concepts:** Spring Security, JWT, OAuth2, RBAC

- Register with email/password
- Login → JWT token
- JWT validation filter
- Role-based access (USER, ADMIN, MODERATOR)
- Password reset flow
- Email verification

## Project 3: E-commerce Backend

**Concepts:** Complex entities, relationships, pagination, caching

- Products (with categories, search, filtering)
- Shopping cart
- Orders (with order items, status workflow)
- User management
- Admin dashboard endpoints
- Payment integration (Stripe/PayPal mock)

**Entity Relationships:**
```
User 1──N Order 1──N OrderItem N──1 Product
User 1──1 Cart 1──N CartItem N──1 Product
Product N──M Category
```

## Project 4: Social Media Backend

**Concepts:** Feeds, friendships, notifications, file upload

- User profiles
- Friendships (request/accept/reject)
- Posts (create, like, comment, share)
- News feed (from friends)
- Notifications
- Image upload (profile, posts)

## Project 5: Real-time Chat System

**Concepts:** WebSockets, STOMP, Redis Pub/Sub

- WebSocket-based messaging
- Chat rooms
- Online status
- Message persistence
- Typing indicators
- File sharing
