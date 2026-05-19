# Lesson 5: Expressions

## Key Concepts
- **Expressions** are combinations of operands and operators that evaluate to a value
- Arithmetic operators: `+`, `-`, `*`, `/`, `%`
- Compound assignment operators: `+=`, `-=`, etc.
- Increment (`++`) and decrement (`--`) operators
- **Type casting** with `(double)` to perform decimal division

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        int friends = 10;

        friends = friends + 1;
        System.out.println("friends + 1 = " + friends);

        friends += 2;
        System.out.println("friends += 2 = " + friends);

        friends++;
        System.out.println("friends++ = " + friends);

        friends--;
        System.out.println("friends-- = " + friends);

        friends = friends * 2;
        System.out.println("friends * 2 = " + friends);

        friends = friends / 3;
        System.out.println("friends / 3 = " + friends);

        int remainder = friends % 3;
        System.out.println("friends % 3 = " + remainder);

        double result = (double) 10 / 3;
        System.out.println("10 / 3 = " + result);
    }
}
```

### Explanation
- `friends = friends + 1` — standard addition reassignment
- `friends += 2` — shorthand for `friends = friends + 2`
- `friends++` — increments by 1 (post-increment)
- `friends--` — decrements by 1
- `friends % 3` — **modulus** gives the remainder after division
- `(double) 10 / 3` — casts `10` to `double` so the division yields `3.333...` instead of `3` (integer truncation)

## Expected Output

```
friends + 1 = 11
friends += 2 = 13
friends++ = 14
friends-- = 13
friends * 2 = 26
friends / 3 = 8
friends % 3 = 2
10 / 3 = 3.3333333333333335
```
