# Lesson 88: JList

## Key Concepts
- `JList` for displaying a selectable list of items
- Generic type `JList<String>` for type safety
- `setSelectionMode()` with `ListSelectionModel.SINGLE_SELECTION`
- `setVisibleRowCount()` to control how many rows are visible
- `ListSelectionListener` to react to selection changes
- `getSelectedValue()` to retrieve the selected item
- Wrapping in `JScrollPane` for scrolling

## Code Example

```java
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JList Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLayout(new FlowLayout());

        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5", "Item 6"};
        JList<String> list = new JList<>(items);
        list.setFont(new Font("Arial", Font.PLAIN, 16));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(4);

        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = list.getSelectedValue();
                if (selected != null) {
                    JOptionPane.showMessageDialog(frame, "Selected: " + selected);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(200, 150));
        frame.add(scrollPane);

        JButton button = new JButton("Get Selection");
        button.addActionListener(e -> {
            String selected = list.getSelectedValue();
            JOptionPane.showMessageDialog(frame, "Selected: " + selected);
        });
        frame.add(button);

        frame.setVisible(true);
    }
}
```

## Explanation
1. A `String` array of items is passed to the `JList` constructor. The generic `JList<String>` ensures type safety.
2. `setSelectionMode(ListSelectionModel.SINGLE_SELECTION)` allows only one item to be selected at a time.
3. `setVisibleRowCount(4)` displays 4 items at once; the rest are accessible via scrolling.
4. A `ListSelectionListener` is added — the `getValueIsAdjusting()` check prevents the event from firing twice during a single selection change.
5. Clicking the "Get Selection" button also reads `getSelectedValue()` and displays it in a dialog.
6. The list is wrapped in a `JScrollPane` with a preferred size.

## Expected Output
- A window titled "JList Example" showing a scrollable list of "Item 1" through "Item 6" (4 visible at a time) and a "Get Selection" button.
- Clicking any list item immediately opens a dialog saying "Selected: Item X".
- Clicking the button also opens a dialog showing the currently selected item.
