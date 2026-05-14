# Enterprise Engineering — Lesson 10: Spring Security

## What is Spring Security?

Spring Security handles **authentication** (who are you?) and **authorization** (what can you do?).

```
Request ──▶ Security Filter Chain ──▶ Controller
                │
                ▼
    ┌───────────────────────┐
    │ Authentication        │
    │ Provider              │
    │ (Username/Password,   │
    │  JWT, OAuth2, LDAP)   │
    └───────────────────────┘
```

## Basic Setup

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                         UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## JWT Authentication

### Dependencies

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.3</version>
    <scope>runtime</scope>
</dependency>
```

### JWT Service

```java
@Component
public class JwtService {
    private static final String SECRET_KEY = "your-256-bit-secret-key-here-must-be-long-enough";
    private static final long EXPIRATION = 86400000; // 24 hours

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        return resolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }
}
```

### JWT Authentication Filter

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String username = jwtService.extractUsername(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
```

### Auth Controller

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        User user = userService.register(request);
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        authManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.email(), request.password()));
        UserDetails user = userService.loadUserByUsername(request.email());
        String token = jwtService.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(token));
    }
}

record AuthRequest(String email, String password) {}
record AuthResponse(String token) {}
```

## Role-Based Access Control (RBAC)

```java
// Entity
@Entity
public class User implements UserDetails {
    @Id
    @GeneratedValue
    private Long id;

    private String name;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;  // USER, ADMIN, MODERATOR

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}

// Controller
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userService.findAll();
    }

    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
```

## Method-Level Security

```java
@EnableMethodSecurity  // Enable @PreAuthorize, @PostAuthorize, etc.
public class SecurityConfig { ... }

@Service
public class DocumentService {

    @PreAuthorize("hasRole('ADMIN')")
    public Document create(Document doc) { ... }

    @PreAuthorize("hasRole('USER') and #doc.owner == authentication.name")
    public Document update(@Param("doc") Document doc) { ... }

    @PostAuthorize("returnObject.owner == authentication.name")
    public Document findById(Long id) { ... }
}
```

## Common Security Headers

Spring Security adds these automatically:

```
X-Content-Type-Options: nosniff
X-Frame-Options: DENY
Strict-Transport-Security: max-age=31536000
Cache-Control: no-cache, no-store, max-age=0, must-revalidate
```

## OWASP Top 10 for Java Developers

### A01: Broken Access Control
Spring Security method security with `@PreAuthorize`, `@PostAuthorize`, `@PreFilter`, `@PostFilter`:

```java
@EnableMethodSecurity
public class SecurityConfig {}

@Service
public class DocumentService {
    @PreAuthorize("hasRole('ADMIN')")
    public Document create(Document doc) { ... }

    @PreAuthorize("hasRole('USER') and #doc.owner == authentication.name")
    public Document update(@Param("doc") Document doc) { ... }

    @PostAuthorize("returnObject.owner == authentication.name")
    public Document findById(Long id) { ... }

    @PostFilter("filterObject.owner == authentication.name")
    public List<Document> findAll() { ... }
}
```

Use Spring Security ACL for domain-object-level permissions.

### A02: Cryptographic Failures
Password hashing with BCrypt or Argon2:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12); // strength 12
}

// For maximum security:
// return Argon2PasswordEncoder(16, 2, 1, 32, 256);
```

Transport Layer Security — enforce TLS in Spring Boot:

```properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=changeme
server.ssl.key-store-type=PKCS12
```

Never store passwords in plaintext, use strong random salts, avoid ECB mode, use AES-GCM for encryption.

### A03: Injection
SQL injection prevention with JPA/Hibernate parameterized queries:

```java
// SAFE — parameterized query
@Query("SELECT u FROM User u WHERE u.email = :email")
Optional<User> findByEmail(@Param("email") String email);

// SAFE — JPA Criteria API
CriteriaBuilder cb = entityManager.getCriteriaBuilder();
CriteriaQuery<User> cq = cb.createQuery(User.class);
Root<User> root = cq.from(User.class);
cq.select(root).where(cb.equal(root.get("email"), email));

// DANGEROUS — never concatenate user input into JPQL/SQL
// @Query("SELECT u FROM User u WHERE u.email = '" + userInput + "'")
```

For NoSQL injections (MongoDB), use `@Query` with parameter binding, not string concatenation.

### A04: Insecure Design
Rate limiting with Spring Boot + Bucket4j or Resilience4j:

```java
// Bucket4j rate limiter
@Bean
public Bucket bucket() {
    return Bucket.builder()
        .addLimit(Bandwidth.simple(10, Duration.ofSeconds(1)))
        .build();
}

// Resilience4j rate limiter
@RateLimiter(name = "apiRateLimiter", fallbackMethod = "fallback")
@GetMapping("/api/orders")
public List<Order> getOrders() { ... }
```

Input validation with `@Valid` and Bean Validation:

