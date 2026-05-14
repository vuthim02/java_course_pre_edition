# Career — Lesson 2: LeetCode & DSA for Java Interviews

## The Interview Landscape

```
FAANG (Google, Amazon, Meta, Apple, Netflix):
  • Coding (LeetCode Medium/Hard — 45 min)
  • System Design (45-60 min)
  • Behavior (Leadership Principles — 30 min)

Mid/Large Companies:
  • Coding (LeetCode Easy/Medium — 30-45 min)
  • System Design (30-45 min)
  • Project Deep Dive (30 min)

Startups:
  • Practical coding (real-world problem — 60 min)
  • Architecture discussion (30-45 min)
  • Culture fit (30 min)
```

## Java-Specific LeetCode Strategy

```java
// 1. Use proper Java idioms
// BAD — C-style
int[] result = new int[2];
for (int i = 0; i < arr.length; i++) { ... }

// GOOD — modern Java
List<Integer> result = new ArrayList<>();
for (int num : arr) { ... }

// 2. Collections API
Map<Character, Integer> freq = new HashMap<>();
freq.merge(c, 1, Integer::sum);  // Clean frequency counting

// 3. Streams for readability (when appropriate)
long count = arr.stream()
    .filter(n -> n > 0)
    .count();

// 4. Optional for null safety
String result = Optional.ofNullable(input)
    .map(String::trim)
    .orElse("");
```

## Essential Data Structures for Interviews

```java
// Arrays & Strings — 90% of problems use these
int[] arr = {1, 2, 3};
Arrays.sort(arr);
Arrays.binarySearch(arr, 2);
Arrays.copyOf(arr, arr.length);

// HashMap — O(1) lookups
Map<Character, Integer> map = new HashMap<>();
map.put('a', 1);
map.getOrDefault('b', 0);
map.containsKey('a');

// HashSet — uniqueness checks
Set<Integer> seen = new HashSet<>();
for (int num : arr) {
    if (!seen.add(num)) {
        // duplicate found
    }
}

// Stack — LIFO (brackets, DFS, undo)
Deque<Character> stack = new ArrayDeque<>();
stack.push('(');
char top = stack.pop();
boolean empty = stack.isEmpty();

// Queue — BFS, level-order
Queue<TreeNode> queue = new LinkedList<>();
queue.offer(root);
TreeNode node = queue.poll();

// PriorityQueue — top K, merge K sorted
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
PriorityQueue<Integer> maxHeap = new PriorityQueue<>((a,b) -> b - a);
minHeap.offer(5);
int smallest = minHeap.poll();
```

## Top 20 Patterns (Master These)

| Pattern | Example Problems | Technique |
|---------|-----------------|-----------|
| **Two Pointers** | Two Sum, 3Sum, Container with Most Water | Left/right pointers |
| **Sliding Window** | Longest Substring, Max Average | Expand/contract window |
| **Binary Search** | Search Rotated Array, Find Peak | O(log n) searching |
| **DFS** | Tree Paths, Island Count | Recursion/stack |
| **BFS** | Level Order, Shortest Path | Queue |
| **Backtracking** | Permutations, N-Queens | Try → undo |
| **DP** | Knapsack, Longest Common Subseq | Memoization/table |
| **Greedy** | Jump Game, Coin Change | Local optimum |
| **Trie** | Word Search, Autocomplete | Prefix tree |
| **Union Find** | Connected Components, Cycle | Disjoint sets |
| **Topological Sort** | Course Schedule | Kahn's algorithm |
| **In-place Reversal** | Reverse Linked List | Pointer manipulation |
| **Fast & Slow** | Cycle Detection, Middle Node | Floyd's algorithm |
| **Merge Intervals** | Merge, Insert, Overlap | Sort + merge |
| **Cyclic Sort** | Find Missing Number | Index mapping |
| **Matrix Traversal** | Spiral Matrix, Rotate | Direction arrays |
| **Monotonic Stack** | Next Greater, Stock Span | Stack of indices |
| **Segment Tree** | Range Sum, Range Min | Tree over array |
| **Prefix Sum** | Subarray Sum, Range Sum | Cumulative sums |
| **Bit Manipulation** | Single Number, Power of Two | XOR, shifts |

## Study Plan (12 Weeks for FAANG)

| Week | Topic | Problems |
|------|-------|----------|
| 1 | Arrays & Hashing | 10 Easy, 10 Medium |
| 2 | Two Pointers + Sliding Window | 10 Medium |
| 3 | Stack + Queue | 8 Medium |
| 4 | Linked Lists | 10 Easy/Medium |
| 5 | Trees (DFS, BFS, BST) | 15 Medium |
| 6 | Heaps + Priority Queues | 8 Medium |
| 7 | Backtracking | 8 Medium |
| 8 | Graphs | 10 Medium |
| 9 | Dynamic Programming | 15 Medium |
| 10 | Intervals + Matrix | 8 Medium |
| 11 | System Design (2 hrs/day) | 10 Design problems |
| 12 | Mock Interviews + Review | Full-length mocks |

---

### Exercises

1. Solve "Two Sum" — brute force → HashMap → two-pointer (if sorted).
2. Implement a binary search tree with insert, search, delete.
3. Solve "Longest Substring Without Repeating Characters" (sliding window).
4. Solve "Number of Islands" (DFS on grid).
5. Complete 5 LeetCode problems per day for week 1.
