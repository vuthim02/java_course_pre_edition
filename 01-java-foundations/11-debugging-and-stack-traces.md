# Java Foundations — Lesson 11: Debugging & Stack Traces

## The Debugging Mindset

**"It's not a bug — it's the program running as written. You just wrote the wrong thing."**

Debugging is the skill of figuring out what the computer is ACTUALLY doing vs what you THINK you told it to do.

## Reading Stack Traces

A stack trace shows the **call stack** at the moment an exception occurred. It's your #1 debugging tool.

```java
public class DebugDemo {
    public static void main(String[] args) {
        methodA();
    }

    public static void methodA() {
        methodB();
    }

    public static void methodB() {
        methodC();
    }

    public static void methodC() {
        int[] arr = {1, 2, 3};
        System.out.println(arr[5]);  // BUG: index 5 doesn't exist!
    }
}
```

Output:
```
Exception in thread "main" java.lang.ArrayIndexOutOfBoundsException:
  Index 5 out of bounds for length 3
    at DebugDemo.methodC(DebugDemo.java:14)     ← THE BUG
    at DebugDemo.methodB(DebugDemo.java:10)     ← called by
    at DebugDemo.methodA(DebugDemo.java:6)      ← called by
    at DebugDemo.main(DebugDemo.java:3)         ← entry point
```

### How to Read a Stack Trace

```
LINE 1:  Exception type + thread
         java.lang.ArrayIndexOutOfBoundsException

LINE 2:  Detail message
         Index 5 out of bounds for length 3

LINE 3+: Stack frames (most recent first)
    at ClassName.methodName(FileName:LineNumber)
    ↑             ↑              ↑
  The class    The method      Exact file + line of the bug

CAUSE:    Read TOP to BOTTOM
          Top = where the error OCCURRED
          Bottom = how execution GOT there
```

## Common Exception Types

| Exception | Meaning | Typical Cause |
|-----------|---------|---------------|
| `NullPointerException` | Called method/field on null | Forgot to initialize an object |
| `ArrayIndexOutOfBoundsException` | Index outside array bounds | Off-by-one error |
| `StringIndexOutOfBoundsException` | Index outside string bounds | Wrong substring args |
| `ArithmeticException` | Math error | Division by zero |
| `NumberFormatException` | Can't parse string to number | Invalid input |
| `ClassCastException` | Invalid type cast | Wrong instanceof check |
| `IllegalArgumentException` | Bad argument passed | Validation failure |
| `FileNotFoundException` | File doesn't exist | Wrong path |
| `StackOverflowError` | Stack full | Infinite recursion |
| `OutOfMemoryError` | Heap full | Memory leak |

## Debugging Techniques

### 1. Print Debugging (The Old Reliable)

```java
public static int calculateTotal(int[] prices) {
    System.out.println("DEBUG: calculateTotal called with " + Arrays.toString(prices));
    int total = 0;
    for (int i = 0; i < prices.length; i++) {
        System.out.println("DEBUG: i=" + i + ", price=" + prices[i] + ", total=" + total);
        total += prices[i];
    }
    System.out.println("DEBUG: returning total=" + total);
    return total;
}
```

### 2. Assertions (Runtime Checks)

```java
public static void setAge(int age) {
    assert age >= 0 && age <= 150 : "Invalid age: " + age;
    // Only runs with -ea (enable assertions) flag
    this.age = age;
}
```

Enable: `java -ea MyApp`

### 3. IntelliJ Debugger (Essential Skill)

**Key commands:**
- **Breakpoint** — Click left gutter (or Ctrl+F8). Execution pauses here.
- **Step Over (F8)** — Execute current line, go to next line
- **Step Into (F7)** — Enter the method being called
- **Step Out (Shift+F8)** — Finish current method, return to caller
- **Resume (F9)** — Continue to next breakpoint
- **Evaluate Expression (Alt+F8)** — Run any code at the breakpoint

### 4. Conditional Breakpoints

Right-click a breakpoint → set condition:

```
i == 50   ← Break only when loop counter is 50
```

### 5. Watch Variables

In debug mode, you can see ALL local variables and their current values. Or add specific "watches."

## Common Bugs & How to Fix Them

### Bug 1: Off-by-One

```java
// BUG: Should be <, not <=
int[] arr = {1, 2, 3};
for (int i = 0; i <= arr.length; i++) {  // ← BUG! <= causes extra iteration
    System.out.println(arr[i]);  // ArrayIndexOutOfBoundsException at i=3
}

// FIX:
for (int i = 0; i < arr.length; i++) {
```

### Bug 2: NullPointerException

```java
// BUG: Object might be null
String name = getUserName();  // Returns null if not found
System.out.println(name.toUpperCase());  // NPE!

// FIX:
if (name != null) {
    System.out.println(name.toUpperCase());
} else {
    System.out.println("Name not found");
}

// Or use Optional:
Optional.ofNullable(name)
    .map(String::toUpperCase)
    .ifPresentOrElse(
        System.out::println,
        () -> System.out.println("Name not found")
    );
```

### Bug 3: Integer Division

```java
// BUG: Expecting 0.333, getting 0
double result = 1 / 3;  // = 0.0 (both operands are int!)

// FIX:
double result = 1.0 / 3;    // = 0.333...
double result2 = (double) 1 / 3;  // = 0.333...
```

### Bug 4: String Comparison with ==

```java
// BUG: Using == instead of .equals()
String input = new String("yes");
if (input == "yes") {  // FALSE! Different objects
    // ...
}

// FIX:
if ("yes".equals(input)) {  // TRUE
    // ...
}
```

### Bug 5: Modifying Collection While Iterating

```java
// BUG: ConcurrentModificationException
List<String> list = new ArrayList<>(Arrays.asList("a", "b", "c"));
for (String s : list) {
    if (s.equals("b")) {
        list.remove(s);  // Can't remove while iterating!
    }
}

// FIX 1: Use Iterator explicitly
Iterator<String> it = list.iterator();
while (it.hasNext()) {
    if (it.next().equals("b")) {
        it.remove();
    }
}

// FIX 2: Use removeIf (Java 8+)
list.removeIf(s -> s.equals("b"));
```

## Systematic Debugging Workflow

```
1. REPRODUCE
   └─ Can you make it happen consistently?
      └─ If not, find the exact conditions

2. ISOLATE
   └─ Which method fails? Which line?
      └─ What are the inputs?
         └─ What is the EXPECTED output?
            └─ What is the ACTUAL output?

3. HYPOTHESIZE
   └─ What could cause the difference?
      └─ Write down your hypothesis

4. TEST
   └─ Add print statements or breakpoint at the hypothesis point
      └─ Check if your assumption is correct
         └─ If wrong, go back to step 3

5. FIX
   └─ Make the smallest possible change
      └─ Verify the fix
         └─ Check that you didn't BREAK anything else
```

---

### Exercises

1. Intentionally write code that throws: NullPointerException, ArrayIndexOutOfBoundsException, ArithmeticException, NumberFormatException. Practice reading each stack trace.
2. Use the IntelliJ debugger to step through a recursive factorial method. Watch the call stack grow and shrink.
3. Set a conditional breakpoint that triggers on the 10th iteration of a loop.
4. Fix a buggy program: try to identify and fix 5 artificially planted bugs.
5. Install `jd-gui` or use `javap` to decompile a .class file and understand what the compiler produced.
