# Spring MVC: REST APIs

Spring MVC provides annotation-driven REST controllers, content negotiation, validation, and exception handling. Use `ResponseEntity` for fine-grained control over HTTP responses and `@ControllerAdvice` for global error handling.

## REST Controller — CRUD

```java
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.*;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping(params = "email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse created = userService.create(request);
        return ResponseEntity
            .created(URI.create("/api/v1/users/" + created.id()))
            .body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {
        return userService.update(id, request)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> partialUpdate(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {
        return userService.partialUpdate(id, updates)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

## Request DTOs with Validation

```java
public record CreateUserRequest(
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100)
    String name,

    @NotBlank
    @Email(regexp = ".+@.+\\..+", message = "Invalid email format")
    String email,

    @NotNull
    @Min(18) @Max(150)
    Integer age,

    @Pattern(regexp = "\\+?[0-9]{10,15}", message = "Invalid phone number")
    String phone,

    @Future(message = "Subscription end must be in the future")
    LocalDate subscriptionEnd
) {}

public record UpdateUserRequest(
    @Size(min = 2, max = 100)
    String name,

    @Min(18) @Max(150)
    Integer age
) {}

public record UserResponse(
    Long id,
    String name,
    String email,
    Integer age,
    LocalDateTime createdAt
) {}
```

## Global Exception Handler

```java
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate(DuplicateResourceException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Resource already exists");
        return pd;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        List<ValidationError> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(fe -> new ValidationError(
                fe.getField(),
                fe.getDefaultMessage(),
                fe.getRejectedValue()))
            .toList();

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation failed");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", LocalDateTime.now());

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleAllUncaught(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        return pd;
    }

    private record ValidationError(String field, String message, Object rejectedValue) {}
}

// Custom exceptions
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Object id) {
        super("%s not found with id: %s".formatted(resource, id));
    }
}

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) { super(message); }
}

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) { super(message); }
}
```

## ResponseEntity — Fine-Grained Control

```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.placeOrder(request);

        return ResponseEntity
            .created(URI.create("/api/v1/orders/" + order.id()))
            .header("X-Order-Id", order.id().toString())
            .header("X-Processing-Time", "150ms")
            .cacheControl(CacheControl.noCache())
            .body(order);
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportOrders() {
        Resource file = orderService.generateCsvExport();

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(file.contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=orders.csv")
            .body(file);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<OrderStatus> getOrderStatus(@PathVariable Long id) {
        return orderService.getStatus(id)
            .map(status -> ResponseEntity.ok()
                .eTag("\"" + status.version() + "\"")
                .body(status))
            .orElse(ResponseEntity.notFound().build());
    }

    // Conditional request handling (ETag / If-None-Match)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserWithCache(
            @PathVariable Long id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {

        UserResponse user = userService.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        String etag = "\"" + user.version() + "\"";
        if (etag.equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED).build();
        }

        return ResponseEntity.ok()
            .eTag(etag)
            .body(user);
    }
}
```

## Content Negotiation

```yaml
spring:
  mvc:
    contentnegotiation:
      favor-parameter: true
      parameter-name: format
      media-types:
        json: application/json
        xml: application/xml
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: true
    date-format: yyyy-MM-dd'T'HH:mm:ss
```

```java
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    // Content negotiation via Accept header or ?format=xml
    @GetMapping(produces = {
        MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE
    })
    public List<ProductResponse> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping(value = "/{id}", produces = "application/vnd.api.v1+json")
    public ProductResponse getProductV1(@PathVariable Long id) {
        return productService.findById(id);
    }

    @GetMapping(value = "/{id}", produces = "application/vnd.api.v2+json")
    public ProductResponseV2 getProductV2(@PathVariable Long id) {
        return productService.findDetailedById(id);
    }
}
```

## Pagination with Pageable

```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/products")
public class ProductPaginationController {

    @GetMapping
    public Page<ProductResponse> getProducts(
            @PageableDefault(size = 20, sort = "name",
                direction = Sort.Direction.ASC) Pageable pageable) {

        return productService.findAll(pageable);
    }

    @GetMapping("/search")
    public Page<ProductResponse> searchProducts(
            @RequestParam String query,
            @PageableDefault(size = 10) Pageable pageable) {

        return productService.search(query, pageable);
    }

    // Manual Pageable construction
    @GetMapping("/manual")
    public Page<ProductResponse> getProductsManual(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name,asc") String[] sort) {

        Sort sorting = Sort.by(
            java.util.stream.Stream.of(sort)
                .map(s -> {
                    String[] parts = s.split(",");
                    return new Sort.Order(
                        parts.length > 1 && "desc".equalsIgnoreCase(parts[1])
                            ? Sort.Direction.DESC : Sort.Direction.ASC,
                        parts[0]);
                })
                .toList()
        );

        Pageable pageable = PageRequest.of(page, size, sorting);
        return productService.findAll(pageable);
    }
}
```

**Response shape:**
```json
{
  "content": [ ... ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

## HATEOAS Basics

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-hateoas</artifactId>
</dependency>
```

```java
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserHateoasController {

    @GetMapping("/{id}")
    public EntityModel<UserResponse> getUser(@PathVariable Long id) {
        UserResponse user = userService.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User", id));

        return EntityModel.of(user,
            linkTo(methodOn(UserHateoasController.class).getUser(id)).withSelfRel(),
            linkTo(methodOn(UserHateoasController.class).getAllUsers()).withRel("users"),
            linkTo(methodOn(OrderController.class).getOrdersByUser(id)).withRel("orders"));
    }

    @GetMapping
    public CollectionModel<UserResponse> getAllUsers() {
        List<UserResponse> users = userService.findAll();

        return CollectionModel.of(users,
            linkTo(methodOn(UserHateoasController.class).getAllUsers()).withSelfRel());
    }

    @GetMapping("/{userId}/orders")
    public CollectionModel<OrderResponse> getUserOrders(@PathVariable Long userId) {
        List<OrderResponse> orders = orderService.findByUserId(userId);

        return CollectionModel.of(orders,
            linkTo(methodOn(UserHateoasController.class).getUserOrders(userId)).withSelfRel(),
            linkTo(methodOn(UserHateoasController.class).getUser(userId)).withRel("user"));
    }
}
```

**Response:**
```json
{
  "id": 1,
  "name": "Alice",
  "email": "alice@example.com",
  "_links": {
    "self": { "href": "/api/v1/users/1" },
    "users": { "href": "/api/v1/users" },
    "orders": { "href": "/api/v1/users/1/orders" }
  }
}
```
