# Lesson 25: For Loops

## Key Concepts
- `for` loop syntax: `for (initialization; condition; update)`
- Counting up and counting down
- Custom step values (`i += 2`)
- Using loop variables in calculations
- Accumulating a sum with a loop

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println("Count 1 to 10:");
        for (int i = 1; i <= 10; i++) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("\nCount down from 10:");
        for (int i = 10; i >= 1; i--) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("\nEven numbers 2-20:");
        for (int i = 2; i <= 20; i += 2) {
            System.out.print(i + " ");
        }
        System.out.println();

        System.out.println("\nMultiplication table of 5:");
        for (int i = 1; i <= 10; i++) {
            System.out.println("5 x " + i + " = " + (5 * i));
        }

        int sum = 0;
        for (int i = 1; i <= 100; i++) {
            sum += i;
        }
        System.out.println("\nSum of 1-100: " + sum);
    }
}
```

## Explanation
1. **Count up**: `i` starts at 1, runs while `i <= 10`, increments by 1.
2. **Count down**: `i` starts at 10, runs while `i >= 1`, decrements by 1.
3. **Step by 2**: `i += 2` produces even numbers from 2 to 20.
4. **Multiplication table**: the loop variable drives the calculation `5 * i`.
5. **Summation**: accumulate `i` into `sum` on each iteration.

## Expected Output

```
Count 1 to 10:
1 2 3 4 5 6 7 8 9 10 

Count down from 10:
10 9 8 7 6 5 4 3 2 1 

Even numbers 2-20:
2 4 6 8 10 12 14 16 18 20 

Multiplication table of 5:
5 x 1 = 5
5 x 2 = 10
5 x 3 = 15
5 x 4 = 20
5 x 5 = 25
5 x 6 = 30
5 x 7 = 35
5 x 8 = 40
5 x 9 = 45
5 x 10 = 50

Sum of 1-100: 5050
```
