# Lesson 79: JCheckBox

## Key Concepts
- `JCheckBox` for toggle-able options (multiple selection)
- Checking state with `isSelected()`
- Building a selection string with `StringBuilder`
- Handling checkbox selection via a button click

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JCheckBox Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Select your toppings:");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        frame.add(label);

        JCheckBox cheeseBox = new JCheckBox("Cheese");
        cheeseBox.setFont(new Font("Arial", Font.PLAIN, 16));

        JCheckBox pepperoniBox = new JCheckBox("Pepperoni");
        pepperoniBox.setFont(new Font("Arial", Font.PLAIN, 16));

        JCheckBox mushroomBox = new JCheckBox("Mushroom");
        mushroomBox.setFont(new Font("Arial", Font.PLAIN, 16));

        frame.add(cheeseBox);
        frame.add(pepperoniBox);
        frame.add(mushroomBox);

        JLabel resultLabel = new JLabel("Selected: ");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(resultLabel);

        JButton orderButton = new JButton("Place Order");
        orderButton.addActionListener(e -> {
            StringBuilder selected = new StringBuilder("Selected: ");
            if (cheeseBox.isSelected()) selected.append("Cheese ");
            if (pepperoniBox.isSelected()) selected.append("Pepperoni ");
            if (mushroomBox.isSelected()) selected.append("Mushroom ");
            resultLabel.setText(selected.toString());
        });
        frame.add(orderButton);

        frame.setVisible(true);
    }
}
```

## Explanation
1. Three `JCheckBox` components are created for "Cheese", "Pepperoni", and "Mushroom".
2. Unlike radio buttons, checkboxes are **independent** — the user can select any combination.
3. When the "Place Order" button is clicked, `isSelected()` checks each checkbox. A `StringBuilder` concatenates all selected toppings.
4. The result is displayed in the `resultLabel`.

## Expected Output
- A window titled "JCheckBox Example" showing a label "Select your toppings:" and three checkboxes.
- The user checks zero or more toppings and clicks "Place Order".
- The result label updates to show e.g., "Selected: Cheese Mushroom".
