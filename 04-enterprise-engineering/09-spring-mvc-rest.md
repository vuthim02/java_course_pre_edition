# Enterprise Engineering — Lesson 9: Spring MVC & REST APIs

## What is Spring MVC?

Spring MVC is the web framework within Spring for building web applications and REST APIs.

```
HTTP Request
     │
     ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Dispatcher    │────▶│   Controller    │────▶│   Service       │
│   Servlet       │     │   (@Controller) │     │   (@Service)    │
│   (Front        │     │   Handles       │     │   Business      │
│    Controller)  │     │   requests      │     │   logic         │
└─────────────────┘     └─────────────────┘     └─────────────────┘
        │                                               │
        ▼                                               ▼
┌─────────────────┐                             ┌─────────────────┐
│   View (HTML)   │                             │   Repository    │
│   OR            │                             │   (@Repository) │
│   JSON Response │                             │   Data access   │
└─────────────────┘                             └─────────────────┘
```

## REST Controller

```java
@RestController  // = @Controller + @ResponseBody (returns JSON)
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users — Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // GET /api/users/{id} — Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/users — Create user
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody @Valid User user) {
        User saved = userService.save(user);
        return ResponseEntity.created(
            URI.create("/api/users/" + saved.getId())
        ).body(saved);
    }

    // PUT /api/users/{id} — Update user
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody @Valid User user) {
        return userService.update(id, user)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/users/{id} — Delete user
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

## Request Mapping Annotations

```java
@GetMapping     // HTTP GET
@PostMapping    // HTTP POST
@PutMapping     // HTTP PUT
@DeleteMapping  // HTTP DELETE
@PatchMapping   // HTTP PATCH
@RequestMapping // Generic — specify method
```

## Request Parameters

```java
// Path variable: /api/users/42
@GetMapping("/{id}")
public User getById(@PathVariable Long id) { ... }

// Query parameter: /api/users?page=0&size=10
@GetMapping
public List<User> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size) { ... }

// Request body (JSON)
@PostMapping
public User create(@RequestBody @Valid User user) { ... }

// Header
@GetMapping
public User getByHeader(@RequestHeader("Authorization") String token) { ... }

// Cookie
@GetMapping
public User getByCookie(@CookieValue("session") String sessionId) { ... }
```

## Validation

```java
// Entity with validation
public class User {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email")
    @NotBlank
    private String email;

    @Min(value = 0, message = "Age must be positive")
    @Max(value = 150, message = "Age must be ≤ 150")
    private int age;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone")
    private String phone;
}

// Controller with validation
@PostMapping
public ResponseEntity<User> create(@RequestBody @Valid User user) {
    // If validation fails, Spring returns 400 with error details
    return ResponseEntity.ok(userService.save(user));
}

// Global exception handler
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}
```

## Error Handling with Problem Details (RFC 7807, Spring Boot 3+)

```java
@RestControllerAdvice
public class ProblemDetailsExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation Error");
        problem.setDetail("Invalid request content");

        // Add field-specific errors
        Map<String, List<String>> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.groupingBy(
                FieldError::getField,
                Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            ));
        problem.setProperty("errors", errors);

        return problem;
    }
}
```

## Testing REST Controllers

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void shouldReturnUsers() throws Exception {
        List<User> users = List.of(new User("Alice"));
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size()").value(1))
            .andExpect(jsonPath("$[0].name").value("Alice"));
    }

    @Test
    void shouldReturn404WhenNotFound() throws Exception {
        when(userService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound());
    }
}
```

---

### Exercises

1. Create a complete REST API for a `Product` entity with full CRUD.
2. Add validation to your entities and handle validation errors gracefully.
3. Write `@WebMvcTest` tests for your controllers using MockMvc.
4. Add pagination and sorting: `?page=0&size=10&sort=name,asc`.
5. Create a global exception handler that returns consistent error responses.
