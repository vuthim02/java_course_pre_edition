# Lesson 30: Variable Scope

## Key Concepts
- **Scope** defines where a variable is accessible
- **Local variables** — declared inside a method; only visible within that method
- **Global (class-level) variables** — declared with `static` in the class; visible to all methods
- **Block scope** — variables declared inside `{ }` blocks (e.g., `if`, loops) are only visible inside that block
- **Loop variables** — declared in the `for` header, scoped to the loop body

## Code Example

```java
public class Main {
    static int globalCount = 0;

    public static void main(String[] args) {
        int localCount = 5;
        System.out.println("Local variable in main: " + localCount);
        System.out.println("Global variable: " + globalCount);

        incrementGlobal();
        incrementGlobal();
        System.out.println("Global after 2 increments: " + globalCount);

        {
            int blockVar = 100;
            System.out.println("Block variable: " + blockVar);
        }

        for (int i = 0; i < 3; i++) {
            System.out.println("Loop variable i: " + i);
        }
    }

    static void incrementGlobal() {
        globalCount++;
        int localVar = 10;
        System.out.println("Local var in method: " + localVar);
    }
}
```

## Explanation
1. **`globalCount`** — declared at the class level with `static`. It is shared across all methods. Both `main` and `incrementGlobal()` can access and modify it.
2. **`localCount`** — declared inside `main()`. It only exists within `main`.
3. **`blockVar`** — declared inside an anonymous block `{ }`. It cannot be accessed outside that block.
4. **`i`** — declared in the `for` loop header. Its scope is limited to the loop body.
5. **`localVar`** — declared inside `incrementGlobal()`. It is separate from variables in `main()` and is created/destroyed with each method call.
6. Each call to `incrementGlobal()` increments the shared `globalCount`.

## Expected Output

```
Local variable in main: 5
Global variable: 0
Local var in method: 10
Local var in method: 10
Global after 2 increments: 2
Block variable: 100
Loop variable i: 0
Loop variable i: 1
Loop variable i: 2
```
