# Lesson 72: BorderLayout

## Key Concepts
- `BorderLayout` divides a container into five regions: NORTH, SOUTH, EAST, WEST, and CENTER.
- Components are added with a `BorderLayout` constant to specify their region.
- `BorderLayout(hgap, vgap)`: sets horizontal and vertical gaps between regions.
- NORTH and SOUTH components get their preferred height; EAST and WEST get their preferred width; CENTER fills remaining space.
- Panels with labels are commonly used to demonstrate each region.

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("BorderLayout Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout(10, 10));

        JPanel northPanel = new JPanel();
        northPanel.setBackground(Color.RED);
        northPanel.add(new JLabel("NORTH"));
        frame.add(northPanel, BorderLayout.NORTH);

        JPanel southPanel = new JPanel();
        southPanel.setBackground(Color.GREEN);
        southPanel.add(new JLabel("SOUTH"));
        frame.add(southPanel, BorderLayout.SOUTH);

        JPanel eastPanel = new JPanel();
        eastPanel.setBackground(Color.YELLOW);
        eastPanel.add(new JLabel("EAST"));
        frame.add(eastPanel, BorderLayout.EAST);

        JPanel westPanel = new JPanel();
        westPanel.setBackground(Color.BLUE);
        westPanel.add(new JLabel("WEST"));
        frame.add(westPanel, BorderLayout.WEST);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.LIGHT_GRAY);
        centerPanel.add(new JLabel("CENTER"));
        frame.add(centerPanel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
```

## Explanation
1. The frame's layout is set to `BorderLayout` with 10px horizontal and vertical gaps.
2. Five colored panels are added to the five regions:
   - **NORTH** (red): spans the top.
   - **SOUTH** (green): spans the bottom.
   - **EAST** (yellow): sits on the right side.
   - **WEST** (blue): sits on the left side.
   - **CENTER** (light gray): fills the remaining middle area.
3. Each panel contains a `JLabel` identifying its region.

## Expected Output

A 500×400 window titled "BorderLayout Example" with five colored regions:
- Top: red panel labeled "NORTH"
- Bottom: green panel labeled "SOUTH"
- Left: blue panel labeled "WEST"
- Right: yellow panel labeled "EAST"
- Center: light gray panel labeled "CENTER"
- 10px gaps between each region
