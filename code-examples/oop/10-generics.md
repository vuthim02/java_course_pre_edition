# Generics

Generics enable type-safe code by parameterizing types. The compiler erases generic type information at runtime (type erasure) — `List<String>` becomes just `List`. Bounded type parameters restrict what types can be used. Wildcards (`? extends`, `? super`, `?`) provide flexibility for producer/consumer scenarios.

```java
import java.util.*;
import java.util.function.Function;

// ============================================================
// 1. Generic class
// ============================================================

class Box<T> {
    private T value;

    public Box() {}

    public Box(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isPresent() {
        return value != null;
    }

    @Override
    public String toString() {
        return "Box{" + value + '}';
    }
}

// ============================================================
// 2. Generic interface
// ============================================================

interface Processor<T, R> {
    R process(T input);
}

class UpperCaseProcessor implements Processor<String, String> {
    @Override
    public String process(String input) {
        return input.toUpperCase();
    }
}

// ============================================================
// 3. Generic methods
// ============================================================

class Utils {
    // Generic method — type parameter before return type
    public static <T> T getMiddle(T... args) {
        return args[args.length / 2];
    }

    // Generic method with bounded type parameter
    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) > 0 ? a : b;
    }

    // Bounded: T must be a Number subclass
    public static <T extends Number> double sum(T a, T b) {
        return a.doubleValue() + b.doubleValue();
    }
}

// ============================================================
// 4. Bounded type parameters
// ============================================================

class NumericBox<T extends Number> {
    private final T value;

    public NumericBox(T value) {
        this.value = value;
    }

    public double doubleValue() {
        return value.doubleValue();
    }

    public int intValue() {
        return value.intValue();
    }
}

// Multiple bounds: T must extend A & implement B & C
interface HasColor { String getColor(); }
class ColoredNumericBox<T extends Number & HasColor> {
    private T value;
    ColoredNumericBox(T value) { this.value = value; }
}

// ============================================================
// 5. Wildcards
// ============================================================

class WildcardDemo {
    // Upper-bounded wildcard (? extends) — producer: read from it
    public static double sumOfList(List<? extends Number> list) {
        double sum = 0;
        for (Number n : list) sum += n.doubleValue();
        return sum;
    }

    // Lower-bounded wildcard (? super) — consumer: write to it
    public static void addNumbers(List<? super Integer> list) {
        for (int i = 1; i <= 5; i++) {
            list.add(i);
        }
    }

    // Unbounded wildcard (?) — any type, read-only
    public static void printAll(List<?> list) {
        for (Object elem : list) {
            System.out.print(elem + " ");
        }
        System.out.println();
    }

    // PECS: Producer Extends, Consumer Super
    public static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) dest.add(item);
    }
}

// ============================================================
// 6. Type erasure — compile-time vs runtime
// ============================================================

class ErasureDemo {
    public static void demo() {
        List<String> strings = new ArrayList<>();
        List<Integer> integers = new ArrayList<>();

        // Both are just List at runtime:
        System.out.println(strings.getClass() == integers.getClass());  // true

        // Cannot do: if (x instanceof List<String>)   // COMPILE ERROR
        // Cannot do: new T()                          // COMPILE ERROR
        // Cannot do: new T[]                          // COMPILE ERROR
    }
}

// ============================================================
// Main
// ============================================================

public class GenericsDemo {
    public static void main(String[] args) {
        // --- Generic class ---
        Box<String> stringBox = new Box<>("Hello");
        Box<Integer> intBox = new Box<>(42);
        System.out.println(stringBox.getValue());
        System.out.println(intBox.getValue());

        // --- Generic method ---
        String mid = Utils.getMiddle("A", "B", "C", "D", "E");
        System.out.println("Middle: " + mid);         // C

        int max = Utils.max(10, 20);
        System.out.println("Max: " + max);            // 20

        double sum = Utils.sum(3.5, 2.5);
        System.out.println("Sum: " + sum);            // 6.0

        // --- Bounded type ---
        NumericBox<Double> dBox = new NumericBox<>(3.14);
        System.out.println(dBox.doubleValue());

        // --- Wildcards ---
        List<Integer> ints = List.of(1, 2, 3);
        System.out.println("Sum: " + WildcardDemo.sumOfList(ints));

        List<Object> objs = new ArrayList<>();
        WildcardDemo.addNumbers(objs);
        WildcardDemo.printAll(objs);                  // 1 2 3 4 5

        // PECS copy
        List<String> src = List.of("a", "b", "c");
        List<Object> dest = new ArrayList<>();
        WildcardDemo.copy(src, dest);
        System.out.println(dest);                     // [a, b, c]

        // --- Generic interface ---
        UpperCaseProcessor uc = new UpperCaseProcessor();
        System.out.println(uc.process("hello"));      // HELLO

        // --- Erasure ---
        ErasureDemo.demo();
    }
}
```
