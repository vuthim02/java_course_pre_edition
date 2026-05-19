# Lesson 86: JColorChooser

## Key Concepts
- `JColorChooser.showDialog()` for opening a color picker
- Setting a component's background color with `setBackground()`
- `setOpaque(true)` to make background color visible on a `JLabel`
- Extracting red, green, and blue components with `getRed()`, `getGreen()`, `getBlue()`

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JColorChooser Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Choose a color!");
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setOpaque(true);
        label.setBackground(Color.WHITE);
        label.setPreferredSize(new Dimension(300, 100));
        label.setHorizontalAlignment(JLabel.CENTER);
        frame.add(label);

        JButton button = new JButton("Pick Color");
        button.addActionListener(e -> {
            Color color = JColorChooser.showDialog(frame, "Choose a color", Color.WHITE);
            if (color != null) {
                label.setBackground(color);
                label.setText("RGB: " + color.getRed() + ", " + color.getGreen() + ", " + color.getBlue());
            }
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. The label is made **opaque** (`setOpaque(true)`) so that its background color is visible.
2. `JColorChooser.showDialog()` takes three arguments: the parent component, dialog title, and initial color. It returns the selected `Color` or `null` if the user cancels.
3. If a color is chosen, the label's background is updated with `setBackground(color)`.
4. The label text changes to show the RGB values of the selected color (e.g., "RGB: 255, 0, 0" for pure red).

## Expected Output
- A window titled "JColorChooser Example" with a white label reading "Choose a color!" and a "Pick Color" button.
- Clicking the button opens a color chooser dialog.
- Selecting a color and clicking OK changes the label's background to that color and displays the RGB values.
