# Java Foundations — Lesson 6: Control Flow

## What is Control Flow?

Your program runs **top to bottom** by default. Control flow statements let you:
- **Branch** — run different code based on conditions
- **Loop** — repeat code multiple times
- **Jump** — break out of or skip parts of code

```
WITHOUT CONTROL FLOW:
  ┌──────────────────┐
  │ Step 1           │
  │ Step 2           │
  │ Step 3           │
  │ Step 4           │
  │ Step 5           │  ← Always runs every step
  └──────────────────┘

WITH CONTROL FLOW:
  ┌──────────────────┐
  │ Step 1           │
  │ Is it raining?───┤──YES──▶ Take umbrella
  │    │ NO          │
  │    ▼             │
  │ Step 2           │
  │ Loop 10 times ──▶│──▶ Steps 3-5 repeat
  └──────────────────┘
```

## The `if` Statement

### Basic if

```java
int age = 18;

if (age >= 18) {
    System.out.println("You can vote!");
}
// (nothing happens if age < 18)
```

### if-else

```java
int temperature = 30;

if (temperature > 25) {
    System.out.println("It's hot outside!");
} else {
    System.out.println("It's not too hot.");
}
```

### if-else if-else (Chain)

```java
int score = 85;

if (score >= 90) {
    System.out.println("Grade: A");
} else if (score >= 80) {
    System.out.println("Grade: B");
} else if (score >= 70) {
    System.out.println("Grade: C");
} else if (score >= 60) {
    System.out.println("Grade: D");
} else {
    System.out.println("Grade: F");
}
```

### Nested if

```java
boolean loggedIn = true;
boolean isAdmin = false;

if (loggedIn) {
    if (isAdmin) {
        System.out.println("Welcome, admin!");
    } else {
        System.out.println("Welcome, user!");
    }
} else {
    System.out.println("Please log in.");
}
```

### Common Mistake: `=` vs `==`

```java
// BUG: Single = is ASSIGNMENT, not comparison
if (age = 18) {        // COMPILE ERROR: age = 18 returns 18 (int), not boolean
    // ...
}

// CORRECT: Double == is comparison
if (age == 18) {       // OK: returns boolean
    // ...
}

// DEFENSIVE: Put constant first (avoids the bug)
if (18 == age) {       // OK: if you accidentally write 18 = age → compile error
    // ...
}
```

## The `switch` Statement

### Traditional switch

```java
int dayOfWeek = 3;

switch (dayOfWeek) {
    case 1:
        System.out.println("Monday");
        break;
    case 2:
        System.out.println("Tuesday");
        break;
    case 3:
        System.out.println("Wednesday");
        break;
    case 4:
        System.out.println("Thursday");
        break;
    case 5:
        System.out.println("Friday");
        break;
    case 6:
    case 7:
        System.out.println("Weekend!");
        break;
    default:
        System.out.println("Invalid day");
        break;
}
```

**Fall-through:** If you omit `break`, execution continues to the next case!

```java
int x = 1;
switch (x) {
    case 1:
        System.out.println("One");   // Prints "One"
        // NO break → falls through!
    case 2:
        System.out.println("Two");   // Also prints "Two"!
        break;
}
// Output: "One" "Two"  ← Probably not what you wanted!
```

### Modern switch (Java 14+, arrow syntax)

```java
switch (dayOfWeek) {
    case 1 -> System.out.println("Monday");
    case 2 -> System.out.println("Tuesday");
    case 3 -> System.out.println("Wednesday");
    case 4 -> System.out.println("Thursday");
    case 5 -> System.out.println("Friday");
    case 6, 7 -> System.out.println("Weekend!");
    default -> System.out.println("Invalid");
}
// Arrow syntax: NO fall-through, NO break needed!
```

### switch as Expression (Java 14+)

```java
String dayName = switch (dayOfWeek) {
    case 1 -> "Monday";
    case 2 -> "Tuesday";
    case 3 -> "Wednesday";
    case 4 -> "Thursday";
    case 5 -> "Friday";
    case 6, 7 -> "Weekend";
    default -> "Invalid";
};
System.out.println(dayName);  // "Wednesday"
```

### Pattern Matching for switch (Java 21+)

```java
Object obj = "Hello, Java 21!";

String result = switch (obj) {
    case Integer i -> "Integer: " + i;
    case String s -> "String of length " + s.length();
    case Long l -> "Long: " + l;
    case null -> "It's null!";
    default -> "Unknown type";
};
```

