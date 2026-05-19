# Lesson 8: Shopping Cart Program

## Key Concepts
- Building a practical program that combines variables, input, and arithmetic
- Reading different data types: `String`, `double`, `int`
- Calculating a total: `price × quantity`
- Formatting output with a receipt-style layout

## Code Example

```java
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== SHOPPING CART ===");
        System.out.print("Enter item name: ");
        String item = scanner.nextLine();

        System.out.print("Enter price: $");
        double price = scanner.nextDouble();

        System.out.print("Enter quantity: ");
        int quantity = scanner.nextInt();

        double total = price * quantity;

        System.out.println("\n=== RECEIPT ===");
        System.out.println("Item: " + item);
        System.out.println("Price: $" + price);
        System.out.println("Quantity: " + quantity);
        System.out.println("Total: $" + total);

        scanner.close();
    }
}
```

### Explanation
- The program simulates a simple point-of-sale receipt
- `nextLine()` reads the item name (as a `String`)
- `nextDouble()` reads the price
- `nextInt()` reads the quantity
- The total is calculated as `price * quantity`
- The receipt displays all entered info plus the calculated total

## Expected Output

```
=== SHOPPING CART ===
Enter item name: Notebook
Enter price: $4.99
Enter quantity: 3

=== RECEIPT ===
Item: Notebook
Price: $4.99
Quantity: 3
Total: $14.97
```
