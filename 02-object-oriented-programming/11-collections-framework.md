# OOP — Lesson 11: Collections Framework

## What is the Collections Framework?

The Java Collections Framework provides a standard set of **interfaces** and **implementations** for storing, retrieving, and manipulating groups of objects.

```
                        COLLECTIONS FRAMEWORK
┌─────────────────────────────────────────────────────────────────┐
│                        INTERFACES                               │
│                                                                  │
│  ┌──────────┐   ┌──────────┐   ┌──────────┐   ┌──────────┐    │
│  │ Iterable │   │          │   │          │   │          │    │
│  └─────┬────┘   │          │   │          │   │          │    │
│        │         │          │   │          │   │          │    │
│  ┌─────▼────┐   │          │   │          │   │          │    │
│  │Collection│   │   Map    │   │  Queue   │   │  Deque   │    │
│  └─────┬────┘   │          │   │          │   │          │    │
│        │         └──────────┘   └──────────┘   └──────────┘    │
│   ┌────┼────┐                                                   │
│   │    │    │                                                   │
│   ▼    ▼    ▼                                                   │
│  List  Set  Queue                                               │
│              ↑                                                  │
│           Deque                                                 │
└─────────────────────────────────────────────────────────────────┘
```

## The Core Interfaces

```java
// Collection — the root interface
Collection<String> c = new ArrayList<>();
c.add("one");
c.remove("one");
c.size();          // number of elements
c.isEmpty();       // true if empty
c.contains("one"); // check membership
c.clear();         // remove all
c.toArray();       // convert to array

// List — ordered, allows duplicates, indexed
List<String> list = new ArrayList<>();
list.get(0);       // element at index
list.set(0, "a");  // replace at index
list.add(0, "b");  // insert at index
list.indexOf("a"); // find index
list.subList(0, 2); // view of portion

// Set — no duplicates
Set<String> set = new HashSet<>();
set.add("a"); set.add("b"); set.add("a"); // "a" added once
// No get(index) — no ordering guarantees

// Queue — FIFO (First In, First Out)
Queue<String> queue = new LinkedList<>();
queue.offer("a");  // add (returns false if full)
queue.poll();      // retrieve and remove (returns null if empty)
queue.peek();      // retrieve but don't remove

// Deque — double-ended queue
Deque<String> deque = new ArrayDeque<>();
deque.addFirst("a");
deque.addLast("b");
deque.removeFirst();
deque.removeLast();

// Map — key-value pairs
Map<String, Integer> map = new HashMap<>();
map.put("Alice", 30);
map.get("Alice");    // 30
map.get("Bob");      // null (not found)
map.getOrDefault("Bob", 0); // 0
map.containsKey("Alice"); // true
map.keySet();   // Set of keys
map.values();   // Collection of values
```

## Implementations — Choosing the Right One

### List Implementations

| Implementation | Internal | Get by Index | Insert/Delete | Iteration | Memory |
|---------------|----------|-------------|---------------|-----------|--------|
| **ArrayList** | Dynamic array | ⚡ O(1) | 🐌 O(n) at front, ⚡ O(1) at end | ⚡ Fast | Low |
| **LinkedList** | Doubly-linked list | 🐌 O(n) | ⚡ O(1) at ends | 🐌 Slow | High |
| **Vector** | Synchronized array | ⚡ O(1) | 🐌 O(n) | ⚡ Fast | Low |

**ArrayList is the default choice for 95% of use cases.**

### Set Implementations

| Implementation | Ordering | Duplicates | Null | Performance |
|---------------|----------|------------|------|-------------|
| **HashSet** | Unordered (hash-based) | No | One null | ⚡ O(1) |
| **LinkedHashSet** | Insertion order | No | One null | 😐 O(1) |
| **TreeSet** | Sorted (natural/comparator) | No | No nulls | 🐌 O(log n) |

### Map Implementations

| Implementation | Ordering | Null Keys | Performance | Use Case |
|---------------|----------|-----------|-------------|----------|
| **HashMap** | Unordered | One null key | ⚡ O(1) | Default choice |
| **LinkedHashMap** | Insertion/access order | One null key | 😐 O(1) | LRU cache |
| **TreeMap** | Sorted (natural/comparator) | No null keys | 🐌 O(log n) | Sorted keys |
| **ConcurrentHashMap** | Unordered | No null | ⚡ O(1) | Thread-safe |
| **EnumMap** | Enum constant order | No null | ⚡ O(1) | Enum keys |
| **IdentityHashMap** | Reference equality | Yes | ⚡ O(1) | Identity comparison |

