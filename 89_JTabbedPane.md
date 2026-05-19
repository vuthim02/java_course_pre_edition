# Lesson 89: JTabbedPane

## Key Concepts
- `JTabbedPane` for organizing content into multiple tabs
- `addTab()` to add a tab with a title and a `JPanel`
- Creating different `JPanel` layouts for each tab
- Mixing components like `JLabel`, `JTextField`, `JButton`, and `JTextArea` across tabs

## Code Example

```java
import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JTabbedPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel panel1 = new JPanel();
        panel1.setBackground(Color.RED);
        panel1.add(new JLabel("This is the first tab."));
        tabbedPane.addTab("Tab 1", panel1);

        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.GREEN);
        panel2.setLayout(new FlowLayout());
        panel2.add(new JLabel("Name:"));
        panel2.add(new JTextField(15));
        panel2.add(new JButton("Submit"));
        tabbedPane.addTab("Form", panel2);

        JPanel panel3 = new JPanel();
        panel3.setBackground(Color.BLUE);
        panel3.add(new JTextArea(10, 30));
        tabbedPane.addTab("Notes", panel3);

        frame.add(tabbedPane);
        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JTabbedPane` is created — it manages multiple "pages" that the user can switch between by clicking tabs.
2. Three `JPanel`s are created, each with a different background color.
3. **Tab 1** — a simple red panel with a label.
4. **Tab 2 ("Form")** — a green panel with a `FlowLayout`, containing a label, text field, and button.
5. **Tab 3 ("Notes")** — a blue panel containing a multi-line `JTextArea`.
6. Each panel is added to the tabbed pane with `addTab("Tab Title", panel)`.
7. The `JTabbedPane` is added directly to the frame (which uses a `BorderLayout` by default, allowing the tabbed pane to fill the window).

## Expected Output
- A window titled "JTabbedPane Example" with three tabs: "Tab 1", "Form", and "Notes".
- Clicking **Tab 1** shows a red panel with the text "This is the first tab."
- Clicking **Form** shows a green panel with a name field and Submit button.
- Clicking **Notes** shows a blue panel with a multi-line text area.
