# Career — Lesson 6: FAANG Preparation Strategy

## The FAANG Landscape

```
FAANG:                                Other Top Tech:
┌──────────────────────┐              ┌──────────────────────┐
│ Facebook (Meta)      │              │ Microsoft            │
│ Amazon               │              │ Uber                 │
│ Apple                │              │ Stripe               │
│ Netflix              │              │ Airbnb               │
│ Google               │              │ Dropbox              │
│                      │              │ Databricks           │
│ Also: Microsoft,     │              │ Snowflake            │
│ LinkedIn, Twitter/X, │              │ Palantir             │
│ Snap, Uber, Stripe   │              │ Robinhood            │
└──────────────────────┘              └──────────────────────┘
```

## The Interview Process

```
┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐   ┌─────────┐
│ RESUME  │──▶│ PHONE   │──▶│ ONSITE  │──▶│ TEAM   │──▶│ OFFER  │
│ SCREEN  │   │ SCREEN  │   │ (4-6    │   │ MATCH  │   │        │
│         │   │ (45 min)│   │  rounds)│   │        │   │        │
└─────────┘   └─────────┘   └─────────┘   └─────────┘   └─────────┘
    │             │              │              │            │
    ▼             ▼              ▼              ▼            ▼
 2-4 weeks     1-2 weeks     1-4 weeks     1-3 weeks    1-2 weeks
             
Total timeline: 2-4 months from application to offer
```

## Phase 1: Preparation (4-8 weeks)

### Coding Interview Prep

```markdown
**Data Structures to Master:**
- Arrays & Strings (two pointers, sliding window)
- HashMaps (O(1) lookups)
- Linked Lists (reversal, cycle detection)
- Stacks & Queues (monotonic stack)
- Trees (BST, traversals, LCA)
- Graphs (BFS, DFS, Dijkstra, topological sort)
- Heaps (top-K, median finding)
- Tries (prefix search)
- Union-Find (disjoint sets)

**Algorithms to Master:**
- Binary search (and its variants)
- Sorting (quick, merge, counting)
- Recursion & backtracking
- Dynamic programming (memoization, tabulation)
- Greedy algorithms
- Sliding window & two pointers
- BFS/DFS on trees and graphs
```

### Java-Specific Preparation

```java
// Know your standard library cold
// Collections
List<String> list = new ArrayList<>();
Set<Integer> set = new HashSet<>();
Map<String, Integer> map = new HashMap<>();
Queue<Task> queue = new LinkedList<>();
Deque<Integer> deque = new ArrayDeque<>();
PriorityQueue<Integer> minHeap = new PriorityQueue<>();
PriorityQueue<Integer> maxHeap = new PriorityQueue<>(Comparator.reverseOrder());

// Streams (write concise code)
List<String> result = list.stream()
    .filter(s -> s.length() > 3)
    .map(String::toUpperCase)
    .sorted()
    .collect(Collectors.toList());

// Concurrency
synchronized (lock) { /* critical section */ }
new Thread(() -> { /* ... */ }).start();
CompletableFuture.supplyAsync(() -> fetchData())
    .thenApply(data -> process(data))
    .exceptionally(e -> fallback());
```

### System Design Prep

See Lesson 3. Focus on:

```markdown
1. URL Shortener
2. Design WhatsApp/Chat
3. Design Uber/Lyft
4. Design Twitter Feed
5. Design YouTube/Netflix
6. Design Web Crawler
7. Design Key-Value Store
8. Design Rate Limiter
```

### Behavioral Preparation

```markdown
**The STAR Method:**

S - Situation (context)
T - Task (what needed to be done)
A - Action (what YOU did)
R - Result (measurable outcome)

**Common Questions:**
- "Tell me about a challenging project"
- "Describe a conflict with a teammate"
- "Tell me about a time you failed"
- "Why do you want to work here?"
- "Describe a time you showed leadership"
```

## Phase 2: Application (1-2 weeks)

```markdown
**Referral (BEST):**
- 10x higher chance of interview
- Ask former colleagues, friends, LinkedIn connections
- Attend meetups and conferences

**Direct Application:**
- careers.google.com, amazon.jobs, etc.
- Use LinkedIn Easy Apply
- Apply to multiple positions

**Recruiters:**
- Set LinkedIn to "Open to Work"
- Connect with company recruiters
- Reply within 24 hours
```

## Phase 3: Interview Rounds

### Coding Round (45 min)

```java
/*
Problem: Two Sum (but with a twist)
Given an array and target, return indices of two numbers that sum to target.
All solutions should be in 15-20 minutes.

Framework:
1. CLARIFY — "Can I assume exactly one solution?"
2. THINK OUT LOUD — "Brute force O(n²), let me optimize to O(n)"
3. CODE — Clean, readable, handle edge cases
4. TEST — Walk through example, check edge cases
5. OPTIMIZE — "Space O(n), time O(n) with HashMap"
*/

public int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> map = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
        int complement = target - nums[i];
        if (map.containsKey(complement)) {
            return new int[]{map.get(complement), i};
        }
        map.put(nums[i], i);
    }
    throw new IllegalArgumentException("No solution");
}
```

### System Design Round (45-60 min)

See Lesson 3. Follow the framework and PRACTICE OUT LOUD.

### Behavioral Round (30-45 min)

```markdown
Prepare 5-7 STAR stories covering:
1. Technical challenge overcame
2. Conflict resolution
3. Leadership / initiative
4. Failure and learning
5. Cross-team collaboration
6. Mentoring others
7. Customer focus

Example STAR:
S: Our payment system had 5% failure rate during peak hours
T: Reduce failure rate below 1% without adding infrastructure
A: Implemented retry with exponential backoff, circuit breaker,
   and async queue for payment processing
R: Failure rate dropped to 0.3%, saved $2M/month in lost revenue
```

## The Java-Specific Edge

```markdown
FAANG loves Java engineers because:
- Most backend systems are Java (Google, Amazon, Netflix)
- Java's type system prevents entire classes of bugs
- Java's tooling (profiling, monitoring) is mature
- Spring Boot is the standard at many non-FAANG tech companies

**Highlight these in interviews:**
- JVM performance tuning experience
- Building high-throughput systems
- Concurrency and thread safety
- Large-scale Spring Boot microservices
- Database optimization (JPA/SQL)
```

## Timeline

```markdown
Week 1-4:   LeetCode (1-2 Easy/day → 1 Medium/day)
Week 5-6:   System Design (1 design/day, practice out loud)
Week 7:     Behavioral prep (write STAR stories, practice)
Week 8:     Mock interviews (with peers or services like Pramp)
Week 9:     Apply broadly (10-20 companies)
Week 10-14: Phone screens (coding + behavioral)
Week 15-20: Onsite interviews
Week 21-23: Negotiation (NEVER accept first offer)
```

## Negotiation Tips

```markdown
**Never accept the first offer.** Always negotiate.

1. Get multiple offers (leverage!)
2. Focus on total comp (base + bonus + RSUs)
3. Use competing offers as leverage
4. Negotiate in writing (email)
5. Be respectful and professional
6. Ask for: higher base, signing bonus, RSU refreshers

Example: "I'm excited about this role. Based on my experience
and competing offers, I was hoping for a total comp closer to
$XXX. Can you work with that?"
```

## Exercises

1. Solve 2 LeetCode problems daily (Easy → Medium → Hard progression).
2. Write down 5 STAR stories for behavioral questions.
3. Practice system design out loud (record yourself).
4. Do a mock coding interview with a friend or on Pramp.
5. Research 3 target companies and prepare "why do you want to work here?"
