# Lesson 80: JRadioButton

## Key Concepts
- `JRadioButton` for mutually exclusive options (single selection)
- `ButtonGroup` to enforce mutual exclusivity
- Checking selection state with `isSelected()`
- Using `if-else` chains to determine which option is chosen

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JRadioButton Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose a size:");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        frame.add(label);

        JRadioButton smallBtn = new JRadioButton("Small");
        JRadioButton mediumBtn = new JRadioButton("Medium");
        JRadioButton largeBtn = new JRadioButton("Large");

        smallBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        mediumBtn.setFont(new Font("Arial", Font.PLAIN, 16));
        largeBtn.setFont(new Font("Arial", Font.PLAIN, 16));

        ButtonGroup group = new ButtonGroup();
        group.add(smallBtn);
        group.add(mediumBtn);
        group.add(largeBtn);

        frame.add(smallBtn);
        frame.add(mediumBtn);
        frame.add(largeBtn);

        JLabel resultLabel = new JLabel("Select a size");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(resultLabel);

        JButton button = new JButton("Submit");
        button.addActionListener(e -> {
            if (smallBtn.isSelected()) resultLabel.setText("You chose Small");
            else if (mediumBtn.isSelected()) resultLabel.setText("You chose Medium");
            else if (largeBtn.isSelected()) resultLabel.setText("You chose Large");
            else resultLabel.setText("No selection");
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. Three `JRadioButton` components represent "Small", "Medium", and "Large".
2. All three are added to a `ButtonGroup`, which ensures **only one** can be selected at a time.
3. A "Submit" button checks each radio button with `isSelected()` in an `if-else` chain and updates the label accordingly.
4. If none is selected, a fallback message "No selection" is shown.

## Expected Output
- A window titled "JRadioButton Example" with the label "Choose a size:" and three radio buttons.
- Selecting one radio button automatically deselects any other.
- Clicking "Submit" displays the chosen size in the result label (e.g., "You chose Medium").
