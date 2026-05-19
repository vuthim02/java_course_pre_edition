# Lesson 47: Overloaded Constructors

## Key Concepts
- **Overloaded constructors** allow multiple constructor definitions with different parameter lists
- Java determines which constructor to call based on the arguments provided
- Overloading provides flexibility — objects can be created with varying levels of detail
- A common pattern is to provide constructors from minimal to full parameter sets
- Each constructor must have a unique signature (different number or type of parameters)

## Code Example

```java
public class Main {
    public static void main(String[] args) {
        Pizza pizza1 = new Pizza("Thin Crust");
        Pizza pizza2 = new Pizza("Thick Crust", "Tomato");
        Pizza pizza3 = new Pizza("Regular", "Tomato", "Mozzarella");
        Pizza pizza4 = new Pizza("Pan", "Marinara", "Provolone", "Pepperoni");

        System.out.println("Pizza 1:");
        pizza1.display();

        System.out.println("\nPizza 2:");
        pizza2.display();

        System.out.println("\nPizza 3:");
        pizza3.display();

        System.out.println("\nPizza 4:");
        pizza4.display();
    }
}

class Pizza {
    String bread;
    String sauce;
    String cheese;
    String topping;

    Pizza(String bread) {
        this.bread = bread;
    }

    Pizza(String bread, String sauce) {
        this.bread = bread;
        this.sauce = sauce;
    }

    Pizza(String bread, String sauce, String cheese) {
        this.bread = bread;
        this.sauce = sauce;
        this.cheese = cheese;
    }

    Pizza(String bread, String sauce, String cheese, String topping) {
        this.bread = bread;
        this.sauce = sauce;
        this.cheese = cheese;
        this.topping = topping;
    }

    void display() {
        System.out.println("Bread: " + bread);
        if (sauce != null) System.out.println("Sauce: " + sauce);
        if (cheese != null) System.out.println("Cheese: " + cheese);
        if (topping != null) System.out.println("Topping: " + topping);
    }
}
```

### Explanation
`Pizza` has four overloaded constructors. Depending on how many arguments are passed, the matching constructor runs. Fields not set remain `null` (the default for object references). The `display()` method checks for `null` before printing each optional field.

### Expected Output
```
Pizza 1:
Bread: Thin Crust

Pizza 2:
Bread: Thick Crust
Sauce: Tomato

Pizza 3:
Bread: Regular
Sauce: Tomato
Cheese: Mozzarella

Pizza 4:
Bread: Pan
Sauce: Marinara
Cheese: Provolone
Topping: Pepperoni
```
