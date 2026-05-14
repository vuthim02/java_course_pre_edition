# Testing: JUnit 5, Mockito, TDD

Testing is critical for enterprise software. JUnit 5 provides the test framework, Mockito enables mock-based isolation, and TDD guides test-first development. Nested classes group related tests, and tags filter them for selective execution.

## Dependencies (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
<!-- Brings in: JUnit 5, Mockito, AssertJ, Hamcrest, JSON Assert -->
```

## JUnit 5 — Core Annotations

```java
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import java.time.Duration;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

class CalculatorTest {

    private Calculator calculator;

    @BeforeAll
    static void initAll() {
        System.out.println("Runs once before ALL tests");
    }

    @AfterAll
    static void tearDownAll() {
        System.out.println("Runs once after ALL tests");
    }

    @BeforeEach
    void init() {
        calculator = new Calculator();
        System.out.println("Runs before each test");
    }

    @AfterEach
    void tearDown() {
        System.out.println("Runs after each test");
    }

    @Test
    @DisplayName("2 + 3 = 5")
    void addsTwoNumbers() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    @DisplayName("Division by zero throws exception")
    void divisionByZeroThrows() {
        assertThrows(ArithmeticException.class,
            () -> calculator.divide(1, 0));
    }

    @Test
    @DisplayName("Completes within timeout")
    void runsWithinTimeout() {
        assertTimeout(Duration.ofMillis(100),
            () -> calculator.slowOperation());
    }

    @Test
    @DisplayName("Assumption: only runs on CI")
    void onlyOnCiServer() {
        assumeTrue("CI".equals(System.getenv("ENV")));
        assertTrue(calculator.isReady());
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1, 2",
        "2, 3, 5",
        "10, -5, 5"
    })
    @DisplayName("Parameterized addition")
    void parameterizedAdd(int a, int b, int expected) {
        assertEquals(expected, calculator.add(a, b));
    }

    @ParameterizedTest
    @ValueSource(strings = { "racecar", "radar", "level" })
    @DisplayName("Palindrome check")
    void palindromes(String word) {
        assertTrue(calculator.isPalindrome(word));
    }

    @ParameterizedTest
    @EnumSource(TimeUnit.class)
    @DisplayName("All enum constants are non-null")
    void allTimeUnitsNonNull(TimeUnit unit) {
        assertNotNull(unit);
    }

    @Test
    @DisplayName("Grouped assertions")
    void groupedAssertions() {
        Person person = new Person("John", "Doe", 30);
        assertAll("person",
            () -> assertEquals("John", person.firstName()),
            () -> assertEquals("Doe", person.lastName()),
            () -> assertEquals(30, person.age())
        );
    }
}
```

## Mockito — Mocks, Spies, Captors

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private PaymentGateway paymentGateway;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    @Spy
    private AuditLogger auditLogger = new AuditLogger();

    @Captor
    private ArgumentCaptor<PaymentRequest> paymentCaptor;

    @Test
    @DisplayName("Successful order places payment")
    void placeOrderSuccess() {
        Order order = new Order("SKU-123", 2, BigDecimal.valueOf(100));

        when(inventoryService.isInStock("SKU-123", 2)).thenReturn(true);
        when(paymentGateway.charge(any(PaymentRequest.class)))
            .thenReturn(new PaymentResponse("txn-001", Status.SUCCESS));

        OrderResult result = orderService.placeOrder(order);

        assertTrue(result.isSuccess());
        assertEquals("txn-001", result.transactionId());

        verify(paymentGateway).charge(paymentCaptor.capture());
        PaymentRequest captured = paymentCaptor.getValue();
        assertEquals(BigDecimal.valueOf(200), captured.amount());
        assertEquals("SKU-123", captured.sku());
    }

    @Test
    @DisplayName("Out-of-stock order is rejected")
    void placeOrderOutOfStock() {
        Order order = new Order("SKU-999", 1, BigDecimal.valueOf(50));

        when(inventoryService.isInStock("SKU-999", 1)).thenReturn(false);

        assertThrows(OutOfStockException.class,
            () -> orderService.placeOrder(order));

        verify(paymentGateway, never()).charge(any());
    }

    @Test
    @DisplayName("Retry on payment failure")
    void retryOnPaymentFailure() {
        Order order = new Order("SKU-123", 1, BigDecimal.valueOf(100));
        when(inventoryService.isInStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(any(PaymentRequest.class)))
            .thenThrow(new PaymentException("Network error"))
            .thenReturn(new PaymentResponse("txn-002", Status.SUCCESS));

        OrderResult result = orderService.placeOrder(order);

        assertTrue(result.isSuccess());
        verify(paymentGateway, times(2)).charge(any());
    }

    @Test
    @DisplayName("Spy verifies audit logging")
    void auditLogged() {
        Order order = new Order("SKU-001", 1, BigDecimal.TEN);
        when(inventoryService.isInStock(anyString(), anyInt())).thenReturn(true);
        when(paymentGateway.charge(any())).thenReturn(
            new PaymentResponse("txn-003", Status.SUCCESS));

        orderService.placeOrder(order);

        verify(auditLogger).log(anyString());
    }

    @Test
    @DisplayName("Stubbing void methods with doAnswer")
    void voidMethodStub() {
        doAnswer(invocation -> {
            String msg = invocation.getArgument(0);
            System.out.println("Mocked send: " + msg);
            return null;
        }).when(auditLogger).log(anyString());

        auditLogger.log("test");
        verify(auditLogger).log("test");
    }

    @Test
    @DisplayName("Mock default answer using lenient")
    void lenientStubbing() {
        lenient().when(inventoryService.getStockLevel("SKU-999")).thenReturn(0);
        // Without lenient(), unused stubs would cause an exception
    }
}
```

