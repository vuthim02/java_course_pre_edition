# Collections Framework

The Java Collections Framework provides data structures for storing, retrieving, and manipulating groups of objects. Understand `List`, `Set`, `Map`, `Queue`, the `Collections` utility class, and `Comparable` vs `Comparator`. Java 21 introduced `SequencedCollection`, `SequencedSet`, and `SequencedMap` for collections with a defined encounter order.

```java
import java.util.*;
import java.util.stream.Collectors;

// ============================================================
// 1. List — ordered, allows duplicates
// ============================================================

class ListDemo {
    static void demo() {
        // --- ArrayList ---
        List<String> al = new ArrayList<>();
        al.add("A"); al.add("B"); al.add("C"); al.add("A");
        System.out.println("ArrayList: " + al);

        // Capacity management
        ArrayList<Integer> nums = new ArrayList<>();
        nums.ensureCapacity(1000);           // pre-allocate to avoid resizing
        for (int i = 0; i < 100; i++) nums.add(i);
        nums.trimToSize();                   // shrink to actual size
        System.out.println("Size = " + nums.size() + ", trimmed");

        // --- LinkedList (also a Queue) ---
        Queue<String> queue = new LinkedList<>();
        queue.offer("first");
        queue.offer("second");
        queue.offer("third");
        System.out.println("Queue poll: " + queue.poll());   // first
        System.out.println("Queue peek: " + queue.peek());   // second
    }
}

// ============================================================
// 2. Set — no duplicates
// ============================================================

class SetDemo {
    static void demo() {
        // HashSet — O(1), no order guarantees
        Set<String> hashSet = new HashSet<>(Set.of("C", "A", "B", "A"));
        System.out.println("HashSet: " + hashSet);           // [A, B, C] (unordered)

        // LinkedHashSet — insertion order preserved
        Set<String> linkedSet = new LinkedHashSet<>(List.of("C", "A", "B"));
        System.out.println("LinkedHashSet: " + linkedSet);   // [C, A, B]

        // TreeSet — sorted order (natural or Comparator)
        Set<String> treeSet = new TreeSet<>(List.of("C", "A", "B"));
        System.out.println("TreeSet: " + treeSet);           // [A, B, C]

        // TreeSet with custom Comparator
        Set<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.addAll(List.of("aaa", "b", "cc"));
        System.out.println("TreeSet by length: " + byLength); // [b, cc, aaa]
    }
}

// ============================================================
// 3. Map — key-value pairs
// ============================================================

class MapDemo {
    static void demo() {
        // HashMap
        Map<String, Integer> hashMap = new HashMap<>();
        hashMap.put("Alice", 30);
        hashMap.put("Bob", 25);
        hashMap.put("Charlie", 35);
        System.out.println("HashMap: " + hashMap);

        // LinkedHashMap — insertion order
        Map<String, Integer> linkedMap = new LinkedHashMap<>();
        linkedMap.put("one", 1);
        linkedMap.put("two", 2);
        linkedMap.put("three", 3);
        System.out.println("LinkedHashMap: " + linkedMap);   // {one=1, two=2, three=3}

        // TreeMap — sorted by keys
        Map<String, Integer> treeMap = new TreeMap<>(hashMap);
        System.out.println("TreeMap: " + treeMap);           // {Alice=30, Bob=25, Charlie=35}

        // EnumMap — array-backed, extremely fast
        Map<Day, String> enumMap = new EnumMap<>(Day.class);
        enumMap.put(Day.MONDAY, "Work");
        enumMap.put(Day.SATURDAY, "Rest");
        System.out.println("EnumMap: " + enumMap);
    }

    enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }
}

// ============================================================
// 4. Queue / Deque
// ============================================================

class QueueDemo {
    static void demo() {
        // PriorityQueue — heap-based, ordered by natural order or Comparator
        Queue<Integer> pq = new PriorityQueue<>();
        pq.offer(30);
        pq.offer(10);
        pq.offer(20);
        System.out.print("PriorityQueue: ");
        while (!pq.isEmpty()) System.out.print(pq.poll() + " ");   // 10 20 30
        System.out.println();

        // ArrayDeque — double-ended queue, faster than Stack and LinkedList
        Deque<String> deque = new ArrayDeque<>();
        deque.addFirst("front");
        deque.addLast("middle");
        deque.addLast("back");
        System.out.println("Deque pollFirst: " + deque.pollFirst()); // front
        System.out.println("Deque pollLast: " + deque.pollLast());   // back
    }
}

// ============================================================
// 5. Comparable vs Comparator
// ============================================================

class Person implements Comparable<Person> {
    String name;
    int age;

    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public int compareTo(Person other) {
        return Integer.compare(this.age, other.age);   // natural: by age
    }

    @Override
    public String toString() {
        return name + "(" + age + ")";
    }
}

class CompareDemo {
    static void demo() {
        var people = new ArrayList<>(List.of(
            new Person("Alice", 30),
            new Person("Bob", 25),
            new Person("Charlie", 35)
        ));

        Collections.sort(people);                         // uses Comparable (age)
        System.out.println("Sorted by age: " + people);

        people.sort(Comparator.comparing(p -> p.name));    // Comparator (name)
        System.out.println("Sorted by name: " + people);

        // Chaining comparators
        people.sort(Comparator.comparingInt((Person p) -> p.name.length())
                    .thenComparing(p -> p.name));
        System.out.println("By name length then name: " + people);
    }
}

// ============================================================
// 6. Collections utility class
// ============================================================

class CollectionsUtilDemo {
    static void demo() {
        List<Integer> list = new ArrayList<>(List.of(5, 3, 1, 4, 2));

        Collections.sort(list);
        System.out.println("Sorted: " + list);

        int idx = Collections.binarySearch(list, 3);
        System.out.println("Index of 3: " + idx);

        Collections.reverse(list);
        System.out.println("Reversed: " + list);

        Collections.shuffle(list, new Random(42));
        System.out.println("Shuffled: " + list);

        System.out.println("Min: " + Collections.min(list));
        System.out.println("Max: " + Collections.max(list));
        System.out.println("Frequency of 3: " + Collections.frequency(list, 3));

        List<Integer> other = List.of(1, 2);
        System.out.println("Disjoint: " + Collections.disjoint(list, other));

        // Unmodifiable views
        List<Integer> unmod = Collections.unmodifiableList(list);
        // unmod.add(10);  // UnsupportedOperationException

        // Synchronized wrappers
        List<Integer> sync = Collections.synchronizedList(new ArrayList<>());
    }
}

// ============================================================
// 7. Fail-fast iterator (ConcurrentModificationException)
// ============================================================

class FailFastDemo {
    static void demo() {
        List<String> list = new ArrayList<>(List.of("A", "B", "C"));
        try {
            for (String s : list) {
                if (s.equals("B")) {
                    list.remove(s);        // ConcurrentModificationException!
                }
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("Caught: " + e);
        }

        // Safe removal: use Iterator.remove()
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equals("A")) {
                it.remove();               // safe
            }
        }
        System.out.println("After safe remove: " + list);  // [B, C]
    }
}

// ============================================================
// 8. Java 21+ SequencedCollection, SequencedSet, SequencedMap
// ============================================================

class SequencedDemo {
    static void demo() {
        // SequencedCollection — first/last, reversed order
        SequencedCollection<String> seq = new ArrayList<>(List.of("A", "B", "C"));
        System.out.println("First: " + seq.getFirst());    // A
        System.out.println("Last: " + seq.getLast());      // C
        seq.addFirst("Z");
        seq.addLast("D");
        System.out.println("After addFirst/addLast: " + seq);  // [Z, A, B, C, D]
        System.out.println("Reversed: " + seq.reversed());     // [D, C, B, A, Z]

        // SequencedSet
        SequencedSet<String> seqSet = new LinkedHashSet<>(List.of("A", "B", "C"));
        System.out.println("SequencedSet first: " + seqSet.getFirst());

        // SequencedMap
        SequencedMap<String, Integer> seqMap = new LinkedHashMap<>();
        seqMap.put("one", 1);
        seqMap.put("two", 2);
        seqMap.put("three", 3);
        System.out.println("SequencedMap firstEntry: " + seqMap.firstEntry());
        System.out.println("SequencedMap lastEntry: " + seqMap.lastEntry());
        System.out.println("SequencedMap reversed: " + seqMap.reversed());
    }
}

// ============================================================
// Main
// ============================================================

public class CollectionsFrameworkDemo {
    public static void main(String[] args) {
        ListDemo.demo();
        SetDemo.demo();
        MapDemo.demo();
        QueueDemo.demo();
        CompareDemo.demo();
        CollectionsUtilDemo.demo();
        FailFastDemo.demo();
        SequencedDemo.demo();
    }
}
```
