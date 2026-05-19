# Lesson 94: 2D Graphics

## Key Concepts
- Override `paintComponent(Graphics g)` in a `JPanel` to draw custom graphics
- Cast `Graphics` to `Graphics2D` for more control (strokes, antialiasing, etc.)
- Common shapes: rectangles, ovals, polygons, round rectangles, arcs, lines
- `fillXxx()` draws filled shapes; `drawXxx()` draws outlines
- `setColor()`, `setStroke()`, `setFont()` control rendering style
- Coordinates start at (0, 0) in the top-left corner; x increases right, y increases down

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("2D Graphics Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 500);

        DrawingPanel panel = new DrawingPanel();
        frame.add(panel);

        frame.setVisible(true);
    }
}

class DrawingPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(Color.RED);
        g2d.fillRect(50, 50, 100, 100);

        g2d.setColor(Color.BLUE);
        g2d.fillOval(200, 50, 100, 100);

        g2d.setColor(Color.GREEN);
        int[] xPoints = {400, 350, 450};
        int[] yPoints = {150, 50, 50};
        g2d.fillPolygon(xPoints, yPoints, 3);

        g2d.setColor(Color.ORANGE);
        g2d.fillRoundRect(50, 200, 150, 80, 20, 20);

        g2d.setColor(Color.MAGENTA);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(250, 200, 400, 280);

        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        g2d.drawString("2D Graphics", 200, 380);

        g2d.setColor(new Color(100, 50, 150, 100));
        g2d.fillArc(50, 320, 100, 100, 0, 180);
    }
}
```

## Explanation
1. `paintComponent(Graphics g)` is called automatically by Swing when the panel is first drawn or needs repainting. Always call `super.paintComponent(g)` first.
2. `Graphics2D g2d = (Graphics2D) g` — the `Graphics` object is actually a `Graphics2D` instance, giving access to advanced features.
3. **fillRect(x, y, w, h)** — filled rectangle at (50,50) with width/height 100.
4. **fillOval(x, y, w, h)** — filled oval bounded by the given rectangle.
5. **fillPolygon(xPoints, yPoints, n)** — filled triangle using three coordinate pairs.
6. **fillRoundRect(x, y, w, h, arcW, arch)** — rectangle with rounded corners.
7. **drawLine(x1, y1, x2, y2)** — a line with a 3px wide stroke.
8. **drawString(text, x, y)** — text drawn with a bold 24pt Arial font.
9. **fillArc(x, y, w, h, startAngle, arcAngle)** — a filled semicircle (180 degrees). The `Color` constructor with alpha (100) makes it semi-transparent.

## Expected Output

A 600x500 window displays:
- A red filled square at top-left
- A blue filled circle to its right
- A green filled triangle further right
- An orange rounded rectangle below the square
- A magenta diagonal line
- "2D Graphics" rendered in bold cyan
- A semi-transparent purple semicircle near the bottom-left
