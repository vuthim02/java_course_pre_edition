# Lesson 84: JMenuBar

## Key Concepts
- `JMenuBar` for creating a menu bar at the top of a window
- `JMenu` for dropdown menu categories
- `JMenuItem` for individual clickable menu options
- `addSeparator()` for adding divider lines in menus
- `setJMenuBar()` to attach the menu bar to a `JFrame`

## Code Example

```java
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Main {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setTitle("JMenuBar Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new FlowLayout());

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem exitItem = new JMenuItem("Exit");

        newItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "New file created"));
        openItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Open file dialog"));
        saveItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "File saved"));
        exitItem.addActionListener(e -> System.exit(0));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem cutItem = new JMenuItem("Cut");
        JMenuItem copyItem = new JMenuItem("Copy");
        JMenuItem pasteItem = new JMenuItem("Paste");

        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(frame, "Java GUI Example v1.0"));
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);
        frame.setVisible(true);
    }
}
```

## Explanation
1. A `JMenuBar` is created and three `JMenu` objects — "File", "Edit", "Help" — are added to it.
2. Each menu contains `JMenuItem` entries. Menu items have `ActionListener`s attached to respond to clicks.
3. `fileMenu.addSeparator()` visually separates "Save" from "Exit".
4. The menu bar is attached to the frame via `frame.setJMenuBar(menuBar)`.
5. Each action shows a `JOptionPane` dialog, except "Exit" which calls `System.exit(0)`.

## Expected Output
- A window titled "JMenuBar Example" with a menu bar displaying "File", "Edit", and "Help".
- Clicking **File** shows a dropdown: New, Open, Save, a separator, and Exit.
- Clicking **Edit** shows Cut, Copy, Paste.
- Clicking **Help** shows About, which displays "Java GUI Example v1.0".
- Each menu item triggers its respective action or dialog.
