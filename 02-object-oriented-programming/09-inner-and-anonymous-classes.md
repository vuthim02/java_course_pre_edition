# OOP — Lesson 9: Inner & Anonymous Classes

## Nested Classes Overview

A class defined INSIDE another class:

```
┌─────────────────────────────────────────────┐
│               OUTER CLASS                    │
│                                              │
│  ┌────────────────────────────────────┐      │
│  │  Static Nested Class               │      │
│  │  (like a top-level class, but      │      │
│  │   nested for packaging)            │      │
│  └────────────────────────────────────┘      │
│                                              │
│  ┌────────────────────────────────────┐      │
│  │  Inner Class (non-static)          │      │
│  │  (has access to Outer's fields)    │      │
│  └────────────────────────────────────┘      │
│                                              │
│  ┌────────────────────────────────────┐      │
│  │  Local Class (inside a method)     │      │
│  └────────────────────────────────────┘      │
│                                              │
│  ┌────────────────────────────────────┐      │
│  │  Anonymous Class (one-time use)    │      │
│  └────────────────────────────────────┘      │
└─────────────────────────────────────────────┘
```

## Static Nested Class

```java
public class Computer {
    private String model;
    private int year;

    public Computer(String model, int year) {
        this.model = model;
        this.year = year;
    }

    // Static nested — behaves like a regular class, just grouped here
    public static class Battery {
        private int capacity;

        public Battery(int capacity) {
            this.capacity = capacity;
        }

        public void charge() {
            System.out.println("Charging battery of " + capacity + "mAh");
            // System.out.println(model);  // ERROR! Can't access outer instance
        }
    }
}

// Usage:
Computer.Battery battery = new Computer.Battery(5000);
battery.charge();
```

**Use when:** The nested class makes sense only in the context of the outer class, but doesn't need access to the outer's instance fields.

## Inner Class (Non-static)

```java
public class Library {
    private String name;
    private List<String> books = new ArrayList<>();

    public Library(String name) {
        this.name = name;
    }

    public void addBook(String title) {
        books.add(title);
    }

    // Inner class — has an implicit reference to the outer object
    public class Book {
        private String title;
        private boolean isBorrowed;

        public Book(String title) {
            this.title = title;
        }

        public String getLibraryName() {
            return Library.this.name;  // Access outer's field!
        }
    }
}

// Usage:
Library lib = new Library("City Library");
Library.Book book = lib.new Book("1984");  // Note: need outer instance first
```

**Use when:** The inner class needs access to the outer class's instance fields.

## Local Class (Inside a Method)

```java
public class ShoppingCart {
    private List<Item> items = new ArrayList<>();

    public double calculateTotal() {
        // Local class — defined INSIDE a method
        class DiscountCalculator {
            private double discountRate;

            DiscountCalculator(double rate) {
                this.discountRate = rate;
            }

            double apply(double price) {
                return price * (1 - discountRate);
            }
        }

        double total = 0;
        for (Item item : items) {
            total += item.getPrice();
        }

        DiscountCalculator calc = new DiscountCalculator(0.1);
        return calc.apply(total);
    }
}
```

**Use when:** You need a temporary class with logic that's only relevant inside one method.

## Anonymous Class (Most Common!)

An **anonymous class** is a class defined AND instantiated in one expression — no name:

```java
// Instead of creating a separate "ButtonClickListener" class:
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        System.out.println("Button clicked!");
    }
});
```

### Anonymous Class Examples

```java
// 1. Overriding a method inline
Animal dog = new Animal() {
    @Override
    public void makeSound() {
        System.out.println("Woof!");
    }
};
dog.makeSound();  // "Woof!"

// 2. Implementing an interface
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Running in a thread!");
    }
};
new Thread(task).start();

// 3. Passing as argument (most common)
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String a, String b) {
        return a.length() - b.length();
    }
});

// 4. With abstract class
abstract class Logger {
    abstract void log(String msg);
}

Logger console = new Logger() {
    @Override
    void log(String msg) {
        System.out.println("[LOG] " + msg);
    }
};
console.log("Hello");
```

### Anonymous Class to Lambda (Java 8+)

```java
// Anonymous class:
button.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        System.out.println("Clicked");
    }
});

// Same thing as lambda:
button.setOnClickListener(v -> System.out.println("Clicked"));
```

---

### Exercises

1. Create an `Outer` class with an inner class `Inner`. Show how the inner can access outer's private fields.
2. Use an anonymous class to create a custom `Comparator<String>` that sorts by string length.
3. Create a `Menu` class with a `MenuItem` inner class. Each MenuItem has a click handler stored as an anonymous class.
4. Convert anonymous class examples to lambda expressions (after we cover lambdas in detail).
