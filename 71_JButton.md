# Lesson 71: JButton

## Key Concepts
- `JButton` is a clickable button component in Swing.
- `addActionListener(ActionListener)`: registers an event handler for button clicks.
- `ActionListener` can be implemented as an anonymous inner class or a lambda expression.
- `ActionEvent` provides information about the click event.
- Buttons can update other components (e.g., changing `JLabel` text) when clicked.
- `setFont(Font)`: customizes the button's text appearance.
- `FlowLayout` arranges components left-to-right in a row.

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JButton Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Click a button!");
        label.setFont(new Font("Arial", Font.PLAIN, 20));
        frame.add(label);

        JButton button1 = new JButton("Say Hello");
        button1.setFont(new Font("Arial", Font.BOLD, 16));
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                label.setText("Hello there!");
            }
        });
        frame.add(button1);

        JButton button2 = new JButton("Count");
        button2.setFont(new Font("Arial", Font.BOLD, 16));
        final int[] count = {0};
        button2.addActionListener(e -> {
            count[0]++;
            label.setText("Count: " + count[0]);
        });
        frame.add(button2);

        JButton button3 = new JButton("Reset");
        button3.setFont(new Font("Arial", Font.BOLD, 16));
        button3.addActionListener(e -> {
            count[0] = 0;
            label.setText("Reset!");
        });
        frame.add(button3);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JFrame` uses `FlowLayout` to arrange components left to right.
2. A `JLabel` displays status text, initially `"Click a button!"`.
3. **Button 1 ("Say Hello")**: Uses an anonymous `ActionListener` class. On click, sets the label to `"Hello there!"`.
4. **Button 2 ("Count")**: Uses a lambda expression. Increments a counter and displays `"Count: N"` on the label. The counter is stored in a single-element int array to work around the effectively-final requirement.
5. **Button 3 ("Reset")**: Uses a lambda. Resets the counter to 0 and sets the label to `"Reset!"`.
6. Lambda expressions (`e -> { ... }`) provide a concise way to handle events.

## Expected Output

A 500×400 window with:
- A label: `"Click a button!"`
- Three buttons side by side: `"Say Hello"`, `"Count"`, `"Reset"`
- Clicking "Say Hello" changes the label to `"Hello there!"`
- Clicking "Count" increments and displays the count
- Clicking "Reset" resets the count to 0