## TDD Example: FizzBuzz

**Step 1 — Write the test first:**

```java
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class FizzBuzzTest {

    private final FizzBuzz fizzBuzz = new FizzBuzz();

    @Test
    void returnsNumberAsString() {
        assertEquals("1", fizzBuzz.of(1));
        assertEquals("2", fizzBuzz.of(2));
    }

    @Test
    void returnsFizzForMultipleOfThree() {
        assertEquals("Fizz", fizzBuzz.of(3));
        assertEquals("Fizz", fizzBuzz.of(6));
    }

    @Test
    void returnsBuzzForMultipleOfFive() {
        assertEquals("Buzz", fizzBuzz.of(5));
        assertEquals("Buzz", fizzBuzz.of(10));
    }

    @Test
    void returnsFizzBuzzForMultipleOfThreeAndFive() {
        assertEquals("FizzBuzz", fizzBuzz.of(15));
        assertEquals("FizzBuzz", fizzBuzz.of(30));
    }

    @ParameterizedTest
    @CsvSource({
        "1, 1",
        "2, 2",
        "3, Fizz",
        "5, Buzz",
        "15, FizzBuzz",
        "45, FizzBuzz"
    })
    void coversAllCases(int input, String expected) {
        assertEquals(expected, fizzBuzz.of(input));
    }
}
```

**Step 2 — Implement to pass the test:**

```java
public class FizzBuzz {

    public String of(int number) {
        if (number % 15 == 0) {
            return "FizzBuzz";
        }
        if (number % 3 == 0) {
            return "Fizz";
        }
        if (number % 5 == 0) {
            return "Buzz";
        }
        return String.valueOf(number);
    }
}
```

## @Nested and @Tag

```java
import org.junit.jupiter.api.*;

@Tag("unit")
@DisplayName("UserService unit tests")
class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    @Tag("smoke")
    @DisplayName("Can create valid user")
    void createUser() {
        // ...
    }

    @Nested
    @DisplayName("when user is admin")
    @Tag("security")
    class AdminTests {

        @Test
        @DisplayName("can delete other users")
        void adminCanDelete() {
            // ...
        }

        @Test
        @DisplayName("can view audit logs")
        void adminCanViewAudit() {
            // ...
        }
    }

    @Nested
    @DisplayName("when user is regular")
    @Tag("security")
    class RegularUserTests {

        @Test
        @DisplayName("cannot delete other users")
        void regularCannotDelete() {
            // ...
        }
    }
}
```

## Test Filtering with Tags

```xml
<!-- Maven: run only "smoke" tests -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <groups>smoke</groups>
        <excludedGroups>slow,integration</excludedGroups>
    </configuration>
</plugin>
```

```bash
# Maven: run tagged tests
./mvnw test -Dgroups="unit,smoke"

# Exclude specific tags
./mvnw test -DexcludedGroups="integration"

# Gradle: filter by tag
./gradlew test --tests "*UserService*"
```

```java
// Gradle build.gradle.kts tag filtering
tasks.test {
    useJUnitPlatform {
        includeTags("unit", "smoke")
        excludeTags("integration", "slow")
    }
}
```
