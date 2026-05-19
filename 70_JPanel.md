# Lesson 70: JPanel

## Key Concepts
- `JPanel` is a lightweight container that groups components together.
- Panels can have their own background color, size, and position.
- Components (like `JLabel`, `JButton`) can be added to a panel instead of directly to the frame.
- `setBounds(x, y, width, height)`: positions and sizes the panel (with `null` layout).
- Panels enable organizing related UI elements visually.

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JPanel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLayout(null);

        JPanel redPanel = new JPanel();
        redPanel.setBackground(Color.RED);
        redPanel.setBounds(0, 0, 200, 200);

        JPanel bluePanel = new JPanel();
        bluePanel.setBackground(Color.BLUE);
        bluePanel.setBounds(200, 0, 200, 200);

        JPanel greenPanel = new JPanel();
        greenPanel.setBackground(Color.GREEN);
        greenPanel.setBounds(0, 200, 400, 150);

        JLabel label = new JLabel("Hello!");
        label.setFont(new Font("Arial", Font.BOLD, 20));
        label.setForeground(Color.WHITE);
        redPanel.add(label);

        JButton button = new JButton("Click me");
        greenPanel.add(button);

        frame.add(redPanel);
        frame.add(bluePanel);
        frame.add(greenPanel);

        frame.setVisible(true);
    }
}
```

## Explanation
1. Three panels are created with different background colors and positions:
   - **Red panel**: top-left (0, 0), 200×200px
   - **Blue panel**: top-right (200, 0), 200×200px
   - **Green panel**: bottom (0, 200), 400×150px
2. A `JLabel` with white bold text `"Hello!"` is added to the red panel.
3. A `JButton` labeled `"Click me"` is added to the green panel.
4. The blue panel is left empty to show its background color.
5. All three panels are added to the frame.

## Expected Output

A 600×400 window with three colored sections:
- Top-left: red panel containing white text `"Hello!"`
- Top-right: blue panel (empty)
- Bottom: green panel containing a `"Click me"` button
