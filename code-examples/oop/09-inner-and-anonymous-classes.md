# Inner Classes, Anonymous Classes, and Lambdas

Java supports four kinds of nested classes: member inner classes, static nested classes, local inner classes, and anonymous inner classes. Anonymous classes can often be replaced with lambdas when the interface is a functional interface (single abstract method).

```java
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

// ============================================================
// Outer class
// ============================================================

public class InnerClassDemo {

    // ============================================================
    // 1. Member inner class — has access to outer instance
    // ============================================================

    private String outerField = "outer";

    class MemberInner {
        void display() {
            // Can access private members of outer class
            System.out.println("Member inner: " + outerField);
        }
    }

    // ============================================================
    // 2. Static nested class — no outer instance needed
    // ============================================================

    static class StaticNested {
        void display() {
            System.out.println("Static nested (no outer instance)");
        }
    }

    // ============================================================
    // 3. Local inner class — defined inside a method
    // ============================================================

    void localInnerDemo() {
        final String localVar = "local";

        class LocalInner {
            void display() {
                System.out.println("Local inner: " + localVar);
            }
        }

        LocalInner li = new LocalInner();
        li.display();
    }

    // ============================================================
    // 4. Anonymous inner class — implementing an interface
    // ============================================================

    void anonymousDemo() {
        // Anonymous class implementing Runnable
        Runnable r1 = new Runnable() {
            @Override
            public void run() {
                System.out.println("Anonymous Runnable");
            }
        };
        r1.run();

        // Anonymous class extending a class
        Object o = new Object() {
            @Override
            public String toString() {
                return "Anonymous Object subclass";
            }
        };
        System.out.println(o);

        // Anonymous class as a Comparator
        List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));
        names.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return a.length() - b.length();
            }
        });
        System.out.println("Sorted by length: " + names);
    }

    // ============================================================
    // 5. Anonymous vs lambda comparison
    // ============================================================

    void lambdaComparison() {
        List<String> words = List.of("apple", "banana", "cherry");

        // Anonymous class
        words.sort(new Comparator<String>() {
            @Override
            public int compare(String a, String b) {
                return Integer.compare(a.length(), b.length());
            }
        });

        // Lambda (same thing, much shorter)
        words.sort((a, b) -> Integer.compare(a.length(), b.length()));

        // Even shorter with method reference
        words.sort(Comparator.comparingInt(String::length));

        System.out.println(words);
    }

    // ============================================================
    // 6. Main
    // ============================================================

    public static void main(String[] args) {
        InnerClassDemo outer = new InnerClassDemo();

        // Member inner — needs outer instance
        MemberInner mi = outer.new MemberInner();
        mi.display();

        // Static nested — no outer instance needed
        StaticNested sn = new StaticNested();
        sn.display();

        // Local inner
        outer.localInnerDemo();

        // Anonymous
        outer.anonymousDemo();

        // Lambda comparison
        outer.lambdaComparison();
    }
}
```
