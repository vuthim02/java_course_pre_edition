# Lesson 68: GUI Introduction — JFrame

## Key Concepts
- `javax.swing.JFrame` is the main window container in Swing GUI programming.
- `setTitle(String)`: sets the window title.
- `setDefaultCloseOperation(int)`: defines the close behavior — `JFrame.EXIT_ON_CLOSE` exits the application.
- `setSize(width, height)`: sets the window dimensions in pixels.
- `setResizable(boolean)`: allows or prevents window resizing.
- `setVisible(true)`: makes the window visible on screen.
- `getContentPane().setBackground(Color)`: changes the background color of the content pane.
- `Color(r, g, b)`: creates a custom RGB color.
- A `JLabel` can display text and be added to the frame.

## Code Example

```java
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();

        frame.setTitle("Java GUI - JFrame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setResizable(true);
        frame.setVisible(true);

        frame.getContentPane().setBackground(new Color(50, 50, 50));

        JLabel label = new JLabel();
        label.setText("Hello, GUI!");
        label.setForeground(Color.WHITE);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);
        frame.add(label);
    }
}
```

## Explanation
1. A `JFrame` object is created (initially invisible).
2. `setTitle()` sets the window title to `"Java GUI - JFrame"`.
3. `setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)` ensures the program exits when the window is closed.
4. `setSize(500, 400)` gives the window a width of 500px and height of 400px.
5. The background of the content pane is set to dark gray using `new Color(50, 50, 50)`.
6. A `JLabel` with centered white text `"Hello, GUI!"` is added to the frame.
7. `setVisible(true)` displays the window.

## Expected Output

A window appears with:
- Title: `Java GUI - JFrame`
- Size: 500 × 400 pixels
- Dark gray background
- Centered white text: `"Hello, GUI!"`