### Queue/Deque Implementations

| Implementation | Type | Bounded | Thread-safe | Use Case |
|---------------|------|---------|-------------|----------|
| **LinkedList** | FIFO Queue / Deque | No | No | General queue |
| **ArrayDeque** | Resizable deque | No | No | Fast stack/queue |
| **PriorityQueue** | Priority heap | No | No | Task scheduling |
| **ArrayBlockingQueue** | FIFO bounded | Yes | Yes | Producer-consumer |
| **ConcurrentLinkedQueue** | FIFO | No | Yes | High-throughput |

## HashMap Deep Dive — How It Works

```
HashMap<String, Integer> ages = new HashMap<>();
ages.put("Alice", 30);

INTERNAL STRUCTURE:

Bucket Array (default 16 entries):
┌────┐
│  0 │ → null
├────┤
│  1 │ → Node("Bob", 25) → Node("Charlie", 35)  ← linked list
├────┤
│  2 │ → Node("Alice", 30)
├────┤
│  3 │ → null
├────┤
│ ...│
└────┘

When you call: ages.put("Alice", 30)
1. hash = hash("Alice")        → some int
2. index = hash & (n-1)        → bucket index
3. Check if key exists at that bucket
   - If yes: replace value
   - If no: add new node
4. If linked list > 8: convert to TREE (Java 8+)
5. If load > 0.75: RESIZE (double buckets, rehash all)
```

## Iterating Collections

```java
List<String> list = Arrays.asList("a", "b", "c", "d");

// 1. For loop with index (List only)
for (int i = 0; i < list.size(); i++) {
    System.out.println(list.get(i));
}

// 2. Enhanced for-each (any Collection)
for (String s : list) {
    System.out.println(s);
}

// 3. Iterator (removal safe!)
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    String s = it.next();
    if (s.equals("b")) {
        it.remove();  // SAFE removal during iteration
    }
}

// 4. forEach (Java 8+)
list.forEach(s -> System.out.println(s));
list.forEach(System.out::println);  // Method reference

// 5. Stream (Java 8+)
list.stream()
    .filter(s -> s.startsWith("a"))
    .forEach(System.out::println);
```

### Iterating Maps

```java
Map<String, Integer> map = Map.of("Alice", 30, "Bob", 25);

// 1. forEach with BiConsumer
map.forEach((name, age) -> System.out.println(name + ": " + age));

// 2. Entry set
for (Map.Entry<String, Integer> entry : map.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}

// 3. Key set + get (slower — don't use!)
for (String key : map.keySet()) {
    System.out.println(key + ": " + map.get(key));  // O(n) per get!
}
```

## Utility Methods

```java
// Creating collections (Java 9+)
List<String> list = List.of("a", "b", "c");       // IMMUTABLE
Set<Integer> set = Set.of(1, 2, 3);                // IMMUTABLE
Map<String, Integer> map = Map.of("a", 1, "b", 2); // IMMUTABLE

// Collections utility class
List<String> syncList = Collections.synchronizedList(new ArrayList<>());
List<String> unmodList = Collections.unmodifiableList(list);
String max = Collections.max(list);
String min = Collections.min(list);
Collections.shuffle(list);
Collections.reverse(list);
Collections.sort(list);
Collections.fill(list, "x");
```

## Common Pitfalls

```java
// 1. ConcurrentModificationException
List<String> list = new ArrayList<>(List.of("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);  // BANG! ConcurrentModificationException!
    }
}
// Fix: use Iterator.remove() or removeIf()

// 2. Using mutable objects as HashMap keys
List<String> key = new ArrayList<>();
key.add("a");
Map<List<String>, String> map = new HashMap<>();
map.put(key, "value");
key.add("b");  // BOOM! hashCode changed, can't find key anymore!
// Fix: use IMMUTABLE keys

// 3. Comparing with == instead of equals() in contains()
Set<String> set = new HashSet<>();
set.add(new String("hello"));
set.contains(new String("hello"));  // TRUE (uses equals())
```

---

### Exercises

1. Create a `Student` class and store students in a `HashSet`. Override equals() and hashCode() properly.
2. Count word frequencies in a sentence using `HashMap<String, Integer>`.
3. Implement a last-in-first-out cache using `LinkedHashMap` (override removeEldestEntry).
4. Use `PriorityQueue` to implement a task scheduler where tasks have priorities.
5. Merge two sorted lists using a `TreeSet`.
6. Benchmark `ArrayList` vs `LinkedList` for: add at end, add at beginning, get by index, iterate all.
