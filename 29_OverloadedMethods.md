# Lesson 29: Overloaded Methods

## Key Concepts
- Method overloading: multiple methods with the *same name* but different parameters
- Distinguished by number or type of parameters
- The compiler picks the right version based on the arguments
- Overloading improves readability and reusability

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        System.out.println(add(5, 10));
        System.out.println(add(5, 10, 15));
        System.out.println(add(5.5, 10.2));
        System.out.println(add(5.5, 10.2, 15.3));

        System.out.println("\nPizza sizes:");
        System.out.println(pizzaPrice("small"));
        System.out.println(pizzaPrice("medium"));
        System.out.println(pizzaPrice("large"));
        System.out.println(pizzaPrice("extra large", 3));
    }

    static int add(int a, int b) {
        System.out.print("int + int = ");
        return a + b;
    }

    static int add(int a, int b, int c) {
        System.out.print("int + int + int = ");
        return a + b + c;
    }

    static double add(double a, double b) {
        System.out.print("double + double = ");
        return a + b;
    }

    static double add(double a, double b, double c) {
        System.out.print("double + double + double = ");
        return a + b + c;
    }

    static String pizzaPrice(String size) {
        return switch (size) {
            case "small" -> "Small pizza: $8.99";
            case "medium" -> "Medium pizza: $10.99";
            case "large" -> "Large pizza: $12.99";
            default -> "Unknown size";
        };
    }

    static String pizzaPrice(String size, int toppings) {
        double base = switch (size) {
            case "small" -> 8.99;
            case "medium" -> 10.99;
            case "large" -> 12.99;
            case "extra large" -> 15.99;
            default -> 0;
        };
        double total = base + (toppings * 1.50);
        return size + " pizza with " + toppings + " topping(s): $" + String.format("%.2f", total);
    }
}
```

## Explanation
1. Four overloaded `add` methods: different parameter counts (2 vs 3) and types (`int` vs `double`).
2. Java selects the correct overload at compile time based on the arguments passed.
3. `pizzaPrice(String)` returns a price string for a given size.
4. `pizzaPrice(String, int)` overloads by accepting a topping count, calculating the total with a base price plus $1.50 per topping.
5. Overloading makes the API intuitive — one method name handles multiple input variations.

## Expected Output

```
int + int = 15
int + int + int = 30
double + double = 15.7
double + double + double = 31.0

Pizza sizes:
Small pizza: $8.99
Medium pizza: $10.99
Large pizza: $12.99
extra large pizza with 3 topping(s): $20.49
```