```java
public record CreateUserRequest(
    @NotBlank @Email String email,
    @Size(min = 8, max = 100) String password,
    @Pattern(regexp = "^[0-9]{10}$") String phoneNumber
) {}
```

### A05: Security Misconfiguration
Disable default credentials, verbose errors, and insecure CORS:

```java
@Configuration
public class SecurityMisconfigFix {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Only for stateless APIs
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .headers(headers -> headers
                .frameOptions(f -> f.deny())
                .xssProtection(xss -> xss.enable())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true))
            );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://myapp.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

Disable HTTP endpoints in production:
```properties
management.endpoints.web.exposure.include=health,info
```

### A06: Vulnerable & Outdated Components
Supply chain security with SBOM, Dependabot, and OWASP Dependency Check:

```xml
<!-- OWASP Dependency Check Maven Plugin -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>9.0.9</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
    </configuration>
</plugin>
```

```bash
mvn dependency-check:check
```

Generate CycloneDX SBOM:
```xml
<plugin>
    <groupId>org.cyclonedx</groupId>
    <artifactId>cyclonedx-maven-plugin</artifactId>
    <version>2.7.6</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals><goal>makeBom</goal></goals>
        </execution>
    </executions>
</plugin>
```

### A07: Identification & Authentication Failures
Session management and JWT best practices:

```java
// JWT with short-lived access + long-lived refresh tokens
public record TokenPair(String accessToken, String refreshToken) {}

@Component
public class SecureJwtService {
    private static final long ACCESS_EXPIRY = 900000;      // 15 minutes
    private static final long REFRESH_EXPIRY = 604800000;   // 7 days

    public TokenPair generateTokenPair(UserDetails user) {
        String access = Jwts.builder()
            .subject(user.getUsername())
            .claim("roles", user.getAuthorities())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + ACCESS_EXPIRY))
            .signWith(getSigningKey())
            .compact();

        String refresh = Jwts.builder()
            .subject(user.getUsername())
            .claim("type", "refresh")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRY))
            .signWith(getSigningKey())
            .compact();

        return new TokenPair(access, refresh);
    }
}
```

Session fixation protection — Spring Security changes session ID on auth by default.

### A08: Software & Data Integrity Failures
Serialization filtering in Java:

```java
// JVM-wide deserialization filter
-Djdk.serialFilter="maxdepth=10;maxarray=10000;maxbytes=500000;!org.example.*"

// Programmatic filter
ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(
    "maxdepth=10;java.base/*;!*"
);
ObjectInputStream ois = new ObjectInputStream(inputStream);
ois.setObjectInputFilter(filter);
```

Avoid Java serialization for untrusted data. Use JSON/Protobuf instead.

### A09: Security Logging & Monitoring Failures
Audit logging with Spring AOP and PII redaction:

```java
@Aspect
@Component
public class AuditAspect {
    private static final Logger log = LoggerFactory.getLogger("AUDIT");

    @AfterReturning("@annotation(auditable)", returning = "result")
    public void logAudit(JoinPoint joinPoint, Auditable auditable, Object result) {
        String method = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        // Redact PII before logging
        Object[] sanitized = Arrays.stream(args)
            .map(arg -> arg instanceof String ? "***" : arg)
            .toArray();
        log.info("AUDIT: method={} args={} result={}", method, sanitized, result);
    }
}

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {}
```

Never log passwords, tokens, credit cards, or personal data. Use Logback's `MaskingMessageConverter` for pattern-based redaction.

### A10: Server-Side Request Forgery (SSRF)
URL validation and network policies:

```java
@Component
public class SafeHttpClient {
    private static final List<String> ALLOWED_HOSTS = List.of(
        "api.trusted.com", "internal.service.local"
    );
    private static final List<String> BLOCKED_RANGES = List.of(
        "169.254.169.254",  // AWS metadata
        "10.", "172.16.", "192.168."  // RFC 1918
    );

    public String fetchUrl(String urlString) {
        URI uri = URI.create(urlString);
        String host = uri.getHost();

        if (ALLOWED_HOSTS.stream().noneMatch(host::endsWith)) {
            throw new SecurityException("Host not allowed: " + host);
        }

        String ip = InetAddress.getByName(host).getHostAddress();
        for (String blocked : BLOCKED_RANGES) {
            if (ip.startsWith(blocked)) {
                throw new SecurityException("Blocked IP range: " + ip);
            }
        }

        return restTemplate.getForObject(urlString, String.class);
    }
}
```

Use `RestTemplate` with connection timeouts and restrict outbound network access via security groups.

---

### Exercises

1. Set up Spring Security with JWT authentication (register + login endpoints).
2. Implement role-based access with USER and ADMIN roles.
3. Add method-level security with `@PreAuthorize`.
4. Create a refresh token mechanism.
5. Test authenticated endpoints with Postman: add `Authorization: Bearer <token>` header.
