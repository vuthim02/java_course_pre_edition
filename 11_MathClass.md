# Lesson 11: Math Class

## Key Concepts
- Java's built-in `Math` class provides useful mathematical methods
- `Math.max()` / `Math.min()` — find largest/smallest of two values
- `Math.abs()` — absolute value
- `Math.sqrt()` — square root
- `Math.round()` / `Math.ceil()` / `Math.floor()` — rounding
- `Math.pow()` — exponentiation
- `Math.PI` and `Math.E` — mathematical constants
- `Math.toRadians()` and trig functions: `sin()`, `cos()`, `tan()`

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        double x = 3.14;
        double y = -10.0;

        System.out.println("Math.max(x, y): " + Math.max(x, y));
        System.out.println("Math.min(x, y): " + Math.min(x, y));
        System.out.println("Math.abs(y): " + Math.abs(y));
        System.out.println("Math.sqrt(25): " + Math.sqrt(25));
        System.out.println("Math.round(x): " + Math.round(x));
        System.out.println("Math.ceil(x): " + Math.ceil(x));
        System.out.println("Math.floor(x): " + Math.floor(x));
        System.out.println("Math.pow(2, 3): " + Math.pow(2, 3));
        System.out.println("Math.PI: " + Math.PI);
        System.out.println("Math.E: " + Math.E);

        double a = 30;
        double radians = Math.toRadians(a);
        System.out.println("\nsin(30): " + Math.sin(radians));
        System.out.println("cos(30): " + Math.cos(radians));
        System.out.println("tan(30): " + Math.tan(radians));
    }
}
```

### Explanation
- All `Math` methods are **static** — call them directly on the class (no object needed)
- `Math.round(3.14)` → `3` (rounds to nearest integer)
- `Math.ceil(3.14)` → `4.0` (rounds up)
- `Math.floor(3.14)` → `3.0` (rounds down)
- `Math.pow(2, 3)` → `8.0` (2³)
- Trig functions expect **radians**, so convert degrees with `Math.toRadians()`

## Expected Output

```
Math.max(x, y): 3.14
Math.min(x, y): -10.0
Math.abs(y): 10.0
Math.sqrt(25): 5.0
Math.round(x): 3
Math.ceil(x): 4.0
Math.floor(x): 3.0
Math.pow(2, 3): 8.0
Math.PI: 3.141592653589793
Math.E: 2.718281828459045

sin(30): 0.49999999999999994
cos(30): 0.8660254037844387
tan(30): 0.5773502691896257
```
