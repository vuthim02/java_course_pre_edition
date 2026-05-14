# Spring Security: JWT, OAuth2, Method Security

Spring Security provides authentication, authorization, and protection against common attacks. JWT-based stateless authentication is common for REST APIs. Method-level security with `@PreAuthorize` enables fine-grained access control.

## SecurityFilterChain Configuration

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize, @PostAuthorize, @Secured
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthenticationEntryPoint authEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtAuthenticationEntryPoint authEntryPoint) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.authEntryPoint = authEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // REST APIs are stateless; CSRF not needed
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint))

            // Stateless session — no JSESSIONID
            .sessionManagement(sm -> sm
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Route authorization
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())

            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "https://app.example.com",
            "http://localhost:3000"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("X-Request-Id"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

## UserDetailsService and UserDetails

```java
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
            .map(SecurityUser::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
    }

    // Custom UserDetails implementation
    public record SecurityUser(User user) implements UserDetails {

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toSet());
        }

        @Override
        public String getPassword() { return user.getPassword(); }

        @Override
        public String getUsername() { return user.getEmail(); }

        @Override
        public boolean isAccountNonExpired() { return true; }

        @Override
        public boolean isAccountNonLocked() { return !user.isLocked(); }

        @Override
        public boolean isCredentialsNonExpired() { return true; }

        @Override
        public boolean isEnabled() { return user.isActive(); }
    }
}
```

## PasswordEncoder

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Map;

@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt with strength 12 (higher = slower but more secure)
        return new BCryptPasswordEncoder(12);

        // DelegatingPasswordEncoder for multiple encoding schemes:
//        return new DelegatingPasswordEncoder("bcrypt", Map.of(
//            "bcrypt", new BCryptPasswordEncoder(),
//            "scrypt", new SCryptPasswordEncoder(),
//            "argon2", new Argon2PasswordEncoder()
//        ));
    }
}
```

```java
@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AuthService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(Role.USER));

        return userRepository.save(user);
    }
}
```

## JWT Token Generation and Validation

```java
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-token-expiration:3600000}") long accessExp,
            @Value("${app.jwt.refresh-token-expiration:604800000}") long refreshExp) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessExp;
        this.refreshTokenExpiration = refreshExp;
    }

    public String generateAccessToken(String email, List<String> roles) {
        Date now = new Date();
        return Jwts.builder()
            .subject(email)
            .claim("roles", roles)
            .issuedAt(now)
            .expiration(new Date(now.getTime() + accessTokenExpiration))
            .signWith(secretKey)
            .compact();
    }

    public String generateRefreshToken(String email) {
        Date now = new Date();
        return Jwts.builder()
            .subject(email)
            .claim("type", "refresh")
            .issuedAt(now)
            .expiration(new Date(now.getTime() + refreshTokenExpiration))
            .signWith(secretKey)
            .compact();
    }

    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        return parseClaims(token).get("roles", List.class);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
```

## JWT Authentication Filter (OncePerRequestFilter)

```java
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            if (tokenProvider.validateToken(token)) {
                String email = tokenProvider.getEmailFromToken(token);

                var authorities = tokenProvider.getRolesFromToken(token)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                        email, null, authorities);
                authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
```

```java
@Component
public class JwtAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException)
            throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String json = """
            {"error": "Unauthorized", "message": "%s"}
            """.formatted(authException.getMessage());

        response.getWriter().write(json);
    }
}
```

## Authentication Controller

```java
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepo;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        String email = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        String accessToken = tokenProvider.generateAccessToken(email, roles);
        String refreshToken = tokenProvider.generateRefreshToken(email);

        refreshTokenRepo.save(new RefreshToken(email, refreshToken));

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, roles));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshRequest request) {
        // Validate refresh token
        if (!tokenProvider.validateToken(request.refreshToken())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = tokenProvider.getEmailFromToken(request.refreshToken());

        // Verify refresh token exists in DB
        refreshTokenRepo.findByToken(request.refreshToken())
            .orElseThrow(() -> new BadRequestException("Invalid refresh token"));

        // Generate new tokens
        List<String> roles = tokenProvider.getRolesFromToken(request.refreshToken());
        String newAccessToken = tokenProvider.generateAccessToken(email, roles);
        String newRefreshToken = tokenProvider.generateRefreshToken(email);

        // Rotate refresh token
        refreshTokenRepo.deleteByEmail(email);
        refreshTokenRepo.save(new RefreshToken(email, newRefreshToken));

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, roles));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(UserResponse.from(user));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        refreshTokenRepo.deleteByToken(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
}

