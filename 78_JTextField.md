# Lesson 78: JTextField

## Key Concepts
- `JTextField` for single-line text input
- Using `addActionListener` to respond to the Enter key
- Retrieving text with `getText()`
- Combining `JTextField` with `JButton` and `JOptionPane`

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JTextField Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter your name:");
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        frame.add(label);

        JTextField textField = new JTextField(20);
        textField.setFont(new Font("Arial", Font.PLAIN, 16));
        textField.addActionListener(e -> {
            label.setText("Hello, " + textField.getText() + "!");
        });
        frame.add(textField);

        JButton button = new JButton("Submit");
        button.addActionListener(e -> {
            String text = textField.getText();
            if (!text.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Hello, " + text + "!");
            }
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JTextField` with a width of 20 columns is created and added to the frame.
2. An `ActionListener` on the text field triggers when the user presses **Enter** — it updates the label text directly.
3. A "Submit" button also reads the text field's contents using `getText()` and displays a `JOptionPane` message dialog.
4. The text field's font is customized with `setFont()`.

## Expected Output
- A window titled "JTextField Example" with a label, a text input field, and a "Submit" button.
- Typing a name and pressing **Enter** updates the label to say "Hello, [name]!".
- Clicking "Submit" opens a `JOptionPane` message dialog saying "Hello, [name]!".
