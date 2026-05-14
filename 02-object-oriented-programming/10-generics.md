# OOP — Lesson 10: Generics

## The Problem Generics Solve

Without generics, collections hold ANY object:

```java
List list = new ArrayList();
list.add("Hello");
list.add(42);
list.add(new Person());

String s = (String) list.get(0);  // Must cast — ugly
String s2 = (String) list.get(1);  // ClassCastException! 42 is an Integer!
```

The code compiles but CRASHES at runtime. **Generics catch these bugs at compile time.**

## Generic Classes

```java
// A Box that can hold ANY type T
public class Box<T> {
    private T content;

    public void set(T content) {
        this.content = content;
    }

    public T get() {
        return content;
    }
}

// Usage:
Box<String> stringBox = new Box<>();
stringBox.set("Hello");
String value = stringBox.get();  // No cast needed!

Box<Integer> intBox = new Box<>();
intBox.set(42);
int num = intBox.get();  // No cast needed!

// stringBox.set(42);   // COMPILE ERROR! Can't put Integer in Box<String>
```

## Generic Methods

A method can have its OWN type parameters:

```java
public class Utils {
    // Generic method — T is determined by the caller
    public static <T> T getLast(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(list.size() - 1);
    }

    // Multiple type parameters
    public static <K, V> void printEntry(K key, V value) {
        System.out.println(key + " → " + value);
    }

    // Bounded type parameter — T must be Comparable
    public static <T extends Comparable<T>> T max(T a, T b) {
        return a.compareTo(b) > 0 ? a : b;
    }
}

// Usage:
String last = Utils.getLast(List.of("a", "b", "c"));  // "c"
Utils.printEntry("name", "Alice");
int max = Utils.max(10, 20);  // 20
```

## Type Parameters Naming Convention

| Letter | Meaning | Example |
|--------|---------|---------|
| `T` | Type | `List<T>` |
| `E` | Element | `List<E>` |
| `K` | Key | `Map<K, V>` |
| `V` | Value | `Map<K, V>` |
| `N` | Number | `Box<N extends Number>` |
| `R` | Return | `Callable<R>` |

## Bounded Type Parameters

### Upper Bound (`extends`)

```java
// T must be Number or a subclass of Number
public class NumberBox<T extends Number> {
    private T value;

    public double doubleValue() {
        return value.doubleValue();  // Can call Number methods!
    }
}

NumberBox<Integer> intBox = new NumberBox<>();  // OK
NumberBox<Double> doubleBox = new NumberBox<>(); // OK
// NumberBox<String> strBox = new NumberBox<>(); // COMPILE ERROR! String is not a Number
```

### Multiple Bounds

```java
// T must implement Serializable AND Comparable<T>
public class Data<T extends Serializable & Comparable<T>> {
    private T data;
}
```

## Wildcards (`?`)

### Unbounded Wildcard (`?`)

```java
// Accept ANY type of List
public static void printList(List<?> list) {
    for (Object elem : list) {
        System.out.println(elem);
    }
}

printList(List.of("a", "b"));        // List<String>
printList(List.of(1, 2, 3));         // List<Integer>
printList(List.of(1.0, 2.0, 3.0));   // List<Double>
```

### Upper Bounded Wildcard (`? extends T`)

```java
// Accept List of Number OR any subclass
public static double sumList(List<? extends Number> list) {
    double sum = 0;
    for (Number n : list) {
        sum += n.doubleValue();
    }
    return sum;
}

sumList(List.of(1, 2, 3));           // List<Integer> OK
sumList(List.of(1.5, 2.5));          // List<Double> OK
sumList(List.of(1L, 2L));            // List<Long> OK
```

### Lower Bounded Wildcard (`? super T`)

```java
// Accept List of Integer OR any superclass
public static void addNumbers(List<? super Integer> list) {
    list.add(1);
    list.add(2);
    list.add(3);
}

List<Number> numbers = new ArrayList<>();
addNumbers(numbers);  // OK — Number is a superclass of Integer

List<Object> objects = new ArrayList<>();
addNumbers(objects);  // OK — Object is a superclass of Integer

List<Integer> ints = new ArrayList<>();
addNumbers(ints);     // OK— Integer IS Integer

// List<String> strs = new ArrayList<>();
// addNumbers(strs);  // COMPILE ERROR!
```

### The PECS Rule

**PECS = Producer Extends, Consumer Super**

```
"If you PRODUCE items from a collection, use ? extends T"
"If you CONSUME (add) items to a collection, use ? super T"
```

```java
// Producer extends
public void copy(List<? extends T> source, List<? super T> target) {
    for (T item : source) {  // READ from source (producer)
        target.add(item);     // WRITE to target (consumer)
    }
}
```

## Type Erasure

Generics are a **compile-time** feature. The compiler removes (erases) type information:

```java
// At compile time:
List<String> strings = new ArrayList<>();

// After type erasure (runtime):
List strings = new ArrayList();  // Just List, no <String>
```

Consequences:
1. You CAN'T: `new T()`, `new T[10]`, `instanceof T`
2. You CAN: cast with `@SuppressWarnings`, use `Class<T>` parameter

```java
public class Factory<T> {
    private Class<T> type;

    public Factory(Class<T> type) {
        this.type = type;
    }

    public T create() throws Exception {
        return type.getDeclaredConstructor().newInstance();
    }
}

Factory<String> factory = new Factory<>(String.class);
String s = factory.create();  // OK — uses Class<T> as runtime type token
```

---

### Exercises

1. Create a generic `Pair<K, V>` class with getters and setters.
2. Create a generic `Stack<T>` class with push, pop, peek, isEmpty.
3. Write a generic method `reverse(List<T> list)` that reverses the list in place.
4. Write a method `countGreaterThan(T[] array, T elem)` that counts elements > elem. (Hint: T must extend Comparable)
5. Use wildcards to write a method `mergeLists(List<? extends T> source, List<? super T> target)`.
