# Lesson 81: JComboBox

## Key Concepts
- `JComboBox` for a dropdown selection list
- Generic type parameter `<String>` to specify item type
- `getSelectedItem()` to retrieve the chosen item
- Using `addActionListener` to react to selection changes

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JComboBox Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        String[] animals = {"Dog", "Cat", "Bird", "Fish", "Hamster"};
        JComboBox<String> comboBox = new JComboBox<>(animals);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 16));
        comboBox.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            JOptionPane.showMessageDialog(frame, "You selected: " + selected);
        });
        frame.add(comboBox);

        JLabel label = new JLabel("Select an animal:");
        label.setFont(new Font("Arial", Font.BOLD, 16));
        frame.add(label);

        JButton button = new JButton("Show Selection");
        button.addActionListener(e -> {
            String selected = (String) comboBox.getSelectedItem();
            JOptionPane.showMessageDialog(frame, "Selected: " + selected);
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `String` array of animal names is created and passed to the `JComboBox` constructor.
2. `JComboBox<String>` uses generics to ensure type safety.
3. An `ActionListener` on the combo box fires immediately when the user makes a selection from the dropdown.
4. A separate "Show Selection" button also reads `getSelectedItem()` to display the currently selected item.
5. `getSelectedItem()` returns an `Object`, so it is cast to `String`.

## Expected Output
- A window titled "JComboBox Example" with a dropdown containing "Dog", "Cat", "Bird", "Fish", "Hamster".
- Selecting an item from the dropdown immediately shows a popup with the selection.
- Clicking "Show Selection" also shows a popup with the currently selected item.
