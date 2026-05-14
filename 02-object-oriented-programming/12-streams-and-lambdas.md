# OOP — Lesson 12: Lambdas, Streams & Functional Programming

## Lambda Expressions (Java 8+)

A **lambda** is a concise way to pass behavior as data — like an anonymous method.

```
Traditional way:           Lambda way:
button.setOnClickListener(  button.setOnClickListener(
    new View.OnClickListener() {        v -> System.out.println("Clicked")
        @Override                      );
        public void onClick(View v) {
            System.out.println("Clicked");
        }
    }
);
```

### Lambda Syntax

```java
(parameters) -> expression
(parameters) -> { statements; }

// Examples:
(int x, int y) -> x + y            // Two params, one expression
(String s) -> s.length()           // One param, one expression
s -> s.length()                    // Type inference — cleaner!
() -> 42                           // No params
x -> {                             // One param, multiple statements
    int y = x * 2;
    return y + 1;
}
```

### Functional Interfaces

A **functional interface** has exactly ONE abstract method. Lambdas can implement them.

```java
@FunctionalInterface
interface Greeter {
    String greet(String name);  // Single abstract method
}

// Using anonymous class:
Greeter formal = new Greeter() {
    @Override
    public String greet(String name) {
        return "Good evening, " + name;
    }
};

// Using lambda:
Greeter casual = name -> "Hey " + name;

System.out.println(formal.greet("Alice"));  // "Good evening, Alice"
System.out.println(casual.greet("Bob"));    // "Hey Bob"
```

### Built-in Functional Interfaces (java.util.function)

```java
// Predicate<T> — takes T, returns boolean
Predicate<String> isEmpty = s -> s.isEmpty();
Predicate<Integer> isEven = n -> n % 2 == 0;

// Function<T, R> — takes T, returns R
Function<String, Integer> lengthFn = s -> s.length();
Function<Integer, String> toString = n -> "Number: " + n;

// Consumer<T> — takes T, returns nothing
Consumer<String> printer = s -> System.out.println(s);
Consumer<Person> saver = p -> database.save(p);

// Supplier<T> — takes nothing, returns T
Supplier<Double> random = () -> Math.random();
Supplier<LocalDate> today = () -> LocalDate.now();

// BiFunction<T, U, R> — takes two params, returns R
BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;

// UnaryOperator<T> — takes T, returns T (same type)
UnaryOperator<String> toUpper = s -> s.toUpperCase();

// BinaryOperator<T> — takes two T's, returns T
BinaryOperator<Integer> multiply = (a, b) -> a * b;
```

### Method References

Shorthand when lambda just calls an existing method:

```java
// Lambda:
list.forEach(s -> System.out.println(s));

// Method reference:
list.forEach(System.out::println);

// Types:
String::toUpperCase           // instance method on a class
System.out::println           // instance method on an object
String::valueOf               // static method
String::new                   // constructor
this::process                 // instance method of current object
```

## Streams API

A **Stream** is a sequence of elements that supports functional-style operations.

```
DATA FLOW:
┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐
│  Source  │──▶│Intermediate│──▶│Intermediate│──▶│ Terminal │
│(List,array)│  │ Operations│  │ Operations│  │Operation │
└──────────┘   └──────────┘   └──────────┘   └──────────┘
                (lazy)         (lazy)          (eager)
```

### Creating Streams

```java
// From collections
List<String> list = List.of("a", "b", "c");
Stream<String> stream = list.stream();
Stream<String> parallelStream = list.parallelStream();

// From arrays
int[] numbers = {1, 2, 3};
IntStream numStream = Arrays.stream(numbers);

// From values
Stream<String> stream1 = Stream.of("a", "b", "c");
Stream<Integer> stream2 = Stream.iterate(0, n -> n + 1);  // infinite

// From range
IntStream.range(1, 10);        // 1, 2, ..., 9
IntStream.rangeClosed(1, 10);  // 1, 2, ..., 10
```

### Stream Operations

```java
List<String> names = List.of("Alice", "Bob", "Charlie", "David", "Alice");

// --- INTERMEDIATE OPERATIONS (return a Stream, lazy) ---

// filter — keep elements matching predicate
names.stream()
    .filter(name -> name.startsWith("A"))
    .forEach(System.out::println);  // Alice, Alice

// map — transform each element
names.stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);  // ALICE, BOB, CHARLIE, ...

// flatMap — flatten nested structures
List<List<String>> nested = List.of(
    List.of("a", "b"),
    List.of("c", "d")
);
nested.stream()
    .flatMap(List::stream)
    .forEach(System.out::println);  // a, b, c, d

// distinct — remove duplicates
names.stream()
    .distinct()
    .forEach(System.out::println);  // Alice, Bob, Charlie, David

// sorted — sort elements
names.stream()
    .sorted()
    .forEach(System.out::println);  // Alice, Alice, Bob, Charlie, David

// peek — for debugging (intermediate forEach)
names.stream()
    .peek(n -> System.out.println("Processing: " + n))
    .filter(n -> n.length() > 4)
    .forEach(System.out::println);

// limit — take first n elements
names.stream()
    .limit(2)
    .forEach(System.out::println);  // Alice, Bob

// skip — ignore first n elements
names.stream()
    .skip(2)
    .forEach(System.out::println);  // Charlie, David, Alice
```

### Terminal Operations (Eager — produce result)

