# Lesson 74: GridLayout

## Key Concepts
- `GridLayout(rows, cols, hgap, vgap)`: arranges components in a uniform grid.
- All cells in the grid are equal size.
- Components are added row-by-row, left-to-right.
- Great for calculator layouts, number pads, and form grids.
- `setFocusable(false)`: prevents a component from receiving keyboard focus.
- GridLayout can be nested inside a `BorderLayout` to create complex layouts (e.g., calculator grid in CENTER, display in NORTH).

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("GridLayout Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
        frame.setLayout(new GridLayout(3, 3, 5, 5));

        String[] labels = {"7", "8", "9", "4", "5", "6", "1", "2", "3"};
        for (String label : labels) {
            JButton button = new JButton(label);
            button.setFont(new Font("Arial", Font.BOLD, 30));
            button.setFocusable(false);
            frame.add(button);
        }

        JFrame calculatorFrame = new JFrame();
        calculatorFrame.setTitle("Calculator Layout");
        calculatorFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        calculatorFrame.setSize(300, 400);
        calculatorFrame.setLayout(new BorderLayout());

        JTextField display = new JTextField();
        display.setFont(new Font("Arial", Font.BOLD, 24));
        display.setHorizontalAlignment(JTextField.RIGHT);
        calculatorFrame.add(display, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 4, 3, 3));

        String[] calcButtons = {
            "7", "8", "9", "/",
            "4", "5", "6", "*",
            "1", "2", "3", "-",
            "0", ".", "=", "+"
        };

        for (String text : calcButtons) {
            JButton btn = new JButton(text);
            btn.setFont(new Font("Arial", Font.BOLD, 20));
            buttonPanel.add(btn);
        }

        calculatorFrame.add(buttonPanel, BorderLayout.CENTER);
        calculatorFrame.setLocation(450, 0);
        calculatorFrame.setVisible(true);

        frame.setVisible(true);
    }
}
```

## Explanation
1. **First frame**: A 3×3 grid with 5px gaps. Nine buttons labeled "7" through "3" (numpad layout) fill the grid cells in row-major order.
2. **Second frame (calculator)**: Uses `BorderLayout` with:
   - **NORTH**: a `JTextField` display (right-aligned, large font).
   - **CENTER**: a panel with `GridLayout(4, 4, 3, 3)` containing 16 calculator buttons (digits and operators).
3. `setFocusable(false)` prevents focus outlines on the numpad buttons.
4. `setLocation(450, 0)` positions the second frame to the right of the first one.

## Expected Output

**Window 1** (400×400): a 3×3 grid of buttons:
```
7 | 8 | 9
4 | 5 | 6
1 | 2 | 3
```

**Window 2** (300×400, positioned to the right): a calculator layout with a text field at the top and a 4×4 button grid:
```
7  8  9  /
4  5  6  *
1  2  3  -
0  .  =  +
```
