# Lambda Expressions, Streams API, and Optional

Lambda expressions provide a concise way to implement functional interfaces. The Streams API enables declarative data processing pipelines. `Optional` is a container that may or may not hold a value, helping avoid null pointer exceptions.

```java
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import java.nio.file.*;

// ============================================================
// 1. Lambda syntax variations
// ============================================================

class LambdaSyntaxDemo {
    static void demo() {
        // (params) -> expression
        BinaryOperator<Integer> add = (a, b) -> a + b;

        // (params) -> { statements }
        Function<String, String> process = (s) -> {
            String trimmed = s.trim();
            return trimmed.isEmpty() ? "default" : trimmed;
        };

        // No params
        Runnable hello = () -> System.out.println("Hello!");

        // Single param — parens optional
        UnaryOperator<String> upper = s -> s.toUpperCase();

        // Type inference
        Comparator<Integer> cmp = (x, y) -> Integer.compare(x, y);
    }
}

// ============================================================
// 2. Method references
// ============================================================

class MethodRefDemo {
    static void demo() {
        List<String> words = List.of("apple", "banana", "cherry");

        // Class::staticMethod
        words.forEach(System.out::println);

        // instance::method (on a specific instance)
        var printer = new MethodRefDemo();
        words.forEach(printer::printWord);

        // Class::instanceMethod (on an arbitrary instance of the class)
        List<String> sorted = words.stream()
            .sorted(String::compareToIgnoreCase)
            .toList();

        // Class::new (constructor reference)
        List<String> copy = words.stream()
            .map(String::new)            // same as s -> new String(s)
            .toList();
    }

    void printWord(String w) {
        System.out.println("Word: " + w);
    }
}

// ============================================================
// 3. Optional deep dive
// ============================================================

class OptionalDemo {
    static Optional<String> findName(int id) {
        return id == 1 ? Optional.of("Alice")
             : id == 2 ? Optional.of("Bob")
             : Optional.empty();
    }

    static void demo() {
        // Creation
        Optional<String> o1 = Optional.of("present");       // throws if null
        Optional<String> o2 = Optional.ofNullable(null);     // empty if null
        Optional<String> o3 = Optional.empty();              // explicitly empty

        // Checking
        System.out.println(o1.isPresent());    // true
        System.out.println(o1.isEmpty());      // false

        // Conditional action
        o1.ifPresent(s -> System.out.println("Found: " + s));

        // Retrieving with defaults
        String val = o2.orElse("default");
        String val2 = o2.orElseGet(() -> computeDefault());
        // String val3 = o2.orElseThrow();                    // NoSuchElementException
        String val4 = o2.orElseThrow(() -> new NoSuchElementException("Not found"));

        // Transforming
        Optional<String> upper = o1.map(String::toUpperCase);
        Optional<Integer> length = o1.map(String::length);

        // flatMap — avoid nested Optional
        Optional<String> nested = o1.flatMap(s -> findName(1));
    }

    static String computeDefault() {
        return "computed-default";
    }
}

// ============================================================
// 4. Stream creation
// ============================================================

class StreamCreationDemo {
    static void demo() {
        // From collections
        Stream<String> fromList = List.of("a", "b").stream();

        // From arrays
        Stream<Integer> fromArray = Arrays.stream(new Integer[]{1, 2, 3});

        // Stream.of
        Stream<String> of = Stream.of("x", "y", "z");

        // Stream.iterate (infinite, then limit)
        Stream<Integer> evens = Stream.iterate(0, n -> n + 2).limit(10);

        // Stream.generate (infinite, then limit)
        Stream<Double> randoms = Stream.generate(Math::random).limit(5);

        // Files.lines (try-with-resources)
        // try (Stream<String> lines = Files.lines(Path.of("file.txt"))) {
        //     lines.forEach(System.out::println);
        // } catch (IOException e) { /* handle */ }
    }
}

// ============================================================
// 5. Intermediate operations
// ============================================================

class IntermediateOpsDemo {
    static void demo() {
        List<String> words = List.of(
            "apple", "banana", "avocado", "cherry", "apricot", "blueberry"
        );

        List<String> result = words.stream()
            .filter(w -> w.startsWith("a"))              // "apple", "avocado", "apricot"
            .map(String::toUpperCase)                     // "APPLE", "AVOCADO", "APRICOT"
            .sorted()                                      // "APPLE", "APRICOT", "AVOCADO"
            .distinct()                                    // remove duplicates (none here)
            .limit(2)                                      // "APPLE", "APRICOT"
            .toList();

        System.out.println("Pipeline result: " + result);

        // flatMap — flatten nested streams
        List<List<Integer>> nested = List.of(
            List.of(1, 2), List.of(3, 4), List.of(5, 6)
        );
        List<Integer> flat = nested.stream()
            .flatMap(Collection::stream)
            .toList();
        System.out.println("Flattened: " + flat);          // [1, 2, 3, 4, 5, 6]

        // peek — for debugging (intermediate)
        long count = words.stream()
            .peek(w -> System.out.println("  before: " + w))
            .filter(w -> w.length() > 5)
            .peek(w -> System.out.println("  after:  " + w))
            .count();

        // skip, takeWhile, dropWhile
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7);
        List<Integer> skipped = nums.stream().skip(3).toList();        // [4, 5, 6, 7]
        List<Integer> taken = nums.stream().takeWhile(n -> n < 5).toList();   // [1, 2, 3, 4]
        List<Integer> dropped = nums.stream().dropWhile(n -> n < 5).toList(); // [5, 6, 7]
    }
}

// ============================================================
// 6. Terminal operations
// ============================================================

class TerminalOpsDemo {
    static void demo() {
        List<String> words = List.of("apple", "banana", "cherry", "date");

        // collect / toList
        List<String> list = words.stream().filter(w -> w.length() > 4).toList();

        // forEach / forEachOrdered (for parallel streams)
        words.stream().forEach(System.out::println);

        // reduce
        Optional<String> concatenated = words.stream().reduce((a, b) -> a + ", " + b);
        concatenated.ifPresent(System.out::println);   // apple, banana, cherry, date

        int totalLength = words.stream().reduce(0, (sum, w) -> sum + w.length(), Integer::sum);

        // count
        long longWords = words.stream().filter(w -> w.length() > 4).count();

        // anyMatch / allMatch / noneMatch
        boolean anyLong = words.stream().anyMatch(w -> w.length() > 10);
        boolean allLong = words.stream().allMatch(w -> w.length() > 1);
        boolean noneEmpty = words.stream().noneMatch(String::isEmpty);

        // findFirst / findAny
        Optional<String> first = words.stream().filter(w -> w.startsWith("b")).findFirst();
        Optional<String> any = words.parallelStream().filter(w -> w.length() > 3).findAny();

        // min / max (need Comparator)
        Optional<String> shortest = words.stream().min(Comparator.comparingInt(String::length));
        Optional<String> longest  = words.stream().max(Comparator.comparingInt(String::length));
    }
}

// ============================================================
// 7. Collectors deep dive
// ============================================================

class CollectorsDemo {
    static void demo() {
        List<String> items = List.of("apple", "banana", "cherry", "date", "fig", "grape");

        // toList, toSet, toMap
        Set<String> set = items.stream().collect(Collectors.toSet());

        // toMap — with key and value mappers
        Map<String, Integer> nameToLength = items.stream()
            .collect(Collectors.toMap(Function.identity(), String::length));

        // groupingBy
        Map<Integer, List<String>> byLength = items.stream()
            .collect(Collectors.groupingBy(String::length));
        System.out.println("Grouped by length: " + byLength);

        // groupingBy with downstream collector
        Map<Integer, Long> countByLength = items.stream()
            .collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println("Count by length: " + countByLength);

        // partitioningBy
        Map<Boolean, List<String>> partitioned = items.stream()
            .collect(Collectors.partitioningBy(w -> w.length() > 4));
        System.out.println("Partitioned (length>4): " + partitioned);

        // joining
        String joined = items.stream().collect(Collectors.joining(", "));
        System.out.println("Joined: " + joined);

        // summarizingInt
        IntSummaryStatistics stats = items.stream()
            .collect(Collectors.summarizingInt(String::length));
        System.out.println("Stats: " + stats);

        // teeing (Java 12+) — two collectors, one result
        record MinMax(String min, String max) {}
        MinMax minMax = items.stream().collect(
            Collectors.teeing(
                Collectors.minBy(Comparator.naturalOrder()),
                Collectors.maxBy(Comparator.naturalOrder()),
                (min, max) -> new MinMax(min.orElse(""), max.orElse(""))
            )
        );
        System.out.println("MinMax: " + minMax);
    }
}

// ============================================================
// 8. Parallel streams — with performance caveats
// ============================================================

class ParallelStreamsDemo {
    static void demo() {
        // Parallel stream from .parallelStream() or .stream().parallel()
        long sum = LongStream.rangeClosed(1, 10_000_000)
            .parallel()
            .sum();

        // CAVEATS:
        // 1. Only beneficial for large datasets with CPU-intensive work
        // 2. Must avoid stateful operations that share mutable state
        // 3. forEachOrdered for deterministic output order
        // 4. Common ForkJoinPool is shared — don't block
        // 5. Overhead of splitting/merging can dominate for small datasets

        // Correct parallel reduction:
        long total = LongStream.range(0, 1_000_000)
            .parallel()
            .filter(n -> n % 2 == 0)
            .sum();

        // Wrong: shared mutable state
        // List<Integer> wrong = new ArrayList<>();
        // LongStream.range(0, 100).parallel()
        //     .forEach(wrong::add);   // NOT thread-safe!
    }
}

// ============================================================
// Main
// ============================================================

public class StreamsLambdasDemo {
    public static void main(String[] args) {
        LambdaSyntaxDemo.demo();
        MethodRefDemo.demo();
        OptionalDemo.demo();
        StreamCreationDemo.demo();
        IntermediateOpsDemo.demo();
        TerminalOpsDemo.demo();
        CollectorsDemo.demo();
        ParallelStreamsDemo.demo();
    }
}
```
