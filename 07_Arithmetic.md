# Lesson 7: Arithmetic

## Key Concepts
- All basic arithmetic operators: `+`, `-`, `*`, `/`, `%`
- **Integer division** truncates the decimal portion
- **Floating-point division** preserves decimals
- **Pre-increment** (`++x`) vs **post-increment** (`x++`)
- **Pre-decrement** (`--y`) vs **post-decrement** (`y--`)

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        int a = 15;
        int b = 4;

        System.out.println("a + b = " + (a + b));
        System.out.println("a - b = " + (a - b));
        System.out.println("a * b = " + (a * b));
        System.out.println("a / b = " + (a / b));
        System.out.println("a % b = " + (a % b));

        double c = 15.0;
        double d = 4.0;
        System.out.println("c / d = " + (c / d));

        System.out.println("\nIncrement/Decrement:");
        int x = 5;
        System.out.println("x++ = " + (x++));
        System.out.println("After x++: " + x);
        System.out.println("++x = " + (++x));

        int y = 10;
        System.out.println("y-- = " + (y--));
        System.out.println("After y--: " + y);
        System.out.println("--y = " + (--y));
    }
}
```

### Explanation
- `15 / 4` with integers gives `3` (truncated)
- `15.0 / 4.0` with doubles gives `3.75`
- `15 % 4` gives `3` (the remainder)
- **Post-increment** `x++` returns the current value *then* increments
- **Pre-increment** `++x` increments *then* returns the new value
- Same logic applies for decrement (`--`)

## Expected Output

```
a + b = 19
a - b = 11
a * b = 60
a / b = 3
a % b = 3
c / d = 3.75

Increment/Decrement:
x++ = 5
After x++: 6
++x = 7
y-- = 10
After y--: 9
--y = 8
```
