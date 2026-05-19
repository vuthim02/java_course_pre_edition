# Lesson 73: FlowLayout

## Key Concepts
- `FlowLayout` arranges components left-to-right in a row, wrapping to the next line when the row is full.
- `FlowLayout(align, hgap, vgap)`: sets alignment and gaps.
  - Alignment options: `FlowLayout.LEFT`, `FlowLayout.CENTER`, `FlowLayout.RIGHT`.
- `setPreferredSize(Dimension)`: hints at a component's preferred size (honored by FlowLayout).
- Components keep their natural size — FlowLayout does not stretch them.
- Multiple containers can have independent layouts (e.g., a frame and a nested panel).

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("FlowLayout Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));

        for (int i = 1; i <= 10; i++) {
            JButton button = new JButton("Button " + i);
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            button.setPreferredSize(new Dimension(100, 40));
            frame.add(button);
        }

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        panel.setBackground(Color.LIGHT_GRAY);
        panel.setPreferredSize(new Dimension(450, 50));
        panel.add(new JLabel("Left aligned: "));
        panel.add(new JButton("A"));
        panel.add(new JButton("B"));
        panel.add(new JButton("C"));
        frame.add(panel);

        frame.setVisible(true);
    }
}
```

## Explanation
1. The frame uses `FlowLayout.CENTER` with 10px horizontal and vertical gaps.
2. **10 buttons** are created in a loop and added to the frame. Each button has a preferred size of 100×40px. They flow left-to-right and wrap when the row fills.
3. A **nested panel** uses `FlowLayout.LEFT` with 5px gaps and a light gray background. It contains a label and three buttons ("A", "B", "C").
4. The panel's preferred size (450×50) hints at its dimensions, and FlowLayout places its children from left to right.

## Expected Output

A 500×400 window titled "FlowLayout Example" with:
- A top section: buttons "Button 1" through "Button 10", centered and wrapping to a second row if needed.
- A bottom section: a light gray panel with left-aligned label `"Left aligned: "` followed by buttons "A", "B", "C".
