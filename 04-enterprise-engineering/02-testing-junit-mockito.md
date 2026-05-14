# Enterprise Engineering — Lesson 2: Testing (JUnit & Mockito)

## Why Test?

**Without tests:** Any change could break anything. You manually test every feature → slow, error-prone.

**With tests:** Change code, run tests, know instantly if something broke → fast, safe.

## Test Pyramid

```
         /\           Manual / E2E tests (few)
        /  \          ────────────────────────
       /    \         Integration tests (some)
      /      \
     /────────\       Unit tests (MANY — 70%+ of tests)
    /          \
   /────────────\
```

**Unit tests** — Test ONE class in isolation (fast, reliable)
**Integration tests** — Test multiple classes together
**E2E tests** — Test the full system from outside

## JUnit 5

### Basic Annotations

```java
import org.junit.jupiter.api.*;

class CalculatorTest {

    @BeforeAll
    static void setupAll() {
        System.out.println("Run ONCE before ALL tests");
    }

    @BeforeEach
    void setup() {
        System.out.println("Run BEFORE each test");
    }

    @Test
    void shouldAddTwoNumbers() {
        Calculator calc = new Calculator();
        int result = calc.add(2, 3);
        Assertions.assertEquals(5, result);
    }

    @Test
    void shouldThrowOnDivisionByZero() {
        Calculator calc = new Calculator();
        Assertions.assertThrows(ArithmeticException.class,
            () -> calc.divide(10, 0));
    }

    @AfterEach
    void teardown() {
        System.out.println("Run AFTER each test");
    }

    @AfterAll
    static void teardownAll() {
        System.out.println("Run ONCE after ALL tests");
    }
}
```

### Assertions

```java
import static org.junit.jupiter.api.Assertions.*;

assertEquals(5, result);                  // Equality
assertNotEquals(5, result);               // Not equal
assertTrue(result > 0);                   // Boolean true
assertFalse(result < 0);                  // Boolean false
assertNull(object);                        // Null
assertNotNull(object);                    // Not null
assertSame(a, b);                          // Same reference (==)
assertNotSame(a, b);                       // Different reference
assertThrows(IOException.class, () -> {   // Exception expected
    throw new IOException();
});
assertDoesNotThrow(() -> method());        // No exception
assertIterableEquals(list1, list2);        // Iterables match
assertLinesMatch(expected, actual);       // Lines match
assertAll("group",
    () -> assertEquals(1, x),
    () -> assertEquals(2, y)
);
```

### Parameterized Tests

```java
@ParameterizedTest
@ValueSource(ints = {1, 2, 3, 4, 5})
void shouldBePositive(int number) {
    assertTrue(number > 0);
}

@ParameterizedTest
@CsvSource({"1,1,2", "2,3,5", "10,-5,5"})
void shouldAdd(int a, int b, int expected) {
    assertEquals(expected, a + b);
}

@ParameterizedTest
@MethodSource("provideData")
void withMethodSource(int input, boolean expected) {
    assertEquals(expected, input > 0);
}

static Stream<Arguments> provideData() {
    return Stream.of(
        Arguments.of(1, true),
        Arguments.of(-1, false),
        Arguments.of(0, false)
    );
}
```

## Mockito — Mocking Dependencies

```java
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;  // Auto-injects mocks

    @Test
    void shouldFindUser() {
        // Arrange
        User expectedUser = new User(1L, "Alice");
        when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

        // Act
        User result = userService.findUser(1L);

        // Assert
        assertEquals("Alice", result.getName());
        verify(userRepository).findById(1L);  // Verify method was called
    }

    @Test
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
            () -> userService.findUser(999L));
    }
}
```

### Mockito Key Methods

```java
// Creating mocks
UserService mock = mock(UserService.class);

// Stubbing
when(mock.findUser(1L)).thenReturn(user);
when(mock.findUser(anyLong())).thenReturn(user);
when(mock.findUser(1L)).thenThrow(new RuntimeException());
when(mock.findUser(anyLong())).thenAnswer(invocation -> {
    Long id = invocation.getArgument(0);
    return new User(id, "User" + id);
});

// Verification
verify(mock).findUser(1L);                    // Called exactly once
verify(mock, times(2)).findUser(1L);           // Called twice
verify(mock, never()).findUser(anyLong());     // Never called
verify(mock, atLeastOnce()).findUser(1L);      // At least once
verify(mock, atMost(3)).findUser(1L);          // At most 3 times
verifyNoInteractions(mock);                    // No interactions at all
verifyNoMoreInteractions(mock);               // No unexpected calls

// Argument matchers
when(mock.save(any(User.class))).thenReturn(user);
when(mock.save(argThat(u -> u.getName().startsWith("A")))).thenReturn(user);

// Spying (partial mocking)
User realUser = new User("Alice");
UserService spy = spy(realUser);
when(spy.getName()).thenReturn("Bob");  // Override one method
```

## TDD — Test-Driven Development

**Red → Green → Refactor**

```
1. RED:    Write a failing test first
2. GREEN:  Write the MINIMUM code to make it pass
3. REFACTOR: Improve code while keeping tests green
```

```java
// Step 1: Write the test (it won't compile yet — no FizzBuzz class)
@Test
void shouldReturnFizzForMultipleOfThree() {
    assertEquals("Fizz", FizzBuzz.of(3));
}

// Step 2: Write minimum code
public class FizzBuzz {
    public static String of(int n) {
        return "Fizz";  // Just enough to pass
    }
}

// Step 3: Add more tests
@Test
void shouldReturnBuzzForMultipleOfFive() {
    assertEquals("Buzz", FizzBuzz.of(5));
}

// Step 4: Make it pass
public static String of(int n) {
    if (n % 3 == 0) return "Fizz";
    return String.valueOf(n);
}
```

## Test Best Practices

1. **One assertion per test** (or related assertions)
2. **Descriptive test names**: `shouldReturnUserWhenFound()` not `test1()`
3. **Arrange-Act-Assert** pattern
4. **Test behavior, not implementation** — refactoring shouldn't break tests
5. **Don't test getters/setters** — they have no logic
6. **Aim for 80%+ coverage** on business logic

---

### Exercises

1. Write JUnit tests for a `StringUtils` class (reverse, isPalindrome, countVowels).
2. Use Mockito to mock a `PaymentGateway` and test a `PaymentService`.
3. Write a parameterized test for a `MathUtils` class.
4. Practice TDD: implement a `ShoppingCart` class following Red-Green-Refactor.
5. Use `verify()` to assert that a repository method was called with specific arguments.