public record AuthResponse(
    String accessToken,
    String refreshToken,
    List<String> roles
) {}
```

## Method-Level Security

```java
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreFilter;
import jakarta.annotation.security.RolesAllowed;

@Service
public class SecureOrderService {

    // Only users with ADMIN role can call this
    @PreAuthorize("hasRole('ADMIN')")
    public void cancelAnyOrder(Long orderId) {
        // ...
    }

    // Only the owner or an admin can view
    @PostAuthorize("returnObject.customerEmail == authentication.name or hasRole('ADMIN')")
    public OrderResponse getOrder(Long orderId) {
        return orderRepository.findById(orderId)
            .map(OrderResponse::from)
            .orElse(null);
    }

    // SpEL: user must have permission
    @PreAuthorize("@orderSecurity.canModifyOrder(#orderId, authentication)")
    public void updateOrder(Long orderId, UpdateOrderRequest request) {
        // ...
    }

    // Filter returned collection — only return items the user owns
    @PostFilter("filterObject.owner == authentication.name")
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll();
    }

    // Filter input collection — only process items user owns
    @PreFilter("filterObject.owner == authentication.name")
    public void processDocuments(List<DocumentRequest> docs) {
        // ...
    }

    // Multiple conditions
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER') and #request.amount > 0")
    public void approveRefund(RefundRequest request) {
        // ...
    }

    // @Secured (simpler, doesn't support SpEL)
    @Secured("ROLE_ADMIN")
    public void deleteUser(Long userId) {
        // ...
    }

    // @RolesAllowed (JSR-250)
    @RolesAllowed({"ADMIN", "MANAGER"})
    public void generateReport() {
        // ...
    }
}
```

**Custom permission evaluator:**

```java
@Component("orderSecurity")
public class OrderSecurityEvaluator {

    public boolean canModifyOrder(Long orderId, Authentication auth) {
        // Load order and check ownership
        return orderRepository.findById(orderId)
            .map(order -> order.getCustomerEmail().equals(auth.getName())
                || auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")))
            .orElse(false);
    }
}
```

## CORS and CSRF

```java
// CORS is configured in SecurityFilterChain (see above)
// Key principles:
// - Development: allow localhost origins
// - Production: restrict to specific domains
// - Allow credentials only when needed (cookies, auth headers)
// - Expose custom headers that clients need to read

// CSRF explained:
// - Should be ENABLED for browser-based apps using cookies (session auth)
// - Should be DISABLED for REST APIs that use:
//   * Stateless authentication (JWT, Bearer token)
//   * Mobile apps / SPAs that set X-Requested-With header
// - When enabled, include CSRF token in requests:
//   * Fetch from GET /api/v1/csrf
//   * Include as header X-XSRF-TOKEN in state-changing requests

// CSRF when enabled:
@Configuration
public class CsrfSecurityConfig {

    @Bean
    public SecurityFilterChain csrfEnabledFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
}
```

```yaml
app:
  jwt:
    secret: "your-256-bit-secret-key-here-must-be-at-least-256-bits-long-for-hmac"
    access-token-expiration: 3600000     # 1 hour
    refresh-token-expiration: 604800000  # 7 days
  cors:
    allowed-origins: https://app.example.com,http://localhost:3000
```
