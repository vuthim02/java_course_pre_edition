# Enterprise Engineering — Lesson 8: Spring Boot

## What is Spring Boot?

Spring Boot = Spring Framework + **Auto-configuration** + **Embedded server** + **Production-ready features**.

```
Traditional Spring:                Spring Boot:
• Manual configuration             • Auto-configuration
• External server (Tomcat)         • Embedded Tomcat/Jetty/Undertow
• XML or JavaConfig                • application.properties/yml
• Complex setup                     • Spring Initializr (start.spring.io)
• Manual dependency management     • Starter POMs
```

## Creating a Spring Boot Application

### Using Spring Initializr

Go to `start.spring.io` or use IntelliJ:

1. Project: Maven/Gradle
2. Language: Java 21
3. Dependencies: Spring Web, Spring Data JPA, PostgreSQL Driver, Spring Security

### The Main Class

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication  // = @Configuration + @EnableAutoConfiguration + @ComponentScan
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
```

### Configuration

```properties
# application.properties
server.port=8080
server.servlet.context-path=/api/v1

spring.application.name=demo

# Datasource
spring.datasource.url=jdbc:postgresql://localhost:5432/demo
spring.datasource.username=postgres
spring.datasource.password=secret
spring.datasource.hikari.maximum-pool-size=20

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### YAML Alternative

```yaml
server:
  port: 8080
  servlet:
    context-path: /api/v1

spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/demo
    username: postgres
    password: secret
    hikari:
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

## Spring Boot Starters

Starters are **pre-configured dependency sets**:

```xml
<!-- Web (REST API) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Actuator — Production Monitoring

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# Expose all endpoints
management.endpoints.web.exposure.include=health,info,metrics,env,beans,loggers

# Custom health check
management.endpoint.health.show-details=always
```

Endpoints available at `/actuator/`:
- `/health` — App health status
- `/info` — Custom app info
- `/metrics` — JVM, memory, GC, thread metrics
- `/env` — Environment properties
- `/beans` — All Spring beans
- `/loggers` — Logging levels (can change at runtime!)

## DevTools — Developer Experience

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <optional>true</optional>
</dependency>
```

Features:
- **Automatic restart** when code changes
- **LiveReload** — browser auto-refresh
- **Disable caching** (templates, Thymeleaf)
- **Remote debugging** support

## Spring Boot 3.x Key Features

```java
// 1. Virtual Threads (Java 21+)
// application.properties:
spring.threads.virtual.enabled=true

// 2. GraalVM Native Image
// Build: ./mvnw -Pnative native:compile
// Startup: milliseconds instead of seconds

// 3. Problem Details (RFC 7807)
@ExceptionHandler(Exception.class)
public ProblemDetail handleError(Exception e) {
    return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
}

// 4. HTTP Interface Clients
@HttpExchange("/api/users")
public interface UserClient {
    @GetExchange("/{id}")
    User getUser(@PathVariable Long id);
}
```

---

### Exercises

1. Create a Spring Boot app with Spring Web and an Actuator. Hit `/actuator/health`.
2. Configure a PostgreSQL datasource (or H2 for testing). Verify connection.
3. Enable DevTools and test auto-restart by changing code.
4. Expose all Actuator endpoints and explore `/beans` and `/metrics`.
5. Build and run the app as a JAR: `mvn clean package -DskipTests && java -jar target/*.jar`.
