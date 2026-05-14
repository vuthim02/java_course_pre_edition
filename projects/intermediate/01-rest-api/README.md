# Project 1: Spring Boot REST API — Book Manager

**Concepts:** Spring Boot 3.2, Spring Data JPA, H2/PostgreSQL, DTOs, Validation, Global Exception Handling, Pagination & Sorting

## Project Structure

```
src/main/java/com/example/bookapi/
├── BookApiApplication.java
├── controller/BookController.java
├── service/BookService.java
├── repository/BookRepository.java
├── model/Book.java
├── dto/BookRequest.java
├── dto/BookResponse.java
├── dto/PageResponse.java
├── exception/ResourceNotFoundException.java
├── exception/GlobalExceptionHandler.java
└── config/SecurityConfig.java

src/main/resources/
├── application.yml
└── application-prod.yml
```

## pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.2</version>
        <relativePath/>
    </parent>

    <groupId>com.example</groupId>
    <artifactId>book-api</artifactId>
    <version>1.0.0</version>
    <name>book-api</name>
    <description>Spring Boot REST API - Book Manager</description>

    <properties>
        <java.version>17</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

## application.yml

```yaml
# Default profile: H2 in-memory database for development
spring:
  datasource:
    url: jdbc:h2:mem:bookdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.H2Dialect

server:
  port: 8080

---
# Production profile: PostgreSQL
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/bookdb
    driver-class-name: org.postgresql.Driver
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD:postgres}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
  h2:
    console:
      enabled: false
```

## BookApiApplication.java

```java
package com.example.bookapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BookApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(BookApiApplication.class, args);
    }
}
```

## model/Book.java

```java
package com.example.bookapi.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false, unique = true)
    private String isbn;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    public Book() {}

    public Book(String title, String author, String isbn, BigDecimal price, LocalDate publishedDate) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.price = price;
        this.publishedDate = publishedDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }
}
```

## dto/BookRequest.java

```java
package com.example.bookapi.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookRequest(
    @NotBlank(message = "Title is required")
    String title,

    @NotBlank(message = "Author is required")
    String author,

    @NotBlank(message = "ISBN is required")
    @Pattern(regexp = "^(?:ISBN(?:-1[03])?:? )?(?=[0-9X]{10}$|(?=(?:[0-9]+[- ]){3})[- 0-9X]{13}$|97[89][0-9]{10}$|(?=(?:[0-9]+[- ]){4})[- 0-9]{17}$)(?:97[89][- ]?)?[0-9]{1,5}[- ]?[0-9]+[- ]?[0-9]+[- ]?[0-9X]$",
             message = "Invalid ISBN format")
    String isbn,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    BigDecimal price,

    @NotNull(message = "Published date is required")
    @PastOrPresent(message = "Published date cannot be in the future")
    LocalDate publishedDate
) {}
```

## dto/BookResponse.java

```java
package com.example.bookapi.dto;

import com.example.bookapi.model.Book;
import java.math.BigDecimal;
import java.time.LocalDate;

public record BookResponse(
    Long id,
    String title,
    String author,
    String isbn,
    BigDecimal price,
    LocalDate publishedDate
) {
    public static BookResponse fromEntity(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPrice(),
            book.getPublishedDate()
        );
    }
}
```

## dto/PageResponse.java

```java
package com.example.bookapi.dto;

import java.util.List;

public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {}
```

## repository/BookRepository.java

```java
package com.example.bookapi.repository;

import com.example.bookapi.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(b.author) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Book> search(@Param("q") String query);

    boolean existsByIsbn(String isbn);
}
```

## service/BookService.java

```java
package com.example.bookapi.service;

import com.example.bookapi.dto.BookRequest;
import com.example.bookapi.dto.BookResponse;
import com.example.bookapi.dto.PageResponse;
import com.example.bookapi.exception.ResourceNotFoundException;
import com.example.bookapi.model.Book;
import com.example.bookapi.repository.BookRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(BookRequest request) {
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.isbn() + " already exists");
        }
        Book book = new Book(request.title(), request.author(), request.isbn(),
                             request.price(), request.publishedDate());
        Book saved = bookRepository.save(book);
        return BookResponse.fromEntity(saved);
    }

    @Transactional(readOnly = true)
    public BookResponse getBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
        return BookResponse.fromEntity(book);
    }

    @Transactional(readOnly = true)
    public PageResponse<BookResponse> getAllBooks(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Book> bookPage = bookRepository.findAll(pageable);

        List<BookResponse> content = bookPage.getContent().stream()
                .map(BookResponse::fromEntity)
                .toList();

        return new PageResponse<>(
            content,
            bookPage.getNumber(),
            bookPage.getSize(),
            bookPage.getTotalElements(),
            bookPage.getTotalPages(),
            bookPage.isFirst(),
            bookPage.isLast()
        );
    }

    public BookResponse updateBook(Long id, BookRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

        if (!book.getIsbn().equals(request.isbn()) && bookRepository.existsByIsbn(request.isbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.isbn() + " already exists");
        }

        book.setTitle(request.title());
        book.setAuthor(request.author());
        book.setIsbn(request.isbn());
        book.setPrice(request.price());
        book.setPublishedDate(request.publishedDate());

        Book updated = bookRepository.save(book);
        return BookResponse.fromEntity(updated);
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }
}
```

## controller/BookController.java

```java
package com.example.bookapi.controller;

import com.example.bookapi.dto.BookRequest;
import com.example.bookapi.dto.BookResponse;
import com.example.bookapi.dto.PageResponse;
import com.example.bookapi.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.createBook(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponse> getBook(@PathVariable Long id) {
        BookResponse response = bookService.getBook(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PageResponse<BookResponse>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        PageResponse<BookResponse> response = bookService.getAllBooks(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookRequest request) {
        BookResponse response = bookService.updateBook(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
```

## exception/ResourceNotFoundException.java

```java
package com.example.bookapi.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

## exception/GlobalExceptionHandler.java

```java
package com.example.bookapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pd.setTitle("Resource Not Found");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadArgument(IllegalArgumentException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pd.setTitle("Bad Request");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Failed");
        pd.setProperty("errors", errors);
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        pd.setTitle("Internal Server Error");
        pd.setProperty("timestamp", Instant.now());
        return pd;
    }
}
```

## config/SecurityConfig.java

```java
package com.example.bookapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().denyAll()
            )
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
        return http.build();
    }
}
```

## API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST   | `/api/books` | Create a new book |
| GET    | `/api/books` | List all books (paginated, sorted) |
| GET    | `/api/books/{id}` | Get a book by ID |
| PUT    | `/api/books/{id}` | Update a book |
| DELETE | `/api/books/{id}` | Delete a book |

Query parameters for `GET /api/books`: `page` (default 0), `size` (default 10), `sortBy` (default id), `sortDir` (asc/desc)
