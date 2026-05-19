# Lesson 69: JLabel

## Key Concepts
- `JLabel` displays a short text string or image on a GUI.
- `setText(String)`: sets the label's text.
- `setFont(Font)`: changes the font (family, style, size).
- `Font` constants: `Font.BOLD`, `Font.ITALIC`, `Font.PLAIN`.
- `setForeground(Color)`: sets the text color.
- `setBackground(Color)`: sets the background color (requires `setOpaque(true)`).
- `setOpaque(boolean)`: when `true`, the label paints its background.
- `setBounds(x, y, width, height)`: sets position and size (requires `null` layout).

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JLabel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(null);

        JLabel label1 = new JLabel();
        label1.setText("Welcome to Java GUI!");
        label1.setFont(new Font("Arial", Font.BOLD, 24));
        label1.setForeground(Color.BLUE);
        label1.setBounds(100, 50, 300, 50);

        JLabel label2 = new JLabel();
        label2.setText("This is a JLabel example.");
        label2.setFont(new Font("Serif", Font.ITALIC, 18));
        label2.setForeground(new Color(0, 128, 0));
        label2.setBounds(100, 120, 300, 40);

        JLabel label3 = new JLabel();
        label3.setText("You can customize fonts and colors!");
        label3.setFont(new Font("Monospaced", Font.PLAIN, 14));
        label3.setForeground(Color.RED);
        label3.setBounds(100, 180, 300, 40);
        label3.setOpaque(true);
        label3.setBackground(Color.YELLOW);

        frame.add(label1);
        frame.add(label2);
        frame.add(label3);

        frame.setVisible(true);
    }
}
```

## Explanation
1. The frame uses `setLayout(null)` for absolute positioning of components.
2. **label1**: Bold Arial 24pt, blue text, positioned at (100, 50).
3. **label2**: Italic Serif 18pt, dark green text, positioned at (100, 120).
4. **label3**: Plain Monospaced 14pt, red text on a yellow background (opaque enabled), positioned at (100, 180).
5. Each label is added to the frame with `frame.add()`.
6. `setVisible(true)` displays the window with all three labels.

## Expected Output

A 500×400 window titled "JLabel Example" containing three labels stacked vertically:
- `"Welcome to Java GUI!"` in bold blue Arial
- `"This is a JLabel example."` in italic green Serif
- `"You can customize fonts and colors!"` in red Monospaced on a yellow background
