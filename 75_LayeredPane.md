# Lesson 75: JLayeredPane

## Key Concepts
- `JLayeredPane` is a container that manages components in depth (z-order).
- Components are assigned an `Integer` **layer** value — higher values appear on top.
- Default layers: `DEFAULT_LAYER` (0), `PALETTE_LAYER` (100), `MODAL_LAYER` (200), `POPUP_LAYER` (300), `DRAG_LAYER` (400).
- Components in higher layers overlap components in lower layers.
- Within the same layer, the order of addition determines z-order.
- `setBounds()` is used to position and size components when using a layered pane.

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("LayeredPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBounds(0, 0, 500, 400);

        JLabel label1 = new JLabel();
        label1.setOpaque(true);
        label1.setBackground(Color.RED);
        label1.setBounds(50, 50, 200, 200);

        JLabel label2 = new JLabel();
        label2.setOpaque(true);
        label2.setBackground(Color.BLUE);
        label2.setBounds(100, 100, 200, 200);

        JLabel label3 = new JLabel();
        label3.setOpaque(true);
        label3.setBackground(Color.GREEN);
        label3.setBounds(150, 150, 200, 200);

        layeredPane.add(label1, Integer.valueOf(1));
        layeredPane.add(label2, Integer.valueOf(2));
        layeredPane.add(label3, Integer.valueOf(3));

        frame.add(layeredPane);
        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JLayeredPane` is created and sized to fill the frame (500×400).
2. Three opaque `JLabel` components are created with different colors and positions:
   - **Red** at (50, 50), 200×200 — layer 1 (bottom)
   - **Blue** at (100, 100), 200×200 — layer 2 (middle)
   - **Green** at (150, 150), 200×200 — layer 3 (top)
3. Each label is added to the layered pane with an `Integer` layer value.
4. The layered pane is added to the frame.
5. Because each label is in a higher layer, they overlap with the higher-layer label drawn on top.

## Expected Output

A 500×400 window titled "LayeredPane Example" showing three overlapping colored squares:
- Bottom layer: red square at (50, 50)
- Middle layer: blue square at (100, 100), partially covering red
- Top layer: green square at (150, 150), partially covering blue
- Each square is 200×200 pixels