## Loops

### The `while` Loop

"Keep doing this WHILE the condition is true."

```java
int count = 1;
while (count <= 5) {
    System.out.println("Count: " + count);
    count++;  // DON'T forget this — infinite loop otherwise!
}
// Output: Count: 1, 2, 3, 4, 5
```

```
Execution flow:
          ┌─────────┐
          │ count=1 │
          └────┬────┘
               │
          ┌────▼────┐
  ┌───────│ count   │──────NO──────▶ Done
  │       │ <= 5?   │
  │       └────┬────┘
  │            │ YES
  │            ▼
  │   ┌────────────────┐
  │   │ Print count    │
  │   │ count++        │
  │   └────────┬───────┘
  └────────────┘
```

### The `do-while` Loop

"Execute the body FIRST, then check condition."

```java
int x = 10;
do {
    System.out.println("This runs at least once!");
    x++;
} while (x < 5);
// Output: "This runs at least once!"
// (even though 10 < 5 is false, the body runs once)
```

**When to use:** When you need the body to execute at least once (e.g., menu systems, input validation).

### The `for` Loop

"Most common loop — great when you know how many iterations."

```java
for (int i = 0; i < 5; i++) {
    System.out.println("Iteration: " + i);
}
// Output: Iteration: 0, 1, 2, 3, 4
```

```
Anatomy:  for (initialization; condition; update)
               ↓               ↓           ↓
          ┌────────────┐ ┌──────────┐ ┌─────────┐
          │int i = 0;  │ │ i < 5;   │ │ i++     │
          │ (once)     │ │ (check   │ │ (after  │
          │            │ │  each    │ │  each   │
          │            │ │  time)   │ │  body)  │
          └────────────┘ └──────────┘ └─────────┘

Execution: ① Init → ② Check → ③ Body → ④ Update
                           ↕        ↑
                         TRUE      │
                           │        │
                           └── ④ ←─┘
                          FALSE → Exit
```

### The Enhanced `for`-each Loop

"For iterating over arrays and collections."

```java
int[] numbers = {10, 20, 30, 40, 50};

for (int num : numbers) {
    System.out.println(num);
}
// Output: 10, 20, 30, 40, 50
```

### Nested Loops

```java
// Multiplication table
for (int i = 1; i <= 10; i++) {
    for (int j = 1; j <= 10; j++) {
        System.out.printf("%4d", i * j);
    }
    System.out.println();  // new line after each row
}
```

## Loop Control: `break` and `continue`

```java
// break — EXIT the loop immediately
for (int i = 0; i < 10; i++) {
    if (i == 5) {
        break;   // Loop stops when i reaches 5
    }
    System.out.print(i + " ");
}
// Output: 0 1 2 3 4

// continue — SKIP to the next iteration
for (int i = 0; i < 10; i++) {
    if (i % 2 == 0) {
        continue;  // Skip even numbers
    }
    System.out.print(i + " ");
}
// Output: 1 3 5 7 9
```

### Labeled break/continue (Rare, but powerful)

```java
outer:  // ← this is a LABEL
for (int i = 0; i < 3; i++) {
    for (int j = 0; j < 3; j++) {
        if (i == 1 && j == 1) {
            break outer;  // breaks BOTH loops!
        }
        System.out.println(i + "," + j);
    }
}
// Output: 0,0  0,1  0,2  1,0  (stops at 1,1)
```

## Infinite Loops (And How to Avoid Them)

```java
// BUG: Infinite loop!
int i = 1;
while (i <= 10) {
    System.out.println(i);
    // MISSING: i++
}
// Runs forever — i never changes!

// Intentional infinite loop (valid):
while (true) {
    // Server loop, game loop, etc.
    // Must have break condition inside
    String command = readInput();
    if (command.equals("quit")) {
        break;
    }
}
```

---

### Exercises

1. **FizzBuzz** — Print numbers 1-100. If divisible by 3, print "Fizz". If by 5, "Buzz". If by both, "FizzBuzz".
2. **Number Guessing Game** — Generate a random number 1-100. User guesses. Tell "higher" or "lower" until correct. Count guesses.
3. **Multiplication Table** — Print a 10×10 multiplication table using nested loops.
4. **Diamond Pattern** — Write nested loops that print:
   ```
       *
      ***
     *****
    *******
     *****
      ***
       *
   ```
5. **Switch Expression** — Take a month number (1-12) and return the season using modern switch expression syntax.