```java
// forEach — do something with each element
names.stream().forEach(System.out::println);

// collect — collect into a collection
List<String> list = names.stream()
    .filter(n -> n.length() > 3)
    .collect(Collectors.toList());

Set<String> set = names.stream()
    .collect(Collectors.toSet());

Map<Integer, List<String>> grouped = names.stream()
    .collect(Collectors.groupingBy(String::length));

String joined = names.stream()
    .collect(Collectors.joining(", "));  // "Alice, Bob, Charlie, ..."

// toList (Java 16+) — shortcut
List<String> result = names.stream()
    .filter(n -> n.length() > 3)
    .toList();  // Immutable!

// reduce — combine elements into one
int sum = IntStream.range(1, 11)
    .reduce(0, (a, b) -> a + b);  // 55

// count
long count = names.stream().filter(n -> n.startsWith("A")).count();

// anyMatch / allMatch / noneMatch
boolean hasLong = names.stream().anyMatch(n -> n.length() > 10);
boolean allShort = names.stream().allMatch(n -> n.length() < 20);

// findFirst / findAny
String first = names.stream()
    .filter(n -> n.startsWith("C"))
    .findFirst()
    .orElse("Not found");  // "Charlie"

// min / max
String shortest = names.stream()
    .min(Comparator.comparingInt(String::length))
    .orElse(null);
```

### Collectors in Detail

```java
List<Person> people = getPeople();

// Grouping
Map<String, List<Person>> byCity = people.stream()
    .collect(Collectors.groupingBy(Person::getCity));

Map<String, Long> countByCity = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.counting()
    ));

Map<String, Double> avgAgeByCity = people.stream()
    .collect(Collectors.groupingBy(
        Person::getCity,
        Collectors.averagingInt(Person::getAge)
    ));

// Partitioning
Map<Boolean, List<Person>> adults = people.stream()
    .collect(Collectors.partitioningBy(p -> p.getAge() >= 18));

// Mapping
List<String> names = people.stream()
    .collect(Collectors.mapping(Person::getName, Collectors.toList()));

// Summarizing
IntSummaryStatistics stats = people.stream()
    .collect(Collectors.summarizingInt(Person::getAge));
stats.getAverage();  // average age
stats.getMax();      // oldest
stats.getMin();      // youngest
```

### Putting It All Together — Real Examples

```java
// Example 1: Top 5 longest names
List<String> top5 = names.stream()
    .distinct()
    .sorted(Comparator.comparingInt(String::length).reversed())
    .limit(5)
    .collect(Collectors.toList());

// Example 2: Average age of active users
double avgAge = users.stream()
    .filter(User::isActive)
    .mapToInt(User::getAge)
    .average()
    .orElse(0.0);

// Example 3: Map of city → sorted list of users
Map<String, List<User>> usersByCity = users.stream()
    .collect(Collectors.groupingBy(
        User::getCity,
        Collectors.collectingAndThen(
            Collectors.toList(),
            list -> list.stream()
                .sorted(Comparator.comparing(User::getName))
                .collect(Collectors.toList())
        )
    ));

// Example 4: Parallel stream for performance
long count = massiveList.parallelStream()
    .filter(item -> expensiveCheck(item))
    .count();
```

## Optional — Null Safety

`Optional<T>` is a container that may or may not contain a value.

```java
// Before Optional:
public String findName(Long id) {
    Person p = database.find(id);
    return p != null ? p.getName() : "Unknown";
}

// With Optional:
public Optional<String> findName(Long id) {
    return database.find(id)
        .map(Person::getName);
}

// Usage:
String name = findName(42L)
    .orElse("Unknown");               // Default value
    // .orElseGet(() -> fetchDefault()) // Lazy default
    // .orElseThrow(() -> new NotFoundException())
    // .ifPresent(System.out::println)
```

### Common Optional Methods

```java
Optional<String> opt = Optional.of("hello");   // Must be non-null
Optional<String> opt = Optional.ofNullable(mightBeNull);  // Nullable
Optional<String> opt = Optional.empty();       // Empty

String result = opt.get();              // Throws if empty!
String result = opt.orElse("default");  // Default value (eager)
String result = opt.orElseGet(() -> fetchDefault());  // Default value (lazy)
String result = opt.orElseThrow();      // Throws NoSuchElementException
String result = opt.orElseThrow(() -> new CustomException());

opt.ifPresent(s -> System.out.println(s));        // If present, do something
opt.ifPresentOrElse(s -> System.out.println(s),   // Java 9+
    () -> System.out.println("Not found"));

opt.filter(s -> s.length() > 5);         // Keep only if condition
opt.map(s -> s.toUpperCase());           // Transform if present
opt.flatMap(s -> maybeGetOther(s));      // Chain Optional-returning methods

// Chain example:
String result = Optional.ofNullable(getName())
    .filter(name -> name.length() > 2)
    .map(String::toUpperCase)
    .orElse("DEFAULT");
```

---

### Exercises

1. Given a list of integers, use streams to: filter evens, double them, sort descending, and take the first 5.
2. Given a list of strings, group them by length using `Collectors.groupingBy`.
3. Use `reduce` to find the maximum element in a list.
4. Given a list of `Person` objects (name, age, city), find the average age per city. Only include cities with at least 3 people.
5. Use `flatMap` to flatten a `List<List<Integer>>` into a single list, then find all unique numbers.
6. Write a method that returns the first non-empty string from a list, wrapped in `Optional`.
7. **Challenge:** Implement a word count program (like MapReduce) using streams.
